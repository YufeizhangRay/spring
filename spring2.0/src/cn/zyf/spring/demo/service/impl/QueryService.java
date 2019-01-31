package cn.zyf.spring.demo.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.zyf.spring.demo.service.IQueryService;
import cn.zyf.spring.framework.annotation.Service;

@Service
public class QueryService implements IQueryService{

	/**
	 * 查询
	 */
	public String query(String name) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(new Date());
		String json = "{name:\"" + name + "\",time:\"" + time + "\"}";
		return json;
	}

}
