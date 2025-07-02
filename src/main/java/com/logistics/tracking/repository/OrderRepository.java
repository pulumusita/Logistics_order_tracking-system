package com.logistics.tracking.repository;

import com.logistics.tracking.model.Order;
import com.logistics.tracking.model.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单仓库接口，用于管理与订单相关的数据操作。
 *
 * 该接口继承自MongoRepository，提供了一系列方法用于查询订单数据。
 * 核心功能包括：
 * - 根据订单状态查询订单
 * - 根据多个订单状态查询订单
 * - 根据发件人或收件人邮箱查询订单
 * - 根据快递员ID查询订单
 * - 根据快递员ID和订单状态查询订单
 *
 * 使用示例：
 * OrderRepository orderRepository = ...; // 获取OrderRepository实例
 * List<Order> orders = orderRepository.findByStatus(OrderStatus.PENDING); // 查询状态为待处理的订单
 *
 * 构造函数参数：
 * 无
 *
 * 特殊使用限制或潜在的副作用：
 * 无
 */
@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStatusIn(List<OrderStatus> statuses);
    List<Order> findBySenderEmailOrReceiverEmail(String senderEmail, String receiverEmail);
    List<Order> findByCourierId(String courierId);
    List<Order> findByCourierIdAndStatus(String courierId, OrderStatus status);
    List<Order> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    List<Order> findByCustomerId(String customerId);
    
    @Query("{'createTime': {$gte: ?0, $lt: ?1}, 'status': ?2}")
    List<Order> findByCreateTimeBetweenAndStatus(LocalDateTime startTime, LocalDateTime endTime, OrderStatus status);
    
    @Query("{'createTime': {$gte: ?0, $lt: ?1}, 'courierId': ?2}")
    List<Order> findByCreateTimeBetweenAndCourierId(LocalDateTime startTime, LocalDateTime endTime, String courierId);
    
    @Query("{'createTime': {$gte: ?0, $lt: ?1}, 'deliveryArea': ?2}")
    List<Order> findByCreateTimeBetweenAndDeliveryArea(LocalDateTime startTime, LocalDateTime endTime, String area);
}