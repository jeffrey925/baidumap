package com.myapp.ford.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.myapp.ford.entity.UserSearch;

public interface UserSearchRepository  extends JpaRepository<UserSearch, Long>{

	@Query(value="select v_searcha.login_id, shop_name, max_cnt " + 
			"from (select login_id, max(cnt) as max_cnt from (select login_id, shop_name, count(shop_name) as cnt from user_search group by login_id, shop_name)  group by login_id) v_searcha  " + 
			"left join " + 
			"(select login_id, shop_name, count(shop_name) as cnt from user_search group by login_id, shop_name) v_search " + 
			"on v_searcha.login_id=v_search.login_id and v_searcha.max_cnt=v_search.cnt ",nativeQuery=true)
	public List<Object[]> querySearch();
}
