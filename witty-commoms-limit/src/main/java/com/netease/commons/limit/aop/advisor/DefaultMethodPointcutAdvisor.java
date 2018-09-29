package com.netease.commons.limit.aop.advisor;

import com.netease.commons.limit.annotation.DistributedRateLimiterMethod;
import com.netease.commons.limit.annotation.RateLimiterMethod;
import com.netease.commons.limit.aop.MethodInterceptor;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author binbinli
 */
public class DefaultMethodPointcutAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {
    private Advice advice;
    private Pointcut pointcut;
    private StringRedisTemplate stringRedisTemplate;
    private String namespace;

    public DefaultMethodPointcutAdvisor(String namespace) {
        this.namespace = namespace;
        Set<Class<? extends Annotation>> annotationTypes = new HashSet<>();
        annotationTypes.add(RateLimiterMethod.class);
        annotationTypes.add(DistributedRateLimiterMethod.class);
        this.pointcut = buildPointcut(annotationTypes);
        buildAdvice();
    }

    public DefaultMethodPointcutAdvisor(String namespace, StringRedisTemplate stringRedisTemplate) {
        this.namespace = namespace;
        Set<Class<? extends Annotation>> annotationTypes = new HashSet<>();
        annotationTypes.add(RateLimiterMethod.class);
        annotationTypes.add(DistributedRateLimiterMethod.class);
        this.pointcut = buildPointcut(annotationTypes);
        buildAdvice(stringRedisTemplate);
    }

    protected void buildAdvice() {
        if (this.advice == null) {
            this.advice = new MethodInterceptor(namespace);
            ((MethodInterceptor) this.advice).setOrder(getOrder());
        } else if (this.advice instanceof MethodInterceptor) {
            ((MethodInterceptor) this.advice).setNamespace(namespace);
            ((MethodInterceptor) this.advice).setOrder(getOrder());
        }
    }

    protected Pointcut buildPointcut(Set<Class<? extends Annotation>> annotationTypes) {
        ComposablePointcut result = null;
        for (Class<? extends Annotation> annotationType : annotationTypes) {
            Pointcut cpc = new AnnotationMatchingPointcut(annotationType, true);
            Pointcut mpc = AnnotationMatchingPointcut.forMethodAnnotation(annotationType);
            if (result == null) {
                result = new ComposablePointcut(cpc);
            } else {
                result.union(cpc);
            }
            result = result.union(mpc);
        }
        return result;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
        }
    }
    protected void buildAdvice(StringRedisTemplate stringRedisTemplate) {
        if (this.advice == null) {
            this.advice = new MethodInterceptor(namespace,stringRedisTemplate);
            ((MethodInterceptor) this.advice).setOrder(getOrder());
        } else if (this.advice instanceof MethodInterceptor) {
            ((MethodInterceptor) this.advice).setNamespace(namespace);
            ((MethodInterceptor) this.advice).setOrder(getOrder());
        }
    }
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
