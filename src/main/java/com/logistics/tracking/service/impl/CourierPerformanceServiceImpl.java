package com.logistics.tracking.service.impl;

import com.logistics.tracking.model.Courier;
import com.logistics.tracking.model.CourierRating;
import com.logistics.tracking.model.Order;
import com.logistics.tracking.model.OrderStatus;
import com.logistics.tracking.repository.CourierRepository;
import com.logistics.tracking.repository.CourierRatingRepository;
import com.logistics.tracking.repository.OrderRepository;
import com.logistics.tracking.service.CourierPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CourierPerformanceServiceImpl implements CourierPerformanceService {

    private final CourierRepository courierRepository;
    private final CourierRatingRepository ratingRepository;
    private final OrderRepository orderRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public Courier createCourier(Courier courier) {
        courier.setActive(true);
        courier.setOnline(false);
        courier.setAverageRating(5.0);
        courier.setCompletedOrders(0);
        courier.setComplaintsCount(0);
        return courierRepository.save(courier);
    }

    @Override
    public Courier updateCourier(Courier courier) {
        Courier existingCourier = getCourierById(courier.getId());
        if (existingCourier == null) {
            throw new RuntimeException("快递员不存在");
        }
        return courierRepository.save(courier);
    }

    @Override
    public void deleteCourier(String id) {
        Courier courier = getCourierById(id);
        if (courier != null) {
            courier.setActive(false);
            courierRepository.save(courier);
        }
    }

    @Override
    public Courier getCourierById(String id) {
        return courierRepository.findById(id).orElse(null);
    }

    @Override
    public List<Courier> getAllActiveCouriers() {
        return courierRepository.findByActiveTrue();
    }

    @Override
    public CourierRating addRating(CourierRating rating) {
        rating.setCreateTime(LocalDateTime.now());
        CourierRating savedRating = ratingRepository.save(rating);
        
        // 更新快递员统计数据
        Courier courier = getCourierById(rating.getCourierId());
        if (courier != null) {
            List<CourierRating> ratings = getCourierRatings(rating.getCourierId());
            double averageRating = ratings.stream()
                    .mapToInt(CourierRating::getRating)
                    .average()
                    .orElse(5.0);
            
            courier.setAverageRating(averageRating);
            if (rating.isComplaint()) {
                courier.setComplaintsCount(courier.getComplaintsCount() + 1);
            }
            courierRepository.save(courier);
        }
        
        return savedRating;
    }

    @Override
    public List<CourierRating> getCourierRatings(String courierId) {
        return ratingRepository.findByCourierIdOrderByCreateTimeDesc(courierId);
    }

    @Override
    public Map<String, Object> getDailyStats(String courierId) {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        List<CourierRating> ratings = ratingRepository.findByCourierIdAndCreateTimeAfter(courierId, today);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("deliveryCount", ratings.size());
        stats.put("complaintCount", ratings.stream().filter(CourierRating::isComplaint).count());
        stats.put("averageRating", ratings.stream().mapToInt(CourierRating::getRating).average().orElse(0.0));
        
        return stats;
    }

    @Override
    public Map<String, Object> getMonthlyStats(String courierId) {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<CourierRating> ratings = ratingRepository.findByCourierIdAndCreateTimeAfter(courierId, monthStart);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("deliveryCount", ratings.size());
        stats.put("complaintCount", ratings.stream().filter(CourierRating::isComplaint).count());
        stats.put("averageRating", ratings.stream().mapToInt(CourierRating::getRating).average().orElse(0.0));
        
        return stats;
    }

    @Override
    public Map<String, Object> getPerformanceReport(String courierId, LocalDateTime startTime, LocalDateTime endTime) {
        List<CourierRating> ratings = ratingRepository.findByCourierIdAndCreateTimeBetween(courierId, startTime, endTime);
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalDeliveries", ratings.size());
        report.put("complaints", ratings.stream().filter(CourierRating::isComplaint).count());
        report.put("averageRating", ratings.stream().mapToInt(CourierRating::getRating).average().orElse(0.0));
        
        // 按评分分布统计
        Map<Integer, Long> ratingDistribution = ratings.stream()
                .collect(Collectors.groupingBy(CourierRating::getRating, Collectors.counting()));
        report.put("ratingDistribution", ratingDistribution);
        
        return report;
    }

    @Override
    // 更新骑手在线状态
    public void updateOnlineStatus(String courierId, boolean online) {
        // 根据骑手id获取骑手对象
        Courier courier = getCourierById(courierId);
        // 如果骑手对象不为空
        if (courier != null) {
            // 设置骑手在线状态
            courier.setOnline(online);
            // 保存骑手对象
            courierRepository.save(courier);
        }
    }

    @Override
    public List<Courier> getOnlineCouriers() {
        return courierRepository.findByOnlineTrue();
    }

    @Override
    public List<Courier> getCouriersByWorkArea(String area) {
        return courierRepository.findByWorkAreaAndActiveTrue(area);
    }

    @Override
    public void updateWorkArea(String courierId, String workArea) {
        Courier courier = getCourierById(courierId);
        if (courier != null) {
            courier.setWorkArea(workArea);
            courierRepository.save(courier);
        }
    }

    @Override
    public void updateCourierOrderStats(String courierId, double distance) {
        Courier courier = getCourierById(courierId);
        if (courier != null) {
            // 更新总体统计
            courier.setCompletedOrders(courier.getCompletedOrders() + 1);
            courier.setTotalDistance(courier.getTotalDistance() + distance);
            
            // 更新日统计
            courier.setDailyDeliveries(courier.getDailyDeliveries() + 1);
            courier.setDailyDistance(courier.getDailyDistance() + distance);
            
            // 更新月统计
            courier.setMonthlyDeliveries(courier.getMonthlyDeliveries() + 1);
            courier.setMonthlyDistance(courier.getMonthlyDistance() + distance);
            
            courierRepository.save(courier);
        }
    }

    @Override
    public Map<String, Object> getCourierRanking(String startDate, String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
        
        // 获取时间范围内的所有订单
        List<Order> orders = orderRepository.findByCreateTimeBetween(start, end);
        
        // 按快递员分组统计订单
        Map<String, List<Order>> ordersByCourier = orders.stream()
                .collect(Collectors.groupingBy(Order::getCourierId));
        
        // 获取所有活跃快递员
        List<Courier> couriers = getAllActiveCouriers();
        
        // 统计每个快递员的绩效数据
        List<Map<String, Object>> rankings = new ArrayList<>();
        for (Courier courier : couriers) {
            List<Order> courierOrders = ordersByCourier.getOrDefault(courier.getId(), new ArrayList<>());
            
            // 统计基本数据
            long deliveries = courierOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                    .count();
            
            double totalDistance = courierOrders.stream()
                    .mapToDouble(Order::getDeliveryDistance)
                    .sum();
            
            double avgRating = courierOrders.stream()
                    .filter(o -> o.getRating() != null)
                    .mapToDouble(Order::getRating)
                    .average()
                    .orElse(0.0);
            
            long complaints = courierOrders.stream()
                    .filter(o -> o.getRating() != null && o.getRating() <= 2)
                    .count();
            
            // 计算准时率
            long onTimeDeliveries = courierOrders.stream()
                    .filter(Order::isDeliveredOnTime)
                    .count();
            double onTimeRate = courierOrders.isEmpty() ? 0 : 
                    (double) onTimeDeliveries / courierOrders.size();
            
            // 组装快递员排名数据
            Map<String, Object> courierStats = new HashMap<>();
            courierStats.put("id", courier.getId());
            courierStats.put("name", courier.getName());
            courierStats.put("deliveries", deliveries);
            courierStats.put("distance", totalDistance);
            courierStats.put("rating", avgRating);
            courierStats.put("complaints", complaints);
            courierStats.put("onTimeRate", onTimeRate);
            
            rankings.add(courierStats);
        }
        
        // 按配送单数降序排序
        rankings.sort((a, b) -> Long.compare(
                (Long) b.get("deliveries"),
                (Long) a.get("deliveries")
        ));
        
        // 返回排名结果
        Map<String, Object> result = new HashMap<>();
        result.put("rankings", rankings);
        result.put("totalCouriers", couriers.size());
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        
        return result;
    }
} 