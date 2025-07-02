package com.logistics.tracking.service.impl;

import com.logistics.tracking.model.Order;
import com.logistics.tracking.model.OrderStatus;
import com.logistics.tracking.repository.OrderRepository;
import com.logistics.tracking.service.StatisticsService;
import com.logistics.tracking.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final OrderRepository orderRepository;
    private final MongoTemplate mongoTemplate;
    private final MapService mapService;

    @Override
    public Map<String, Object> getStatisticsSummary(String startDate, String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).plusDays(1).atStartOfDay();

        // 查询时间范围内的所有订单
        List<Order> orders = orderRepository.findByCreateTimeBetween(start, end);
        
        // 查询已完成的订单
        List<Order> completedOrders = orderRepository.findByCreateTimeBetweenAndStatus(
            start, end, OrderStatus.DELIVERED);

        // 原步骤：计算总配送距离
        // double totalDistance = orders.stream()
        //     .mapToDouble(order -> {
        //         try {
        //             Map<String, Object> routeInfo = mapService.getDeliveryRoute(order);
        //             return Double.parseDouble(String.valueOf(routeInfo.get("distance")));
        //         } catch (Exception e) {
        //             return 0.0;
        //         }
        //     })
        //     .sum();

        // 直接使用订单中存储的配送距离，避免调用地图API
        double totalDistance = orders.stream()
        .mapToDouble(order -> order.getDeliveryDistance() != null ? order.getDeliveryDistance() : 0.0)
        .sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOrders", orders.size());
        summary.put("completedOrders", completedOrders.size());
        summary.put("totalDistance", totalDistance);

        return summary;
    }

    @Override
    public List<Map<String, Object>> getCourierRankings(String startDate, String endDate, int limit) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).plusDays(1).atStartOfDay();

        // 创建查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("createTime").gte(start).lt(end));
        
        // 获取所有订单并统计
        List<Order> orders = mongoTemplate.find(query, Order.class);
        Map<String, List<Order>> ordersByCourier = orders.stream()
            .filter(order -> order.getCourierId() != null)
            .collect(Collectors.groupingBy(Order::getCourierId));
        
        // 计算每个快递员的统计数据
        List<Map<String, Object>> rankings = new ArrayList<>();
        ordersByCourier.forEach((courierId, courierOrders) -> {
            Map<String, Object> courierStats = new HashMap<>();
            Order firstOrder = courierOrders.get(0);
            
            // 基本信息
            courierStats.put("courierId", courierId);
            courierStats.put("courierName", firstOrder.getCourierName());
            courierStats.put("deliveries", courierOrders.size());
            courierStats.put("totalDistance", firstOrder.getDeliveryDistance()); // 使用deliveryDistance字段
            courierStats.put("avgRating", firstOrder.getRating()); // 使用rating字段
            
            rankings.add(courierStats);
        });
        
        // 按配送单数排序
        rankings.sort((a, b) -> {
            Integer countA = (Integer) a.get("deliveries");
            Integer countB = (Integer) b.get("deliveries");
            return countB.compareTo(countA);
        });
        
        // 返回前limit个结果
        return rankings.subList(0, Math.min(limit, rankings.size()));
    }
} 