package com.logistics.tracking.service;

import com.logistics.tracking.model.Courier;
import com.logistics.tracking.model.CourierRating;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CourierPerformanceService {
    // 快递员基本信息管理
    Courier createCourier(Courier courier);
    Courier updateCourier(Courier courier);
    void deleteCourier(String id);
    Courier getCourierById(String id);
    List<Courier> getAllActiveCouriers();
    
    // 评分和投诉管理
    CourierRating addRating(CourierRating rating);
    List<CourierRating> getCourierRatings(String courierId);
    
    // 统计数据
    Map<String, Object> getDailyStats(String courierId);
    Map<String, Object> getMonthlyStats(String courierId);
    Map<String, Object> getPerformanceReport(String courierId, LocalDateTime startTime, LocalDateTime endTime);
    Map<String, Object> getCourierRanking(String startDate, String endDate);
    
    // 在线状态管理
    void updateOnlineStatus(String courierId, boolean online);
    List<Courier> getOnlineCouriers();
    
    // 工作区域管理
    List<Courier> getCouriersByWorkArea(String area);
    void updateWorkArea(String courierId, String workArea);
    
    // 新增方法：更新快递员订单统计数据
    void updateCourierOrderStats(String courierId, double distance);
} 