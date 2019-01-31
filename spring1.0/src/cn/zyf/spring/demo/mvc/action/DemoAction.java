package cn.zyf.spring.demo.mvc.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.zyf.spring.annotation.Autowired;
import cn.zyf.spring.annotation.Controller;
import cn.zyf.spring.annotation.RequestMapping;
import cn.zyf.spring.annotation.RequestParam;
import cn.zyf.spring.demo.service.IDemoService;

@Controller
@RequestMapping("/demo")
public class DemoAction {

	@Autowired
	private IDemoService demoService;
	
	@RequestMapping("/query.json")
	public void query(HttpServletRequest req,HttpServletResponse resp,
			   @RequestParam("name") String name){
			String result = demoService.get(name);
			System.out.println(result);
			try {
				resp.getWriter().write(result);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@RequestMapping("/edit.json")
		public void edit(HttpServletRequest req,HttpServletResponse resp,Integer id){

		}

}
