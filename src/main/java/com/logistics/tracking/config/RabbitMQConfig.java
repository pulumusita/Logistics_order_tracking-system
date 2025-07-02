package com.logistics.tracking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.boot.ApplicationRunner;

@Configuration
public class RabbitMQConfig {
    
    // 队列名称
    public static final String ORDER_NOTIFICATION_QUEUE = "order.notification.queue";
    public static final String ORDER_STATUS_QUEUE = "order.status.queue";
    public static final String ORDER_DELIVERY_QUEUE = "order.delivery.queue";
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String EMAIL_DLQ = "email.dlq"; // 死信队列

    // 交换机名称
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String EMAIL_DLX = "email.dlx"; // 死信交换机

    // 路由键
    public static final String NOTIFICATION_ROUTING_KEY = "order.notification";
    public static final String STATUS_ROUTING_KEY = "order.status";
    public static final String DELIVERY_ROUTING_KEY = "order.delivery";
    public static final String EMAIL_ROUTING_KEY = "email.routing.key";
    public static final String EMAIL_DLQ_ROUTING_KEY = "email.dlq.key";

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public ApplicationRunner rabbitInitializer(RabbitAdmin rabbitAdmin) {
        return args -> {
            // 删除现有队列和交换机
            rabbitAdmin.deleteQueue(ORDER_NOTIFICATION_QUEUE);
            rabbitAdmin.deleteQueue(ORDER_STATUS_QUEUE);
            rabbitAdmin.deleteQueue(ORDER_DELIVERY_QUEUE);
            rabbitAdmin.deleteQueue(EMAIL_QUEUE);
            rabbitAdmin.deleteQueue(EMAIL_DLQ);
            
            rabbitAdmin.deleteExchange(ORDER_EXCHANGE);
            rabbitAdmin.deleteExchange(EMAIL_EXCHANGE);
            rabbitAdmin.deleteExchange(EMAIL_DLX);
            
            // 重新声明队列和交换机
            rabbitAdmin.declareQueue(orderNotificationQueue());
            rabbitAdmin.declareQueue(orderStatusQueue());
            rabbitAdmin.declareQueue(orderDeliveryQueue());
            rabbitAdmin.declareQueue(emailQueue());
            rabbitAdmin.declareQueue(deadLetterQueue());
            
            rabbitAdmin.declareExchange(orderExchange());
            rabbitAdmin.declareExchange(emailExchange());
            rabbitAdmin.declareExchange(deadLetterExchange());
            
            // 重新绑定
            rabbitAdmin.declareBinding(bindingNotification(orderNotificationQueue(), orderExchange()));
            rabbitAdmin.declareBinding(bindingStatus(orderStatusQueue(), orderExchange()));
            rabbitAdmin.declareBinding(bindingDelivery(orderDeliveryQueue(), orderExchange()));
            rabbitAdmin.declareBinding(emailBinding());
            rabbitAdmin.declareBinding(deadLetterBinding());
        };
    }

    // 创建队列
    @Bean
    public Queue orderNotificationQueue() {
        return new Queue(ORDER_NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Queue orderStatusQueue() {
        return new Queue(ORDER_STATUS_QUEUE, true);
    }

    @Bean
    public Queue orderDeliveryQueue() {
        return new Queue(ORDER_DELIVERY_QUEUE, true);
    }

    // 创建邮件队列
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", EMAIL_DLX)
                .withArgument("x-dead-letter-routing-key", EMAIL_DLQ_ROUTING_KEY)
                .withArgument("x-message-ttl", 60000) // 消息过期时间：1分钟
                .build();
    }

    // 创建死信队列
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(EMAIL_DLQ).build();
    }

    // 创建交换机
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EMAIL_DLX);
    }

    // 绑定队列和交换机
    @Bean
    public Binding bindingNotification(Queue orderNotificationQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderNotificationQueue)
                .to(orderExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding bindingStatus(Queue orderStatusQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderStatusQueue)
                .to(orderExchange)
                .with(STATUS_ROUTING_KEY);
    }

    @Bean
    public Binding bindingDelivery(Queue orderDeliveryQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderDeliveryQueue)
                .to(orderExchange)
                .with(DELIVERY_ROUTING_KEY);
    }

    // 绑定邮件队列到交换机
    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(emailExchange())
                .with(EMAIL_ROUTING_KEY);
    }

    // 绑定死信队列到死信交换机
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(EMAIL_DLQ_ROUTING_KEY);
    }

    // 配置消息转换器
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        return retryTemplate;
    }

    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, EMAIL_DLX, EMAIL_DLQ_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setRetryTemplate(retryTemplate());
        return rabbitTemplate;
    }
} 