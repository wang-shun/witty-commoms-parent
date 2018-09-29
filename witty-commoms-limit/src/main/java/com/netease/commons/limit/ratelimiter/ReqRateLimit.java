package com.netease.commons.limit.ratelimiter;


import org.aopalliance.intercept.MethodInvocation;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @author binbinli
 */
public abstract class ReqRateLimit {

    private String namespace;

    public ReqRateLimit(String namespace) {
        this.namespace = namespace;
    }

    protected abstract boolean tryAcquire(MethodInvocation methodInvocation, StandardEvaluationContext context) throws Exception;


    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }


}
