package com.deb.spring_ratelimit_redis_aop.aspect;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.deb.spring_ratelimit_redis_aop.annotation.ERequestIdentifier;
import com.deb.spring_ratelimit_redis_aop.annotation.RateLimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Aspect
@Configuration
public class RateLimitAspect {
	
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	private static final String UNKNOWN = "unknown";

	@Around("@annotation(com.deb.spring_ratelimit_redis_aop.annotation.RateLimit)")
	public Object rateLimit(ProceedingJoinPoint jointPoint) throws Throwable {
		
		MethodSignature methodSignature = (MethodSignature) jointPoint.getSignature();
		Method method = methodSignature.getMethod();
		RateLimit annotation = method.getAnnotation(RateLimit.class);
		
		String baseKey = !StringUtils.hasText(annotation.key()) ? method.getDeclaringClass().getSimpleName().concat(".").concat(method.getName())
				: annotation.key();
		String identifier = getIdentifier(annotation.identifier());
		String redisKey = "rate-limit:" + baseKey + ":" + identifier;
		
		if(!allow(redisKey, annotation.limit(), annotation.timeWindow(), annotation.timeUnit())) {
			handleRateLimitExceeded(HttpStatus.TOO_MANY_REQUESTS, annotation.errorMessage());
			return null;
		}
		
		return jointPoint.proceed();
	}
	
	
	private String getIdentifier(ERequestIdentifier identifierType) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
                RequestContextHolder.getRequestAttributes())).getRequest();
        
        switch (identifierType) {
            case IP:
                return getClientIp(request);
            case USER_ID:
                return getCurrentUserId();
            case API_KEY:
                return getApiKey(request);
            case CUSTOM:
                // Implementation would depend on our specific needs
                return request.getHeader("X-Custom-Identifier");
            default:
                return UNKNOWN;
        }
    }
	
	private String getClientIp(HttpServletRequest request) {
		String xfHeader = request.getHeader("X-Forwarded-For");
	    if (xfHeader != null && !xfHeader.isEmpty()) {
	        return xfHeader.split(",")[0].trim();
	    }
	    return request.getRemoteAddr();
	}
	
	private String getCurrentUserId() {
        /**
         * Get the Authentication object from spring security
         * and check whether user is authenticated or not.
         * If authenticated, get the user Id/name or whatever identifer we use.
         */
        return UNKNOWN;
    }
	
	private String getApiKey(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        return apiKey != null ? apiKey : UNKNOWN;
    }
	
	private boolean allow(String redisKey, int limit, long timeWindow, TimeUnit timeUnit) {
		Long count = redisTemplate.opsForValue().increment(redisKey, 1);
        if (Objects.nonNull(count) && count == 1) {
            redisTemplate.expire(redisKey, timeWindow, timeUnit);
        }
        return count <= limit;
	}
	
	private void handleRateLimitExceeded(HttpStatus status, String message) {
        try {
            HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(
                    RequestContextHolder.getRequestAttributes())).getResponse();
            
            if (response != null) {
                response.setStatus(status.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"" + message + "\"}");
                response.getWriter().flush();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to handle rate limit response", e);
        }
    }
}
