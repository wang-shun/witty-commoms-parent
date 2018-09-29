package com.netease.commons.limit.ratelimiter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netease.commons.limit.annotation.RateLimiterMethod;
import com.netease.commons.limit.utils.ProxyUtils;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author binbinli
 */
public class TimeWindowReqRateLimit extends ReqRateLimit {

    protected static Logger logger = LoggerFactory.getLogger(TimeWindowReqRateLimit.class);

    /**
     * Guava Cache来存储计数器，过期时间设置为2秒（保证1秒内的计数器是有效的）
     */
    private ConcurrentHashMap<String, LoadingCache<Long, AtomicLong>> counters = new ConcurrentHashMap<String, LoadingCache<Long, AtomicLong>>();
    private SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

    public TimeWindowReqRateLimit(String namespace) {
        super(namespace);
    }

    private String getKey(String uk, Method method, StandardEvaluationContext context) {
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

    @Override
    public boolean tryAcquire(MethodInvocation methodInvocation, StandardEvaluationContext context) throws Exception {

        if (!ProxyUtils.hasAnnotation(methodInvocation.getMethod(), RateLimiterMethod.class)) {
            return true;
        }
        RateLimiterMethod rateLimiterMethod = methodInvocation.getMethod().getAnnotation(RateLimiterMethod.class);

        String key = getKey(rateLimiterMethod.uk(), methodInvocation.getMethod(), context);
        if (StringUtils.isEmpty(key)) {
            return true;
        }
        key = String.format("%s#%s", ProxyUtils.createKey(getNamespace(), methodInvocation), key);
        LoadingCache<Long, AtomicLong> result = counters.get(key);
        if (result == null) {
            //Guava Cache来存储计数器，过期时间设置为2秒（保证1秒内的计数器是有效的）
            LoadingCache<Long, AtomicLong> value = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS)
                    .build(new CacheLoader<Long, AtomicLong>() {
                        @Override
                        public AtomicLong load(Long seconds) throws Exception {
                            return new AtomicLong(0);
                        }
                    });
            result = value;
            LoadingCache<Long, AtomicLong> putByOtherThread = counters.putIfAbsent(key, value);
            //有其他线程写入了值
            if (putByOtherThread != null) {
                result = putByOtherThread;
            }
        }
        //获取当前时间戳,然后取秒数来作为key进行计数统计和限流
        long currentSecond = System.currentTimeMillis() / 1000;
        long qps = rateLimiterMethod.ukQps();
        AtomicLong atomicLong = result.get(currentSecond);
        return null == atomicLong || atomicLong.incrementAndGet() <= qps;

    }

}
