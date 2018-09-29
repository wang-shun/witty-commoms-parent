package com.netease.commons.limit.ratelimiter;

import com.google.common.util.concurrent.RateLimiter;
import com.netease.commons.limit.annotation.RateLimiterMethod;
import com.netease.commons.limit.utils.ProxyUtils;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author binbinli
 */
public class GuavaReqRateLimit extends ReqRateLimit {

    private ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<String, RateLimiter>();

    public GuavaReqRateLimit(String name) {
        super(name);
    }

    @Override
    public boolean tryAcquire(MethodInvocation methodInvocation, StandardEvaluationContext context) {
        if (!ProxyUtils.hasAnnotation(methodInvocation.getMethod(), RateLimiterMethod.class)) {
            return true;
        }
        RateLimiterMethod rateLimiterMethod = methodInvocation.getMethod().getAnnotation(RateLimiterMethod.class);
        String key = ProxyUtils.createKey(getNamespace(), methodInvocation);
        RateLimiter result = limiters.get(key);
        if (result == null) {
            RateLimiter value = RateLimiter.create(rateLimiterMethod.qps());
            result = value;
            RateLimiter putByOtherThread = limiters.putIfAbsent(key, value);
            //有其他线程写入了值
            if (putByOtherThread != null) {
                result = putByOtherThread;
            }
        }
        return result == null || result.tryAcquire();
    }


}
