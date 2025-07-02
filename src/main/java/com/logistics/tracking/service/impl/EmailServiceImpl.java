package com.logistics.tracking.service.impl;

/**
 * 邮件服务实现类
 * 负责处理邮件的发送和接收
 * 发送邮件的流程：
 * 1. 创建邮件数据，包括收件人、主题、模板、模板数据
 * 2. 将邮件数据发送到邮件队列
 * 3. 邮件队列中的邮件数据被消费，发送邮件
 * 4. 发送邮件的实现：
 * 4.1 使用Thymeleaf模板引擎生成邮件内容
 * 4.2 使用JavaMailSender发送邮件
 * 4.3 使用RabbitMQ作为消息队列，实现邮件的异步发送
 * 5. 邮件模板：
 * 5.1 订单状态更新通知
 * 5.2 订单创建成功通知
 * 5.3 订单已签收通知
 * 5.4 自定义邮件
 */
import com.logistics.tracking.config.RabbitMQConfig;
import com.logistics.tracking.model.Order;
import com.logistics.tracking.model.OrderStatus;
import com.logistics.tracking.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final RabbitTemplate rabbitTemplate;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    // 发送订单状态更新通知
    public void sendStatusChangeNotification(Order order, OrderStatus newStatus) {
        if (order == null) {
            throw new IllegalArgumentException("订单对象不能为空");
        }
        if (order.getReceiverEmail() == null || order.getReceiverEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("收件人邮箱地址不能为空");
        }
        
        Map<String, Object> templateData = new HashMap<>();
        // 基础订单信息
        templateData.put("orderId", order.getId());
        templateData.put("status", newStatus);
        
        // 当前物流信息
        templateData.put("location", order.getCurrentLocation());
        templateData.put("estimatedDeliveryTime", order.getEstimatedDeliveryTime());
        
        // 快递员信息（如果已分配）
        if (order.getCourierName() != null) {
            templateData.put("courierName", order.getCourierName());
            templateData.put("courierId", order.getCourierId());
            templateData.put("courierPhone", order.getCourierPhone()); // 添加快递员电话
        }

        // 构建邮件发送参数
        Map<String, Object> emailData = new HashMap<>();
        emailData.put("to", order.getReceiverEmail());
        emailData.put("subject", "订单状态更新通知");
        emailData.put("template", "email/order-status");
        emailData.put("templateData", templateData);

        // 将邮件加入发送队列
        sendToEmailQueue(emailData);
    }

    @Override
    // 发送订单创建通知
    public void sendOrderCreationNotification(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("订单对象不能为空");
        }
        if (order.getSenderEmail() == null || order.getSenderEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("发件人邮箱地址不能为空");
        }

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("orderId", order.getId());
        templateData.put("senderName", order.getSenderName());
        templateData.put("senderAddress", order.getSenderAddress());
        templateData.put("receiverName", order.getReceiverName());
        templateData.put("receiverAddress", order.getReceiverAddress());
        templateData.put("createTime", order.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("to", order.getSenderEmail().trim());
        emailData.put("subject", "订单创建成功通知");
        emailData.put("template", "email/order-creation");
        emailData.put("templateData", templateData);

        sendToEmailQueue(emailData);
    }

    @Override
    // 发送订单签收通知
    public void sendOrderDeliveredNotification(Order order) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("orderId", order.getId());
        templateData.put("deliveryTime", order.getDeliveryTime());
        templateData.put("courierName", order.getCourierName());
        templateData.put("signatureImage", order.getSignatureImage());

        // 发送给发件人
        Map<String, Object> senderEmailData = new HashMap<>();
        senderEmailData.put("to", order.getSenderEmail());
        senderEmailData.put("subject", "订单已送达通知");
        senderEmailData.put("template", "email/order-delivered");
        senderEmailData.put("templateData", templateData);
        sendToEmailQueue(senderEmailData);

        // 发送给收件人
        Map<String, Object> receiverEmailData = new HashMap<>();
        receiverEmailData.put("to", order.getReceiverEmail());
        receiverEmailData.put("subject", "订单已送达通知");
        receiverEmailData.put("template", "email/order-delivered");
        receiverEmailData.put("templateData", templateData);
        sendToEmailQueue(receiverEmailData);
    }

//    @Override
//    // 发送自定义邮件
//    public void sendCustomEmail(String to, String subject, String content) {
//        Map<String, Object> templateData = new HashMap<>();
//        templateData.put("content", content);
//
//        Map<String, Object> emailData = new HashMap<>();
//        emailData.put("to", to);
//        emailData.put("subject", subject);
//        emailData.put("template", "email/custom");
//        emailData.put("templateData", templateData);
//
//        sendToEmailQueue(emailData);
//    }

    private void sendToEmailQueue(Map<String, Object> emailData) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EMAIL_EXCHANGE,
            RabbitMQConfig.EMAIL_ROUTING_KEY,
            emailData
        );
    }

    public void processEmailMessage(Map<String, Object> emailData) {
        try {
            // 验证必要的邮件数据
            if (emailData == null) {
                throw new IllegalArgumentException("邮件数据不能为空");
            }

            String to = (String) emailData.get("to");
            String subject = (String) emailData.get("subject");
            String template = (String) emailData.get("template");
            @SuppressWarnings("unchecked")
            Map<String, Object> templateData = (Map<String, Object>) emailData.get("templateData");

            // 验证必要字段
            if (to == null || to.trim().isEmpty()) {
                throw new IllegalArgumentException("收件人地址不能为空");
            }
            if (subject == null || subject.trim().isEmpty()) {
                throw new IllegalArgumentException("邮件主题不能为空");
            }
            if (template == null || template.trim().isEmpty()) {
                throw new IllegalArgumentException("邮件模板不能为空");
            }
            if (templateData == null) {
                throw new IllegalArgumentException("模板数据不能为空");
            }

            Context context = new Context();
            templateData.forEach(context::setVariable);

            String htmlContent = templateEngine.process(template, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to.trim());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("发送邮件失败: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("处理邮件消息失败: " + e.getMessage(), e);
        }
    }
} 