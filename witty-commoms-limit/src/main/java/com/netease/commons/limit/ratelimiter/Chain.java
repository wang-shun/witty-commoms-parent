package com.netease.commons.limit.ratelimiter;


import com.netease.commons.limit.annotation.RateLimiterMethod;
import com.netease.commons.limit.utils.ProxyUtils;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;

/**
 * @author binbinli
 */
public class Chain {

    private List<ReqRateLimit> reqRateLimits;

    public Chain(List<ReqRateLimit> reqRateLimits) {
        this.reqRateLimits = reqRateLimits;
    }


    public Object invoke(MethodInvocation methodInvocation, StandardEvaluationContext context) throws Throwable {
        for (ReqRateLimit reqRateLimit : reqRateLimits) {
            if (!reqRateLimit.tryAcquire(methodInvocation, context)) {
                return ProxyUtils.demotionMethod(methodInvocation, methodInvocation.getMethod().getAnnotation(RateLimiterMethod.class).fallBackMethod());
            }
        }
        return methodInvocation.proceed();
    }


}
