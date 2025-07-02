package com.logistics.tracking.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 敏感操作注解，用于标记需要记录日志的敏感操作方法
 * 如订单删除、修改等操作
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SensitiveOperation {
    /**
     * 操作描述
     */
    String value() default "";
    
    /**
     * 操作类型
     */
    String type() default "";
} 