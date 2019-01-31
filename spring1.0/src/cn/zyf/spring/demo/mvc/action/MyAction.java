package cn.zyf.spring.demo.mvc.action;

import cn.zyf.spring.annotation.Autowired;
import cn.zyf.spring.annotation.Controller;
import cn.zyf.spring.annotation.RequestMapping;
import cn.zyf.spring.demo.service.IDemoService;

@Controller
public class MyAction {

	@Autowired
	private IDemoService demoService;
	
	@RequestMapping("/index.html")
	public void query() {
		
	}
}
