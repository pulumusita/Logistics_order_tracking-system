package com.logistics.tracking.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.tracking.model.OperationLog;
import com.logistics.tracking.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 敏感操作切面
 * 用于拦截标记了@SensitiveOperation注解的方法，记录操作日志
 */
@Slf4j
@Aspect
@Component
public class SensitiveOperationAspect {

    @Autowired
    private OperationLogService operationLogService;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 环绕通知，拦截所有标记了@SensitiveOperation注解的方法
     */
    @Around("@annotation(com.logistics.tracking.aspect.SensitiveOperation)")
    public Object logSensitiveOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        OperationLog operationLog = new OperationLog();
        
        // 获取当前时间
        operationLog.setOperationTime(LocalDateTime.now());
        
        // 获取目标方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取注解信息
        SensitiveOperation annotation = method.getAnnotation(SensitiveOperation.class);
        operationLog.setOperationType(annotation.type());
        operationLog.setOperationDescription(annotation.value());
        
        // 获取目标ID（通常是第一个参数）
        if (joinPoint.getArgs().length > 0) {
            Object arg = joinPoint.getArgs()[0];
            if (arg instanceof String) {
                operationLog.setTargetId((String) arg);
            }
        }
        
        // 设置目标类型
        operationLog.setTargetType(joinPoint.getTarget().getClass().getSimpleName());
        
        // 获取请求信息
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operationLog.setIp(getClientIp(request));
                
                // 这里可以通过Spring Security获取当前登录用户信息
                // 简化实现，实际应从当前登录用户信息中获取
                operationLog.setOperatorId("system");
                operationLog.setOperatorName("System");
            }
        } catch (Exception e) {
            log.warn("获取请求信息失败", e);
        }
        
        // 尝试记录参数
        try {
            operationLog.setDetails(objectMapper.writeValueAsString(joinPoint.getArgs()));
        } catch (Exception e) {
            log.warn("记录方法参数失败", e);
            operationLog.setDetails("参数序列化失败");
        }
        
        // 执行目标方法
        Object result;
        try {
            result = joinPoint.proceed();
            operationLog.setSuccess(true);
        } catch (Throwable e) {
            operationLog.setSuccess(false);
            operationLog.setDetails(operationLog.getDetails() + "\n异常信息：" + e.getMessage());
            throw e;
        } finally {
            // 保存日志
            try {
                operationLogService.save(operationLog);
                log.debug("记录敏感操作日志成功: {}", operationLog);
            } catch (Exception e) {
                log.error("记录敏感操作日志失败", e);
            }
        }
        
        return result;
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_FORWARDED");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_VIA");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("REMOTE_ADDR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 