package com.logistics.tracking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.boot.ApplicationRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisConnectionFactory connectionFactory;

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, Object.class);
        
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    @Bean
    public ApplicationRunner redisInitializer(StringRedisTemplate redisTemplate) {
        return args -> {
            // 清空所有数据
            redisTemplate.getConnectionFactory().getConnection().flushAll();

            // 设置配送员在线状态
            redisTemplate.opsForHash().put("courier:status", "courier:1", "1");
            redisTemplate.opsForHash().put("courier:status", "courier:2", "1");

            // 设置配送员位置信息
            redisTemplate.opsForGeo().add("courier:locations", 
                new Point(116.434062, 39.909652), "courier:1");
            redisTemplate.opsForGeo().add("courier:locations", 
                new Point(121.501654, 31.238068), "courier:2");

            // 设置订单缓存过期时间
            redisTemplate.expire("order:ORDER1", Duration.ofDays(1));
            redisTemplate.expire("order:ORDER2", Duration.ofDays(1));

            // 设置配送员工作区域订单队列
            redisTemplate.opsForList().leftPush("area:beijing:orders", "ORDER1");
            redisTemplate.opsForList().leftPush("area:shanghai:orders", "ORDER2");
        };
    }
} 