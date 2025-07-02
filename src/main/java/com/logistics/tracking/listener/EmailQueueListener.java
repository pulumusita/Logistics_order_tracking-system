package com.logistics.tracking.listener;

import com.logistics.tracking.config.RabbitMQConfig;
import com.logistics.tracking.service.impl.EmailServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailQueueListener {

    private final EmailServiceImpl emailService;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void processEmailMessage(Map<String, Object> emailData) {
        try {
            log.debug("收到邮件消息: {}", emailData);
            if (emailData == null) {
                log.error("邮件数据为空");
                throw new AmqpRejectAndDontRequeueException("邮件数据为空");
            }
            
            String to = (String) emailData.get("to");
            if (to == null || to.trim().isEmpty()) {
                log.error("收件人地址为空");
                throw new AmqpRejectAndDontRequeueException("收件人地址为空");
            }
            
            emailService.processEmailMessage(emailData);
            log.debug("邮件发送成功: {}", to);
        } catch (Exception e) {
            log.error("处理邮件消息失败: {}", e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException("处理邮件消息失败", e);
        }
    }
} 