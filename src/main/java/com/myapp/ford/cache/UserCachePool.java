package com.myapp.ford.cache;

import java.util.HashMap;
import java.util.Map;


/**
 * 用户缓冲池，内存对象模拟
 * @author Jeffrey
 *
 */
public class UserCachePool {
	//用户登录信息缓存在这里,根据token查找 
	private static Map<String, UserInfo> userCache = new HashMap<>(); 
	
	public static void putUser(String token, UserInfo ui) {
		userCache.put(token, ui);
	}
	
	public static UserInfo getUser(String token) {
		return userCache.get(token);
	}
	
}
