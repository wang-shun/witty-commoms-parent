package com.netease.commons.limit.utils;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * AOP工具类
 *
 * @author libinbin
 */
public class ProxyUtils {

    /**
     * 获取指定方法名的Method对象
     *
     * @param joinPoint
     * @param methodName
     * @return
     */
    public static Method getMethodFromTarget(JoinPoint joinPoint, String methodName) {
        Method method = null;
        if (joinPoint.getSignature() instanceof MethodSignature) {
            method = getDeclaredMethod(joinPoint.getTarget().getClass(), methodName, getParameterTypes(joinPoint));
        }
        return method;
    }

    public static Method getMethodFromTarget(MethodInvocation methodInvocation, String methodName) {
        Method method = getDeclaredMethod(methodInvocation.getThis().getClass(), methodName, methodInvocation.getMethod().getParameterTypes());
        return method;
    }

    public static boolean hasAnnotation(Method method, Annotation annotation) {
        Annotation[] annotations = method.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {  //遍历循环
            if (annotations[i].hashCode() == annotation.hashCode()) //用哈希码判断
                return true;
        }
        return false;
    }

    public static <T extends Annotation> boolean hasAnnotation(Method method, Class<T> annotationClass) {
        T rateLimiterMethod = method.getAnnotation(annotationClass);
        return rateLimiterMethod != null;
    }

    private static Method getDeclaredMethod(Class<?> type, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            method = type.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            Class<?> superclass = type.getSuperclass();
            if (superclass != null) {
                method = getDeclaredMethod(superclass, methodName, parameterTypes);
            }
        }
        return method;
    }

    private static Class[] getParameterTypes(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getParameterTypes();
    }

    public static String createKey(JoinPoint jp) {
        StringBuilder sb = new StringBuilder();
        appendType(sb, getType(jp));
        Signature signature = jp.getSignature();
        if (signature instanceof MethodSignature) {
            MethodSignature ms = (MethodSignature) signature;
            sb.append("#");
            sb.append(ms.getMethod().getName());
            sb.append("(");
            appendTypes(sb, ms.getMethod().getParameterTypes());
            sb.append(")");
        }
        return sb.toString();
    }

    public static String createKey(String namaespace, MethodInvocation methodInvocation) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(namaespace)) {
            sb.append(namaespace);
            sb.append("#");
        }
        appendType(sb, methodInvocation.getThis().getClass());
        sb.append("#");
        sb.append(methodInvocation.getMethod().getName());
        sb.append("(");
        appendTypes(sb, methodInvocation.getMethod().getParameterTypes());
        sb.append(")");
        return sb.toString();
    }

    private static Class<?> getType(JoinPoint jp) {
        if (jp.getSourceLocation() != null) {
            return jp.getSourceLocation().getWithinType();
        } else {
            return jp.getSignature().getDeclaringType();
        }
    }

    private static void appendTypes(StringBuilder sb, Class<?>[] types) {
        for (int size = types.length, i = 0; i < size; i++) {
            appendType(sb, types[i]);
            if (i < size - 1) {
                sb.append(",");
            }
        }
    }

    private static void appendType(StringBuilder sb, Class<?> type) {
        if (type.isArray()) {
            appendType(sb, type.getComponentType());
            sb.append("[]");
        } else {
            sb.append(type.getName());
        }
    }
    /**
     * 被限流了,如果设置了降级方法，则执行降级方法
     *
     * @param methodInvocation
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object demotionMethod(MethodInvocation methodInvocation, String fallBackMethod ) throws InvocationTargetException, IllegalAccessException {

        //被限流了,如果设置了降级方法，则执行降级方法
        if (StringUtils.isNotBlank(fallBackMethod)) {
            Object obj = methodInvocation.getThis();
            Method method = ProxyUtils.getMethodFromTarget(methodInvocation, fallBackMethod);
            if (method != null) {
                Object result = method.invoke(obj, methodInvocation.getArguments());
                return result;
            }
        }
        return null;
    }

}
