package cn.zyf.spring.framework.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.zyf.spring.framework.beans.BeanDefinition;

public class DefaultListableBeanFactory extends AbstractApplicationContext {
	
	//beanDefinitionMap用来保存配置信息
    protected Map<String,BeanDefinition> beanDefinitionsMap = new ConcurrentHashMap<String,BeanDefinition>();

	protected void onRefresh() {

	}

	@Override
	protected void refreshBeanFactory() {

	}

}
