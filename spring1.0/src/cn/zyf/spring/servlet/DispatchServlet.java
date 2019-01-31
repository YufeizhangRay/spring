package cn.zyf.spring.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.zyf.spring.annotation.Autowired;
import cn.zyf.spring.annotation.Controller;
import cn.zyf.spring.annotation.Service;
import cn.zyf.spring.demo.mvc.action.DemoAction;

public class DispatchServlet extends HttpServlet{
	
	private Properties  contextConfig = new Properties();
	
	private Map<String, Object> beanMap = new ConcurrentHashMap<String, Object>();
	
	private List<String> classNames = new ArrayList<String>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("======== 调用dopost =========");
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		//开始初始化的进程
		
		//定位
		doLoadConfig(config.getInitParameter("contextConfigLocation"));
		
		//加载
		doScanner(contextConfig.getProperty("scanPackage"));
		
		//注册
		doRegistry();
		
		//自动依赖注入
		//在spring中是调用getBean方法来触发依赖注入的
		doAutowired();
		
//        DemoAction action = (DemoAction)beanMap.get("demoAction");
//        action.query(null,null,"zyf");
		
		//如果是SpringMVC会多一个HandlerMapping
		//将@RequestMapping中配置的url和一个Method关联上
		//以便于从浏览器获得用户输入的url以后，能够找到具体执行的方法，通过反射去调用
		initHandlerMapping();
	}

	private void initHandlerMapping() {
	}

	private void doAutowired() {
		if(beanMap.isEmpty()) {
			return;
		}
		for(Map.Entry<String,Object> entry : beanMap.entrySet()) {
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for(Field field : fields) {
				if(!field.isAnnotationPresent(Autowired.class)) {
					continue;
				}else {
					Autowired autowired = field.getAnnotation(Autowired.class);
					String fieldName = autowired.value().trim();
					if("".equals(fieldName)) {
						fieldName = field.getType().getName();
					}
					field.setAccessible(true);
					try {
						field.set(entry.getValue(), beanMap.get(fieldName));
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void doRegistry() {
		if(classNames.isEmpty()) {
			return;
		}
		try {
			for(String className : classNames) {
				//反射出class
				Class<?> clazz = Class.forName(className);
				//在spring中是用多个子方法来处理的
				if(clazz.isAnnotationPresent(Controller.class)) {
					String beanName = lowerFirstCase(clazz.getSimpleName());
					//Spring在这个阶段是不会直接put instance
					//而是put BeanDefinition
					beanMap.put(beanName, clazz.newInstance());
				}else if(clazz.isAnnotationPresent(Service.class)) {
					Service service = clazz.getAnnotation(Service.class);
					//默认用类名首字母注入
					//如果自己定义了beanName，则优先使用自己定义的beanName
					//如果是一个接口，则使用接口的类型去自动的注入
					//在spring中同样会调用不同的方法 autowireByName autowireByType
					String beanName = service.value();
					if("".equals(beanName.trim())) {
						beanName = lowerFirstCase(clazz.getSimpleName());
					}
					Object instance = clazz.newInstance();
					beanMap.put(beanName, instance);
					
					Class<?>[] interfaces = clazz.getInterfaces();
					for(Class<?> i :interfaces) {
						beanMap.put(i.getName(), instance);
					}
				}else {
					continue;
				}
			}
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	private void doScanner(String packageName) {
		URL url = this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.", "/"));
		File classDir = new File(url.getFile());
		for(File file : classDir.listFiles()) {
			if(file.isDirectory()) {
				doScanner(packageName+"."+file.getName());
			}else {
				classNames.add(packageName+"."+file.getName().replace(".class", ""));
			}
		}
	}

	private void doLoadConfig(String location) {
		//在Spring中是通过Reader去查找和定位的
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(location.replace("classpath:", ""));
		try {
			contextConfig.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(is!=null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String lowerFirstCase(String str){
        char [] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
	
}
