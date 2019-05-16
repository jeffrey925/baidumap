package com.myapp.ford.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.ford.cache.UserCachePool;
import com.myapp.ford.cache.UserInfo;

@Controller
public class AuthController {
	private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);
	@Autowired
	private RestTemplateBuilder restTemplateBuilder;
	
	@Autowired
	private GithubConfig githubConfig;
	
	
	@RequestMapping("/auth")
	//code来源于Github
	public String auth(@RequestParam String code) throws Exception {
		
		RestTemplate client = restTemplateBuilder.build();
		//获取access_token
		MultiValueMap<String, String> requestEntity = new LinkedMultiValueMap<>();
		requestEntity.add("client_id", githubConfig.getClientId());
        requestEntity.add("client_secret", githubConfig.getClientSecret());
        requestEntity.add("code", code);
        String str = client.postForObject("https://github.com/login/oauth/access_token", requestEntity, String.class);
		String[] arrays = str.split("&");
		String token = null;
		for (String s:arrays) {
			if (s.startsWith("access_token=")) {
				token = s.substring("access_token=".length());
				break;
			}
		}
		//获取用户信息
		String userinfo = client.getForObject("https://api.github.com/user?access_token="+token, String.class);
		ObjectMapper mapper = new ObjectMapper();
		UserInfo user = mapper.readValue(userinfo, UserInfo.class);
        String login = user.getLogin();
    	String userid = user.getUserId();
    	LOG.info("login===="+login);
    	//放入缓存池
    	UserCachePool.putUser(token, user);
    	//将token带入搜索页
        return "redirect:/search.html?token="+token;
	}
	
	
}
