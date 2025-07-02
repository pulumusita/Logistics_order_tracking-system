package com.logistics.tracking.aspect;

import com.logistics.tracking.model.OperationLog;
import com.logistics.tracking.model.Order;
import com.logistics.tracking.model.OrderStatus;
import com.logistics.tracking.service.OperationLogService;
import com.logistics.tracking.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 订单服务切面
 * 用于拦截订单相关的敏感操作（如删除订单），记录操作日志
 * 这个切面不需要修改原有代码，直接通过表达式拦截方法
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OrderServiceAspect {

    private final OperationLogService operationLogService;
    private final OrderService orderService;

    /**
     * 拦截删除订单操作
     */
    @Before("execution(* com.logistics.tracking.service.OrderService.deleteOrder(..))")
    public void logBeforeDeleteOrder(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof String) {
                String orderId = (String) args[0];
                
                // 获取订单信息
                Optional<Order> orderOpt = orderService.getOrderById(orderId);
                if (orderOpt.isPresent()) {
                    Order order = orderOpt.get();
                    
                    OperationLog operationLog = new OperationLog();
                    operationLog.setOperationType("DELETE_ORDER");
                    operationLog.setOperationDescription("删除订单");
                    operationLog.setTargetId(orderId);
                    operationLog.setTargetType("Order");
                    operationLog.setOperationTime(LocalDateTime.now());
                    
                    // 设置操作人信息（简化处理，实际应从Session中获取）
                    operationLog.setOperatorId("system");
                    operationLog.setOperatorName("System");
                    
                    // 获取IP地址
                    try {
                        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                        if (attributes != null) {
                            HttpServletRequest request = attributes.getRequest();
                            operationLog.setIp(request.getRemoteAddr());
                        }
                    } catch (Exception e) {
                        log.warn("获取请求信息失败", e);
                    }
                    
                    // 记录订单详细信息
                    StringBuilder details = new StringBuilder();
                    details.append("订单ID: ").append(order.getId())
                           .append(", 发件人: ").append(order.getSenderName())
                           .append(", 收件人: ").append(order.getReceiverName())
                           .append(", 状态: ").append(order.getStatus())
                           .append(", 金额: ").append(order.getAmount());
                    
                    operationLog.setDetails(details.toString());
                    operationLog.setSuccess(true);
                    
                    // 保存日志
                    operationLogService.save(operationLog);
                    log.info("记录订单删除操作日志成功: {}", orderId);
                }
            }
        } catch (Exception e) {
            log.error("记录订单删除操作日志失败", e);
        }
    }
    
    /**
     * 拦截更新订单状态操作
     */
    @AfterReturning(pointcut = "execution(* com.logistics.tracking.service.OrderService.updateOrderStatus(..))", returning = "result")
    public void logAfterUpdateOrderStatus(JoinPoint joinPoint, Object result) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length >= 2 && args[0] instanceof String && args[1] instanceof OrderStatus) {
                String orderId = (String) args[0];
                OrderStatus status = (OrderStatus) args[1];
                
                OperationLog operationLog = new OperationLog();
                operationLog.setOperationType("UPDATE_ORDER_STATUS");
                operationLog.setOperationDescription("更新订单状态");
                operationLog.setTargetId(orderId);
                operationLog.setTargetType("Order");
                operationLog.setOperationTime(LocalDateTime.now());
                
                // 设置操作人信息（简化处理，实际应从Session中获取）
                operationLog.setOperatorId("system");
                operationLog.setOperatorName("System");
                
                // 记录订单状态变更详情
                operationLog.setDetails("订单 " + orderId + " 状态变更为: " + status);
                operationLog.setSuccess(true);
                
                // 保存日志
                operationLogService.save(operationLog);
                log.info("记录订单状态变更操作日志成功: {}", orderId);
            }
        } catch (Exception e) {
            log.error("记录订单状态变更操作日志失败", e);
        }
    }
} 