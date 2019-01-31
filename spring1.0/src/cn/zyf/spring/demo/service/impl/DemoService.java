package cn.zyf.spring.demo.service.impl;

import cn.zyf.spring.annotation.Service;
import cn.zyf.spring.demo.service.IDemoService;

@Service
public class DemoService implements IDemoService {

	@Override
	public String get(String name) {
		
		return "My name is "+name;
	}

}
