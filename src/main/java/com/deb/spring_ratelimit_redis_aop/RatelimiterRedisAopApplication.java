package com.deb.spring_ratelimit_redis_aop;

import java.util.Date;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
@RestController
public class RatelimiterRedisAopApplication {
	
	public static long appStartTime;
	
	public static void main(String[] args) {
		SpringApplication.run(RatelimiterRedisAopApplication.class, args);
		appStartTime = System.currentTimeMillis();
	}

	@GetMapping
	public String home() {
		return "<b>Demo</b> Application is Running since: " + new Date(appStartTime);
	}
	
}
