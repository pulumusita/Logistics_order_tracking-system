package com.logistics.tracking.service;

import com.logistics.tracking.model.Order;
import com.logistics.tracking.model.OrderStatus;
import java.util.Map;

public interface EmailService {
    // 发送订单状态变更通知
    void sendStatusChangeNotification(Order order, OrderStatus newStatus);
    
    // 发送订单创建通知
    void sendOrderCreationNotification(Order order);
    
    // 发送订单签收通知
    void sendOrderDeliveredNotification(Order order);
    
    // 发送自定义邮件
//    void sendCustomEmail(String to, String subject, String content);

    void processEmailMessage(Map<String, Object> emailData);
} 