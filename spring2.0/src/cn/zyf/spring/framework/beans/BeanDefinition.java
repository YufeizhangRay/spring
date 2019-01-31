package cn.zyf.spring.framework.beans;

//用来存储配置文件中的信息，相当于内存中的配置
public class BeanDefinition {

	private String beanClassName;
	private String factoryBeanName;
	private boolean lazyInit = false;

	public String getBeanClassName() {
		return beanClassName;
	}

	public void setBeanClassName(String beanClassName) {
		this.beanClassName = beanClassName;
	}

	public String getFactoryBeanName() {
		return factoryBeanName;
	}

	public void setFactoryBeanName(String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}

	public boolean isLazyInit() {
		return lazyInit;
	}

	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

}
