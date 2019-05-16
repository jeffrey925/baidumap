package com.myapp.ford.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.myapp.ford.cache.UserCachePool;
import com.myapp.ford.cache.UserInfo;
import com.myapp.ford.service.MapService;

@Controller
public class SearchController {
	
	private static final Logger LOG = LoggerFactory.getLogger(SearchController.class);
	
	@Autowired
	private MapService mapService;
	
	@PostMapping("/dibiao")
	@ResponseBody
	public String search(@RequestParam String token, @RequestParam String dibiao, @RequestParam String station) {
		LOG.info("token="+token+",dibiao="+dibiao);
		UserInfo userInfo = UserCachePool.getUser(token);
		if (userInfo != null) {
			LOG.info("login="+userInfo.getLogin());
			mapService.logSearch(userInfo.getLogin(), dibiao, station);
		} else {
			LOG.warn("用户未经过Github认证授权");
			return "NOT_AUTH";
		}
		return token;
	}
	
	@GetMapping("/report")
	@ResponseBody
	public String report() {
		return mapService.reportSearch();
	}
}
