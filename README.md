# 🔐 Rate Limiting with Spring AOP and Redis

A lightweight and extensible implementation of rate limiting in Spring Boot using:
- Custom `@RateLimit` annotation
- Spring AOP for aspect-driven interception
- Redis as a fast, in-memory store to track request counts

This is a great alternative to libraries like `Resilience4j` when you want full control over logic or lightweight custom behavior.(Resilience4j has lots of features, I have just covered the rate limiting part)

---

## Features

- Declarative `@RateLimit` annotation on controller or service methods
- Limit requests based on client IP or custom identifier (e.g., user ID)
- Backed by Redis for high-performance
- Supports fixed time windows (in seconds, minutes, etc.)
- Customizable error message

---

## Usage

### Add the Annotation

```java
@RateLimit(limit = 5, timeWindow = 2, timeUnit = TimeUnit.MINUTES)
```

## Example
```java
@RateLimit(limit = 10)
@GetMapping("/monthly-report")
public String getMonthlyReport() {
    return "Here is your monthly report";
}

@RateLimit(limit = 5, timeWindow = 2, timeUnit = TimeUnit.MINUTES, errorMessage = "Too many requests. Try again later.")
@GetMapping("/yearly-report")
public String getYearlyReport() {
    return "Here is your yearly report";
}
```

## How it Works
- AOP intercepts methods annotated with @RateLimit
- Redis key is generated as: rate-limit:<method-name>:<IP or ID>
- Request count is incremented per key
- TTL is set for the time window using redisTemplate.expire(...)
- If count exceeds the limit, a RuntimeException is thrown

  
