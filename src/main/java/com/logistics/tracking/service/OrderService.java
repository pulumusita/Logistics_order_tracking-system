package com.logistics.tracking.service;

import com.logistics.tracking.model.Order;
import com.logistics.tracking.model.OrderStatus;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderService {
    /**
     * 创建订单
     */
    Order createOrder(Order order);

    /**
     * 更新订单
     */
    Order updateOrder(Order order);

    /**
     * 更新订单状态
     */
    Order updateOrderStatus(String orderId, OrderStatus status);

    /**
     * 根据ID获取订单
     */
    Optional<Order> getOrderById(String id);

    /**
     * 删除订单
     */
    void deleteOrder(String id);

    /**
     * 获取所有订单
     */
    List<Order> getAllOrders();

    /**
     * 根据状态获取订单
     */
    List<Order> getOrdersByStatus(OrderStatus status);

    /**
     * 根据邮箱获取订单
     */
    List<Order> getOrdersByEmail(String email);

    /**
     * 获取快递员的所有订单
     */
    List<Order> getCourierOrders(String courierId);

    /**
     * 获取快递员的指定状态订单
     */
    List<Order> getCourierOrdersByStatus(String courierId, OrderStatus status);

    /**
     * 分配快递员（简单版本）
     */
    Order assignCourier(String orderId, String courierId);

    /**
     * 分配快递员（带名字版本）
     */
    Order assignCourier(String orderId, String courierId, String courierName);

    /**
     * 完成配送
     */
    void completeDelivery(String orderId);

    /**
     * 计算运费
     * @param weight 重量（kg）
     * @param distance 距离（km）
     * @return 运费（元）
     */
    double calculateFreight(double weight, double distance);

    /**
     * 派发订单
     */
    Order dispatchOrder(String orderId, String courierId, String courierName);

    /**
     * 检查并更新订单状态
     */
    void checkAndUpdateOrderStatus();

    // 统计报表相关方法
    Map<String, Object> getStatisticsSummary(String startDate, String endDate);
    Map<String, Object> getOrderTrend(String startDate, String endDate);
    Map<String, Object> getPerformanceMetrics(String startDate, String endDate);
    Map<String, Object> getAreaDistribution(String startDate, String endDate);
} 