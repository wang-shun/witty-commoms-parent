package com.netease.commons.limit.ratelimiter;

import com.netease.commons.limit.annotation.DistributedRateLimiterMethod;
import com.netease.commons.limit.utils.ProxyUtils;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Collections;

/**
 * @author binbinli
 */
public class RedisRateLimit extends ReqRateLimit {
    protected static Logger logger = LoggerFactory.getLogger(RedisRateLimit.class);

    private StringRedisTemplate stringRedisTemplate;
    DefaultRedisScript<Number> redisluaScript;
    private SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

    public RedisRateLimit(String namespace, StringRedisTemplate stringRedisTemplate) {
        super(namespace);
        this.stringRedisTemplate = stringRedisTemplate;
        redisluaScript = new DefaultRedisScript<Number>();
        redisluaScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("rateLimit.lua")));
        redisluaScript.setResultType(Number.class);
    }

    public Object invoke(MethodInvocation methodInvocation, StandardEvaluationContext context) throws Throwable {
        if (tryAcquire(methodInvocation, context)) {
            return methodInvocation.proceed();
        }
        return ProxyUtils.demotionMethod(methodInvocation, methodInvocation.getMethod().getAnnotation(DistributedRateLimiterMethod.class).fallBackMethod());
    }

    @Override
    protected boolean tryAcquire(MethodInvocation methodInvocation, StandardEvaluationContext context) throws Exception {

        if (!ProxyUtils.hasAnnotation(methodInvocation.getMethod(), DistributedRateLimiterMethod.class)) {
            return true;
        }

        try {
            DistributedRateLimiterMethod distributedRateLimiterMethod = methodInvocation.getMethod().getAnnotation(DistributedRateLimiterMethod.class);

            String key = getKey(distributedRateLimiterMethod.uk(), context);
            if (StringUtils.isEmpty(key)) {
                return true;
            }
            key = String.format("%s#%s", ProxyUtils.createKey(getNamespace(), methodInvocation), key);
            Number number = stringRedisTemplate.execute(redisluaScript, Collections.singletonList(key), String.valueOf(10), String.valueOf(1));
            if (number != null && number.intValue() > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.warn("Redis throttling failed,errMsg:" + e.getMessage());
        }
        return true;
    }

    private String getKey(String uk, StandardEvaluationContext context) {
        String uid = null;
        if (!org.springframework.util.StringUtils.isEmpty(uk)) {
            // parseExpression是否需要缓存今后需要考察下
            try {
                uid = String.valueOf(spelExpressionParser.parseExpression(uk).getValue(context));
            } catch (Exception e) {
                logger.error("spEL error:" + uk, e);
            }
        }
        return uid;
    }
}
