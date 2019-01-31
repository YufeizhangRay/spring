package cn.zyf.spring.framework.context.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import cn.zyf.spring.framework.beans.BeanDefinition;

//用来对配置文件进行查找、读取、解析
public class BeanDefinitionReader {

	private Properties config = new Properties();

	private List<String> registryBeanClasses = new ArrayList<String>();
	
	//在配置文件中用来获取自动扫描的包名的key
	private final String SCAN_PACKAGE = "scanPackage";

	public BeanDefinitionReader(String... locations) {
		// 在Spring中是通过Reader去查找和定位的
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));
		try {
			config.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		doScanner(config.getProperty(SCAN_PACKAGE));
	}

	public List<String> loadBeanDefinitions() {
		return this.registryBeanClasses;
	}

	//每注册一个className就返回一个BeanDefinition，我们自己包装
	//只是为了对配置信息进行一个包装
	public BeanDefinition registerBean(String className) {
		if(this.registryBeanClasses.contains(className)) {
			BeanDefinition beanDefinition = new BeanDefinition();
			beanDefinition.setBeanClassName(className);
			beanDefinition.setFactoryBeanName(lowerFirstCase(className.substring(className.lastIndexOf(".")+1)));
			return beanDefinition;
		}
		return null;
	}

	//递归扫描所有相关联的class，并保存到一个list中
	private void doScanner(String packageName) {
		URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
		File classDir = new File(url.getFile());
		for (File file : classDir.listFiles()) {
			if (file.isDirectory()) {
				doScanner(packageName + "." + file.getName());
			} else {
				registryBeanClasses.add(packageName + "." + file.getName().replace(".class", ""));
			}
		}
	}

	public Properties getConfig() {
		return this.config;
	}
	
	private String lowerFirstCase(String str){
        char [] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
