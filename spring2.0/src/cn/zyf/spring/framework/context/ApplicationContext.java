package cn.zyf.spring.framework.context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.zyf.spring.framework.annotation.Autowired;
import cn.zyf.spring.framework.annotation.Controller;
import cn.zyf.spring.framework.annotation.Service;
import cn.zyf.spring.framework.aop.AopConfig;
import cn.zyf.spring.framework.beans.BeanDefinition;
import cn.zyf.spring.framework.beans.BeanPostProcessor;
import cn.zyf.spring.framework.beans.BeanWrapper;
import cn.zyf.spring.framework.context.support.BeanDefinitionReader;
import cn.zyf.spring.framework.core.BeanFactory;

public class ApplicationContext extends DefaultListableBeanFactory implements BeanFactory {

	private String[] configLocations;

	private BeanDefinitionReader reader;

	// 用来保证注册式单例模式的容器
	private Map<String, Object> beanCacheMap = new ConcurrentHashMap<String, Object>();

	// 用来存储所有的被代理过的对象
	private Map<String, BeanWrapper> beanWrapperMap = new ConcurrentHashMap<String, BeanWrapper>();

	public ApplicationContext(String... configLocations) {
		this.configLocations = configLocations;
		refresh();
	}

	public void refresh() {

		// 定位
		this.reader = new BeanDefinitionReader(configLocations);

		// 加载
		List<String> beanDefinitions = reader.loadBeanDefinitions();
		// 注册
		doRegistry(beanDefinitions);
		// 依赖注入(lazy-init=false),要执行依赖注入
		// 在这里自动调用getBean方法
		doAutowired();
		
	}

	// 开始执行自动化的依赖注入
	private void doAutowired() {
		for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : this.beanDefinitionsMap.entrySet()) {
			String beanName = beanDefinitionEntry.getKey();
			if (!beanDefinitionEntry.getValue().isLazyInit()) {
				getBean(beanName);
			}
		}
		for (Map.Entry<String, BeanWrapper> beanWrapperEntry : this.beanWrapperMap.entrySet()) {
			// 用原生类作为参数 注入的是代理类
			populateBean(beanWrapperEntry.getKey(), beanWrapperEntry.getValue().getOriginalInstance());
		}
	}

	public void populateBean(String beanName, Object instance) {
		Class<?> clazz = instance.getClass();
		if (!(clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class))) {
			return;
		}
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (!field.isAnnotationPresent(Autowired.class)) {
				continue;
			}
			Autowired autowired = field.getAnnotation(Autowired.class);
			String autowiredBeanName = autowired.value().trim();
			if ("".equals(autowiredBeanName)) {
				autowiredBeanName = field.getType().getName();
			}
			field.setAccessible(true);
			try {
				// 注入的实际是代理类
				field.set(instance, this.beanWrapperMap.get(autowiredBeanName).getWrappedInstance());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	// 真正的将beanDefinitions注册到beanDefinitionsMap中
	private void doRegistry(List<String> beanDefinitions) {
		// beanName有三种情况
		// 1.默认是类名首字母小写
		// 2.自定义名字
		// 3.接口注入
		try {
			for (String className : beanDefinitions) {
				Class<?> beanClass = Class.forName(className);
				// 如果是一个接口,是不能实例化的
				// 用它的实现类实例化
				if (beanClass.isInterface()) {
					continue;
				}
				BeanDefinition beanDefinition = reader.registerBean(className);
				if (beanDefinition != null) {
					this.beanDefinitionsMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
				}
				Class<?>[] interfaces = beanClass.getInterfaces();
				for (Class<?> i : interfaces) {
					// 如果是多个实现类，只能覆盖，因为Spring没有那么智能
					// 于是这个时候可以自定义名字
					this.beanDefinitionsMap.put(i.getName(), beanDefinition);
				}
				// 到这里，容器初始化完毕
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 读取beanDefinition中的信息
	// 通过反射创建一个实例并返回
	// 但是Spring不会把最原始的对象放出去，会用一个beanWrapper包装
	// 装饰器模式：
	// 1.保留原来的OOP关系
	// 2.我们需要对它进行扩展，增强（为了以后的AOP打基础）
	@Override
	public Object getBean(String beanName) {
		BeanDefinition beanDefinition = this.beanDefinitionsMap.get(beanName);
		try {
			// 生成通知事件
			BeanPostProcessor beanPostProcessor = new BeanPostProcessor();
			// 得到原生bean
			Object instance = instantionBean(beanDefinition);
			if (instance == null) {
				return null;
			}
			// 在实例初始化以前调用一次
			beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
			// 被warpper包装
			BeanWrapper beanWrapper = new BeanWrapper(instance);
			// 获得AOP
			beanWrapper.setAopConfig(instantionAopConfig(beanDefinition));
			this.beanWrapperMap.put(beanName, beanWrapper);
			// 在实例初始化以后调用一次
			beanPostProcessor.postProcessAfterInitialization(instance, beanName);
			// 返回的是代理类
			return this.beanWrapperMap.get(beanName).getWrappedInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private AopConfig instantionAopConfig(BeanDefinition beanDefinition) throws Exception {
		AopConfig config = new AopConfig();
		String expression = reader.getConfig().getProperty("pointCut");
		String[] before = reader.getConfig().getProperty("aspectBefore").split("\\s");
		String[] after = reader.getConfig().getProperty("aspectAfter").split("\\s");
		String className = beanDefinition.getBeanClassName();
		// 原生类
		Class<?> clazz = Class.forName(className);
		Pattern pattern = Pattern.compile(expression);
		Class<?> aspectClass = Class.forName(before[0]);
		// 在这里得到的方法都是原生的方法
		for (Method m : clazz.getMethods()) {
			// public .*               cn\.zyf\.spring\.demo\.service\.         .*Service\..*\(.*                              \)
			// public java.lang.String cn .zyf .spring .demo .service .impl.ModifyService .add(java.lang.String,java.lang.String)
			Matcher matcher = pattern.matcher(m.toString());
			if (matcher.matches()) {
				// 能满足切面规则的类，添加的AOP配置中
				config.put(m, aspectClass.newInstance(),
						new Method[] { aspectClass.getMethod(before[1]), aspectClass.getMethod(after[1]) });
			}
		}
		return config;
	}

	// 传一个BeanDefinition就返回一个实例Bean
	private Object instantionBean(BeanDefinition beanDefinition) {
		Object instance = null;
		String className = beanDefinition.getBeanClassName();
		try {
			// 因为根据class才能确定一个类是否有实例
			if (this.beanCacheMap.get(className) == null) {
				Class<?> clazz = Class.forName(className);
				instance = clazz.newInstance();
				this.beanCacheMap.put(className, instance);
			}
			return this.beanCacheMap.get(className);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String[] getBeanDefinitionNames() {
		return this.beanDefinitionsMap.keySet().toArray(new String[this.beanDefinitionsMap.size()]);
	}

	public int getBeanDefinitionCount() {
		return this.beanDefinitionsMap.size();
	}

	public Properties getConfig() {
		return this.reader.getConfig();
	}

}
