package com.netease.commons.limit.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author libinbin
 * @date 2018.08.30
 */
public abstract class AbstractMethodInterceptor implements MethodInterceptor, Ordered, BeanFactoryAware {
    private int order = Ordered.LOWEST_PRECEDENCE;
    private BeanFactory beanFactory;
    private String namespace;
    private StringRedisTemplate stringRedisTemplate;


    public AbstractMethodInterceptor(String namespace, StringRedisTemplate stringRedisTemplate) {
        this.namespace = namespace;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public AbstractMethodInterceptor(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public StringRedisTemplate getStringRedisTemplate() {
        return stringRedisTemplate;
    }

    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
}
