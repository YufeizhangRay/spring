package cn.zyf.spring.framework.beans;

//用于做时间监听的
public class BeanPostProcessor {
	
	public Object postProcessBeforeInitialization(Object bean,String beanName) {
//		System.out.println(beanName+"初始化前");
		return bean;
	}
	
	public Object postProcessAfterInitialization(Object bean,String beanName) {
//		System.out.println(beanName+"初始化前后");
		return bean;
	}

}
