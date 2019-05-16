package com.myapp.ford.control;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class HelloWorldController {

	
	
	@RequestMapping("/say.html")
	@ResponseBody
	public String say() {
		return "hello world";
	}

	

}
