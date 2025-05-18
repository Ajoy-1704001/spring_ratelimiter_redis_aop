package com.deb.spring_ratelimit_redis_aop;

import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deb.spring_ratelimit_redis_aop.annotation.RateLimit;


@RestController
public class DemoController {
	
	@RateLimit(limit = 10)
	@GetMapping("/monthly-report")
	public String getMonthlyReportData() {
		return "Here is your monthly report";
	}
	
	@RateLimit(limit = 5, timeWindow = 2, timeUnit = TimeUnit.MINUTES, errorMessage = "We have received too many requests. Please try again after some time.")
	@GetMapping("/yearly-report")
	public String getYearlyReportData() {
		return "Here is your yearly report";
	}
	
	
}
