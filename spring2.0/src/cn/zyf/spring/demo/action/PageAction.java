package cn.zyf.spring.demo.action;

import java.util.HashMap;
import java.util.Map;

import cn.zyf.spring.demo.service.IQueryService;
import cn.zyf.spring.framework.annotation.Autowired;
import cn.zyf.spring.framework.annotation.Controller;
import cn.zyf.spring.framework.annotation.RequestMapping;
import cn.zyf.spring.framework.annotation.RequestParam;
import cn.zyf.spring.framework.webmvc.ModelAndView;

@Controller
@RequestMapping("/")
public class PageAction {

	@Autowired 
	IQueryService queryService;
	
	@RequestMapping("/first.html")
	public ModelAndView query(@RequestParam("name") String name){
		String result = queryService.query(name);
		Map<String,Object> model = new HashMap<String,Object>();
		model.put("name", name);
		model.put("data", result);
		model.put("token", "123456");
		return new ModelAndView("first.html",model);
	}
	
}

