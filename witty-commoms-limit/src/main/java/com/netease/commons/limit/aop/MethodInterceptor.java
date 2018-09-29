package com.netease.commons.limit.aop;

import com.google.common.collect.Lists;
import com.netease.commons.limit.annotation.DistributedRateLimiterMethod;
import com.netease.commons.limit.annotation.RateLimiterMethod;
import com.netease.commons.limit.annotation.RetryExecution;
import com.netease.commons.limit.ratelimiter.Chain;
import com.netease.commons.limit.ratelimiter.GuavaReqRateLimit;
import com.netease.commons.limit.ratelimiter.RedisRateLimit;
import com.netease.commons.limit.ratelimiter.TimeWindowReqRateLimit;
import com.netease.commons.limit.utils.ProxyUtils;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author binbinli
 */
public class MethodInterceptor extends AbstractMethodInterceptor {

    protected static Logger logger = LoggerFactory.getLogger(MethodInterceptor.class);

    private Chain chain;
    private RedisRateLimit redisRateLimit;

    private DefaultParameterNameDiscoverer defaultParameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public MethodInterceptor(String namespace) {
        super(namespace);
        chain = new Chain(Lists.newArrayList(new GuavaReqRateLimit(namespace), new TimeWindowReqRateLimit(namespace)));
    }

    public MethodInterceptor(String namespace, StringRedisTemplate stringRedisTemplate) {
        super(namespace, stringRedisTemplate);
        chain = new Chain(Lists.newArrayList(new GuavaReqRateLimit(namespace), new TimeWindowReqRateLimit(namespace)));
        redisRateLimit = new RedisRateLimit(namespace,stringRedisTemplate);
    }

    public MethodInterceptor(String namespace, Chain chain) {
        super(namespace);
        this.chain = chain;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        Class<?> targetClass = (methodInvocation.getThis() != null ? AopUtils.getTargetClass(methodInvocation.getThis()) : null);
        Method specificMethod = ClassUtils.getMostSpecificMethod(methodInvocation.getMethod(), targetClass);
        final Method userDeclaredMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        StandardEvaluationContext context = new MethodEvaluationContext(
                targetClass, userDeclaredMethod, methodInvocation.getArguments(), defaultParameterNameDiscoverer);
        context.setBeanResolver(new BeanFactoryResolver(getBeanFactory()));

        if (ProxyUtils.hasAnnotation(methodInvocation.getMethod(), RateLimiterMethod.class)) {
            return chain.invoke(methodInvocation, context);
        } else if (ProxyUtils.hasAnnotation(methodInvocation.getMethod(), RetryExecution.class)) {
            //TODO 后期扩展
        } else if (ProxyUtils.hasAnnotation(methodInvocation.getMethod(), DistributedRateLimiterMethod.class)) {
            if (null != getStringRedisTemplate() && null != redisRateLimit) {
               return redisRateLimit.invoke(methodInvocation,context);
            }
        }
        return methodInvocation.proceed();
    }

    static class MethodEvaluationContext extends StandardEvaluationContext {

        private final Method method;

        private final Object[] arguments;

        private final ParameterNameDiscoverer parameterNameDiscoverer;

        private boolean argumentsLoaded = false;


        MethodEvaluationContext(Object rootObject, Method method, Object[] arguments,
                                ParameterNameDiscoverer parameterNameDiscoverer) {

            super(rootObject);
            this.method = method;
            this.arguments = arguments;
            this.parameterNameDiscoverer = parameterNameDiscoverer;
        }


        @Override
        public Object lookupVariable(String name) {
            Object variable = super.lookupVariable(name);
            if (variable != null) {
                return variable;
            }
            if (!this.argumentsLoaded) {
                lazyLoadArguments();
                this.argumentsLoaded = true;
                variable = super.lookupVariable(name);
            }
            return variable;
        }

        /**
         * Load the param information only when needed.
         */
        protected void lazyLoadArguments() {
            // Shortcut if no args need to be loaded
            if (ObjectUtils.isEmpty(this.arguments)) {
                return;
            }

            // Expose indexed variables as well as parameter names (if discoverable)
            String[] paramNames = this.parameterNameDiscoverer.getParameterNames(this.method);
            int paramCount = (paramNames != null ? paramNames.length : this.method.getParameterTypes().length);
            int argsCount = this.arguments.length;

            for (int i = 0; i < paramCount; i++) {
                Object value = null;
                if (argsCount > paramCount && i == paramCount - 1) {
                    // Expose remaining arguments as vararg array for last parameter
                    value = Arrays.copyOfRange(this.arguments, i, argsCount);
                } else if (argsCount > i) {
                    // Actual argument found - otherwise left as null
                    value = this.arguments[i];
                }
                setVariable("a" + i, value);
                setVariable("p" + i, value);
                if (paramNames != null) {
                    setVariable(paramNames[i], value);
                }
            }
        }
    }
}
