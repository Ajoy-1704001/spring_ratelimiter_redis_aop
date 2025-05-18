package com.deb.spring_ratelimit_redis_aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
	int limit() default 10;
	
	int timeWindow() default 60;
	
	TimeUnit timeUnit() default TimeUnit.SECONDS;
	
	/**
	 * Key prefix for redis
	 * @return
	 */
	String key() default "";
	
	ERequestIdentifier identifier() default ERequestIdentifier.IP;
	
	String errorMessage() default "Rate limit exceeded. Try again later.";
	
}
