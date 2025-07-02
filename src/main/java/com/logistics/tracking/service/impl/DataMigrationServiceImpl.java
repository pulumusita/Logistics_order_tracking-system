package com.logistics.tracking.service.impl;

import com.logistics.tracking.model.Order;
import com.logistics.tracking.repository.OrderRepository;
import com.logistics.tracking.service.MapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 数据迁移服务实现类，用于批量更新历史数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataMigrationServiceImpl {
    
    private final OrderRepository orderRepository;
    private final MapService mapService;
    private final MongoTemplate mongoTemplate;
    
    /**
     * 更新所有没有配送距离的订单
     * 
     * @return 更新的订单数量
     */
    @Transactional
    public int updateOrdersWithoutDistance() {
        log.info("开始更新缺少配送距离的历史订单");
        
        // 查找所有没有配送距离的订单
        Query query = new Query();
        query.addCriteria(Criteria.where("deliveryDistance").isNull());
        List<Order> orders = mongoTemplate.find(query, Order.class);
        
        log.info("找到 {} 个缺少配送距离的订单", orders.size());
        int updatedCount = 0;
        
        for (Order order : orders) {
            try {
                // 使用直线距离计算
                double distance = mapService.calculateDistance(
                    order.getSenderAddress(), 
                    order.getReceiverAddress()
                );
                order.setDeliveryDistance(distance);
                
                // 尝试获取更精确的路线规划距离
                try {
                    Map<String, Object> routeInfo = mapService.getDeliveryRoute(order);
                    if (routeInfo != null && routeInfo.containsKey("distance")) {
                        double routeDistance = Double.parseDouble(String.valueOf(routeInfo.get("distance")));
                        order.setDeliveryDistance(routeDistance);
                        log.debug("订单 {}: 通过路线规划API获取更精确的配送距离: {}米", 
                            order.getId(), routeDistance);
                    }
                } catch (Exception e) {
                    log.warn("订单 {}: 路线规划请求失败，使用直线距离: {}米", 
                        order.getId(), distance, e);
                }
                
                orderRepository.save(order);
                updatedCount++;
                
                // 每处理100条记录输出一次日志
                if (updatedCount % 100 == 0) {
                    log.info("已更新 {} 个订单的配送距离", updatedCount);
                }
                
                // 每处理100条暂停一下，避免API限流
                if (updatedCount % 100 == 0) {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                log.error("更新订单 {} 配送距离失败", order.getId(), e);
            }
        }
        
        log.info("订单配送距离更新完成，共更新 {} 个订单", updatedCount);
        return updatedCount;
    }
} 