package com.myapp.ford.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.ford.control.GithubConfig;
import com.myapp.ford.entity.UserSearch;

@Service
@Transactional
public class MapService {
	private static final Logger LOG = LoggerFactory.getLogger(MapService.class);
	
	@Autowired
	private UserSearchRepository userSearchDao;
	
	@Autowired
	RestTemplateBuilder restTemplateBuilder;
	
	@Autowired
	private GithubConfig githubConfig;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	enum ShopType {
		GAS_STATION, FORD_4S;
	}
	
	public void logSearch(String login, String address, String shopQuery) {
		Point circle = findPoint(address);
		ShopInfo shopInfo = findCloset4SShop(shopQuery, circle);
		LOG.info(shopInfo.getAddress());
		UserSearch us = new UserSearch();
		us.setLoginId(login);
		us.setShopName(shopInfo.getShopName());
		us.setShopAddress(shopInfo.getAddress());
		us.setShopType(ShopType.GAS_STATION.name());
		userSearchDao.save(us);
	}
	
	
	
	private Point findPoint(String address)  {
		RestTemplate client = restTemplateBuilder.build();
		try {
			String url = String.format("http://api.map.baidu.com/geocoder/v2/?address=%s&output=json&ak=%s", address, githubConfig.getAk());
			LOG.info(url);
			String geoinfo = client.getForObject(url, String.class);
			LOG.info(geoinfo);
			ObjectMapper mapper = new ObjectMapper();
			Map geo = mapper.readValue(geoinfo, Map.class);
			Map location = (Map) ((Map)geo.get("result")).get("location");
			Point point = new Point();
			point.setLat((double)location.get("lat"));
			point.setLng((double)location.get("lng"));
			return point;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private ShopInfo findCloset4SShop(String shopQuery, Point point) {
		RestTemplate client = restTemplateBuilder.build();
		try {
			String url = String.format("http://api.map.baidu.com/place/v2/search?query=%s&location=%s,%s&radius=5000&output=json&scope=2&ak=%s", 
					shopQuery, point.lat, point.lng, githubConfig.getAk());
			String shoplist = client.getForObject(url, String.class);
			LOG.info(shoplist);
			ObjectMapper mapper = new ObjectMapper();
			Map root = mapper.readValue(shoplist, Map.class);
			List results =(List)root.get("results");
			if (results.size()>0) {
				Map entry = (Map) results.get(0);
				ShopInfo shopInfo = new ShopInfo();
				shopInfo.setAddress((String) entry.get("address"));
				shopInfo.setShopName((String) entry.get("name"));
				shopInfo.setUid((String) entry.get("uid"));
				return shopInfo;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static class Point {
		private double lng;
		private double lat;
		public double getLng() {
			return lng;
		}
		public void setLng(double lng) {
			this.lng = lng;
		}
		public double getLat() {
			return lat;
		}
		public void setLat(double lat) {
			this.lat = lat;
		}
		
	}
	
	static class ShopInfo {
		private String shopName;
		private String address;
		private String uid;
		public String getShopName() {
			return shopName;
		}
		public void setShopName(String shopName) {
			this.shopName = shopName;
		}
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public String getUid() {
			return uid;
		}
		public void setUid(String uid) {
			this.uid = uid;
		}
		
	}

	public String reportSearch() {
		List<Object[]> list = userSearchDao.querySearch();
		ObjectMapper mapper = new ObjectMapper();
		try {
			List result = new ArrayList();
			for (int i=0; i < list.size(); i++) {
				Map<String, Object> map = new HashMap<>();
				Object[] obj = list.get(i);
				map.put("login_id", obj[0]);
				map.put("shop_name", obj[1]);
				map.put("max_search_count", obj[2]);
				result.add(map);
			}
			String  json = mapper.writeValueAsString(result);
			return json;
		} catch (JsonProcessingException e) {
			LOG.error(e.getMessage(), e);
		}
		return "REPORT_ERROR";
	}
}
