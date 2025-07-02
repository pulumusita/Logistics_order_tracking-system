package com.logistics.tracking.service.impl;


import com.logistics.tracking.model.Order;
import com.logistics.tracking.model.OrderStatus;
import com.logistics.tracking.model.Courier;
import com.logistics.tracking.repository.OrderRepository;
import com.logistics.tracking.service.OrderService;
import com.logistics.tracking.service.CourierPerformanceService;
import com.logistics.tracking.service.EmailService;
import com.logistics.tracking.service.MapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final CourierPerformanceService courierService;
    private final EmailService emailService;
    private final MapService mapService;
    private final MongoTemplate mongoTemplate;

    // 基础运费（元/公里）
    private static final double BASE_FREIGHT_PER_KM = 2.0;
    // 重量附加费（元/公斤）
    private static final double WEIGHT_SURCHARGE_PER_KG = 1.5;
    // 体积附加费（元/立方米）
    private static final double VOLUME_SURCHARGE_PER_M3 = 100.0;
    // 骑手速度（米/小时）
    private static final double COURIER_SPEED = 80 * 1000;
    
    @Override
    @Transactional
    public Order createOrder(Order order) {
        log.debug("开始创建订单: {}", order);
        
        // 验证邮箱地址
        if (order.getSenderEmail() == null || order.getSenderEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("发件人邮箱地址不能为空");
        }
        if (order.getReceiverEmail() == null || order.getReceiverEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("收件人邮箱地址不能为空");
        }
        
        // 设置初始状态
        order.setStatus(OrderStatus.PENDING);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        // 计算运费和配送距离
        double distance = mapService.calculateDistance(order.getSenderAddress(), order.getReceiverAddress());
        log.debug("计算得到运输距离: {}公里", distance);
        
        double amount = calculateFreight(order.getWeight(), distance);
        log.debug("计算得到运费: {}元", amount);
        order.setAmount(amount);
        
        // 保存配送距离
        order.setDeliveryDistance(distance);
        
        // 获取更详细的路线信息用于报表统计
        try {
            Map<String, Object> routeInfo = mapService.getDeliveryRoute(order);
            if (routeInfo != null && routeInfo.containsKey("distance")) {
                double routeDistance = Double.parseDouble(String.valueOf(routeInfo.get("distance")));
                // 更新为更准确的距离
                order.setDeliveryDistance(routeDistance);
                log.debug("通过路线规划API获取更准确的配送距离: {}米", routeDistance);
            }
        } catch (Exception e) {
            log.warn("获取路线规划详情失败，使用直线距离: {}米", distance, e);
        }
        
        // 保存订单
        Order savedOrder = orderRepository.save(order);
        log.info("订单创建成功: {}", savedOrder);
        
        //发送订单创建通知
       try {
           emailService.sendOrderCreationNotification(savedOrder);
           log.debug("订单创建通知邮件发送成功");
       } catch (Exception e) {
           log.error("订单创建通知邮件发送失败", e);
       }
        
        return savedOrder;
    }
    
    @Override
    @Transactional
    public Order updateOrder(Order order) {
        // 获取原始订单数据
        Optional<Order> existingOrderOpt = orderRepository.findById(order.getId());
        if (existingOrderOpt.isPresent()) {
            Order existingOrder = existingOrderOpt.get();
            
            // 检查地址是否变更
            boolean addressChanged = !existingOrder.getSenderAddress().equals(order.getSenderAddress()) || 
                                    !existingOrder.getReceiverAddress().equals(order.getReceiverAddress());
            
            // 如果地址变更，重新计算配送距离
            if (addressChanged) {
                log.debug("订单地址已变更，重新计算配送距离");
                
                // 计算新的配送距离
                double distance = mapService.calculateDistance(order.getSenderAddress(), order.getReceiverAddress());
                order.setDeliveryDistance(distance);
                
                // 获取更详细的路线信息
                try {
                    Map<String, Object> routeInfo = mapService.getDeliveryRoute(order);
                    if (routeInfo != null && routeInfo.containsKey("distance")) {
                        double routeDistance = Double.parseDouble(String.valueOf(routeInfo.get("distance")));
                        order.setDeliveryDistance(routeDistance);
                        log.debug("地址变更后，通过路线规划API获取更准确的配送距离: {}米", routeDistance);
                    }
                } catch (Exception e) {
                    log.warn("获取路线规划详情失败，使用直线距离: {}米", distance, e);
                }
                
                // 重新计算配送费用
                double amount = calculateFreight(order.getWeight(), distance);
                order.setAmount(amount);
            } else {
                // 如果地址未变更但配送距离为空，则尝试更新
                if (order.getDeliveryDistance() == null) {
                    double distance = mapService.calculateDistance(order.getSenderAddress(), order.getReceiverAddress());
                    order.setDeliveryDistance(distance);
                    log.debug("订单配送距离为空，已更新为: {}米", distance);
                }
            }
        }
        
        order.setUpdateTime(LocalDateTime.now());
        return orderRepository.save(order);
    }
    
    @Override
    @Transactional
    public Order updateOrderStatus(String orderId, OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        Order order = orderOpt.get();
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());

        // 处理特殊状态
        if (status == OrderStatus.PICKED_UP) {
            order.setPickupTime(LocalDateTime.now());
            // 设置初始位置为发件人地址的坐标
            try {
                Map<String, Double> senderLocation = mapService.getLocation(order.getSenderAddress());
                order.setCurrentLat(senderLocation.get("latitude"));
                order.setCurrentLng(senderLocation.get("longitude"));
                log.info("订单{}初始化骑手位置为发件人地址坐标: ({}, {})", 
                    order.getId(), order.getCurrentLat(), order.getCurrentLng());
            } catch (Exception e) {
                log.error("获取发件人地址坐标失败", e);
            }
        } else if (status == OrderStatus.DELIVERED) {
            order.setDeliveryTime(LocalDateTime.now());
            // 设置最终位置为收件人地址的坐标
            try {
                Map<String, Double> receiverLocation = mapService.getLocation(order.getReceiverAddress());
                order.setCurrentLat(receiverLocation.get("latitude"));
                order.setCurrentLng(receiverLocation.get("longitude"));
                log.info("订单{}完成配送，更新骑手位置为收件人地址坐标: ({}, {})", 
                    order.getId(), order.getCurrentLat(), order.getCurrentLng());
            } catch (Exception e) {
                log.error("获取收件人地址坐标失败", e);
            }
        }

        return orderRepository.save(order);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(String id) {
        return orderRepository.findById(id);
    }
    
    @Override
    @Transactional
    public void deleteOrder(String id) {
        orderRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        log.debug("获取所有订单");
        List<Order> orders = orderRepository.findAll();
        log.debug("获取到 {} 个订单", orders.size());
        return orders;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByEmail(String email) {
        return orderRepository.findBySenderEmailOrReceiverEmail(email, email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> getCourierOrders(String courierId) {
        return orderRepository.findByCourierId(courierId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> getCourierOrdersByStatus(String courierId, OrderStatus status) {
        return orderRepository.findByCourierIdAndStatus(courierId, status);
    }
    
    @Override
    @Transactional
    public void completeDelivery(String orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        Order order = orderOpt.get();
        /**
         * 更新订单状态为已送达，并设置送达时间和更新时间。
         * //t: 更新订单状态为已送达
         * //r: 设置送达时间和更新时间
         * //n: 保存更新后的订单信息到数据库
         */
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveryTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderRepository.save(order);
    }
    
    /**
     * 根据重量和距离计算运费。
     * //t: 计算基础运费
     * //r: 计算重量附加费
     * //n: 返回总运费
     *
     * @param weight  订单的重量
     * @param distance 订单的距离
     * @return 总运费
     */
    @Override
    public double calculateFreight(double weight, double distance) {
        // 基础运费 = 距离 * 每公里费用
        double baseFreight = distance * BASE_FREIGHT_PER_KM;
        // 重量附加费 = 重量 * 每公斤附加费
        double weightSurcharge = weight * WEIGHT_SURCHARGE_PER_KG;
        // 总运费 = 基础运费 + 重量附加费
        return baseFreight + weightSurcharge;
    }
    
    private double calculateAmount(double weight, double volume, double distance) {
        // 基础运费 = 距离 * 每公里费用
        double baseFreight = distance * BASE_FREIGHT_PER_KM;
        // 重量附加费 = 重量 * 每公斤附加费
        double weightSurcharge = weight * WEIGHT_SURCHARGE_PER_KG;
        // 体积附加费 = 体积 * 每立方米附加费
        double volumeSurcharge = volume * VOLUME_SURCHARGE_PER_M3;
        // 总运费 = 基础运费 + 重量附加费 + 体积附加费
        return baseFreight + weightSurcharge + volumeSurcharge;
    }

    @Override
    @Transactional
    public Order assignCourier(String orderId, String courierId) {
        return assignCourier(orderId, courierId, null);
    }
    
    @Override
    @Transactional
    public Order assignCourier(String orderId, String courierId, String courierName) {
        /**
         * 分配快递员并直接开始运输流程。
         * 
         * 此方法会执行以下操作：
         * 1. 根据订单ID查找订单，如果找不到则抛出异常
         * 2. 设置快递员信息（ID和姓名）
         * 3. 更新订单状态为运输中（IN_TRANSIT）
         * 4. 设置相关时间戳（派发时间、取件时间、运输开始时间和更新时间）
         * 5. 保存更新后的订单信息到数据库
         * 6. 发送订单状态变更通知邮件
         *
         * 注意：此方法跳过了DISPATCHED和PICKED_UP状态，直接从PENDING进入IN_TRANSIT状态，
         *       简化了配送流程。适用于需要快速启动配送的场景。
         * 
         * @param orderId 订单的唯一标识符
         * @param courierId 快递员的唯一标识符
         * @param courierName 快递员姓名
         * @return 已更新的订单对象
         */
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        Order order = orderOpt.get();
        LocalDateTime now = LocalDateTime.now();
        
        order.setCourierId(courierId);
        order.setCourierName(courierName);
        // 直接设置为运输中状态
        order.setStatus(OrderStatus.IN_TRANSIT);
        // 设置相关时间
        order.setDispatchTime(now);
        order.setPickupTime(now);  // 设置取件时间
        order.setTransitTime(now); // 设置开始运输时间
        order.setUpdateTime(now);
        
        log.info("订单{}已分配给快递员{}并开始运输", orderId, courierName);
        
        // 发送通知
        try {
            emailService.sendStatusChangeNotification(order, OrderStatus.IN_TRANSIT);
        } catch (Exception e) {
            log.error("发送订单状态更新通知失败", e);
        }
        
        return orderRepository.save(order);
    }
    
    @Scheduled(fixedRate = 30000) // 检查订单状态并更新，30秒更新一次
    public void checkAndUpdateOrderStatus() {
        log.debug("开始检查订单状态");
        LocalDateTime now = LocalDateTime.now();
        
        // 获取所有进行中的订单
        List<Order> orders = orderRepository.findByStatusIn(
            Arrays.asList(OrderStatus.DISPATCHED, OrderStatus.PICKED_UP, OrderStatus.IN_TRANSIT)
        );
        
        for (Order order : orders) {
            boolean updated = false;
            
            // 派发后直接设置为运输中状态，不再需要中间状态
            if (order.getStatus() == OrderStatus.DISPATCHED 
                && order.getDispatchTime() != null) {
                order.setStatus(OrderStatus.IN_TRANSIT);
                order.setPickupTime(now);
                order.setTransitTime(now);
                updated = true;
                log.debug("订单{}状态从已派发直接更新为运输中", order.getId());
            }
            
            // 已取件状态更新为运输中
            else if (order.getStatus() == OrderStatus.PICKED_UP 
                && order.getPickupTime() != null) {
                order.setStatus(OrderStatus.IN_TRANSIT);
                order.setTransitTime(now);
                updated = true;
                log.debug("订单{}状态从已取件更新为运输中", order.getId());
            }
            
            // 检查运输中的订单是否应该更新为已送达
            else if (order.getStatus() == OrderStatus.IN_TRANSIT 
                && order.getTransitTime() != null) {
                
                // 计算预估总距离
                double estimatedDistance = mapService.calculateDistance(
                    order.getSenderAddress(), 
                    order.getReceiverAddress()
                );
                
                // 计算已行驶时间（小时）
                double hoursElapsed = java.time.Duration.between(order.getTransitTime(), now).toHours();
                
                // 计算已行驶距离（米）
                double distanceTraveled = hoursElapsed * 80 * 1000; // 80km/h转换为米/小时
                
                // 计算剩余距离
                double distanceFinsh = Math.max(0, estimatedDistance - distanceTraveled);
                
                // 如果剩余距离为0或运输时间超过48小时，则更新为已送达
                if (distanceFinsh == 0 || hoursElapsed >= 48) {
                    order.setStatus(OrderStatus.DELIVERED);
                    order.setDeliveryTime(now);
                    updated = true;
                    log.debug("订单{}已自动更新为已送达状态 - 已行驶时间: {}小时, 剩余距离: {}米", 
                        order.getId(), hoursElapsed, distanceFinsh);
                }
            }
            
            if (updated) {
                order.setUpdateTime(now);
                orderRepository.save(order);
                log.debug("订单{}状态已更新为{}", order.getId(), order.getStatus());
                
                // 发送快递送达通知
                try {
                    emailService.sendOrderDeliveredNotification(order);
                } catch (Exception e) {
                    log.error("发送订单已送达邮件失败", e);
                }
            }
        }
    }
    
    @Override
    @Transactional
    public Order dispatchOrder(String orderId, String courierId, String courierName) {
        /**
         * 派发订单给快递员并开始运输流程。
         * 
         * 此方法会执行以下操作：
         * 1. 根据订单ID查找订单，如果找不到则抛出异常
         * 2. 检查订单状态是否为待处理（PENDING），否则抛出异常
         * 3. 设置快递员信息、更新订单状态为运输中（IN_TRANSIT）
         * 4. 设置相关时间戳（派发时间、取件时间、运输开始时间）
         * 5. 更新快递员的统计数据（配送距离）
         * 6. 发送订单状态变更通知邮件
         * 
         * @param orderId 订单的唯一标识符
         * @param courierId 快递员的唯一标识符
         * @param courierName 快递员姓名
         * @return 已更新的订单对象
         */
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderId));
            
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("只能派发待处理的订单");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        order.setCourierId(courierId);
        order.setCourierName(courierName);
        // 直接设置为运输中状态
        order.setStatus(OrderStatus.IN_TRANSIT);
        // 设置相关时间
        order.setDispatchTime(now);
        order.setPickupTime(now);  // 设置取件时间
        order.setTransitTime(now); // 设置开始运输时间
        order.setUpdateTime(now);
        
        log.info("订单{}已派发给快递员{}并开始运输", orderId, courierName);
        
        // 更新配送距离和快递员统计数据
        double distance = mapService.calculateDistance(order.getSenderAddress(), order.getReceiverAddress());
        
        // 保存配送距离
        if (order.getDeliveryDistance() == null) {
            order.setDeliveryDistance(distance);
            
            // 获取更详细的路线信息
            try {
                Map<String, Object> routeInfo = mapService.getDeliveryRoute(order);
                if (routeInfo != null && routeInfo.containsKey("distance")) {
                    double routeDistance = Double.parseDouble(String.valueOf(routeInfo.get("distance")));
                    order.setDeliveryDistance(routeDistance);
                    distance = routeDistance; // 更新为更准确的距离
                    log.debug("通过路线规划API获取更准确的配送距离: {}米", routeDistance);
                }
            } catch (Exception e) {
                log.warn("获取路线规划详情失败，使用直线距离: {}米", distance, e);
            }
        }
        
        // 更新快递员统计数据
        courierService.updateCourierOrderStats(courierId, distance);
        
        // 发送通知
        try {
            emailService.sendStatusChangeNotification(order, OrderStatus.IN_TRANSIT);
        } catch (Exception e) {
            log.error("发送订单状态更新通知失败", e);
        }
        
        return orderRepository.save(order);
    }

    @Scheduled(fixedRate = 30000) // 每30秒执行一次
    public void updateCourierLocations() {
        log.debug("开始更新骑手位置");
        LocalDateTime now = LocalDateTime.now();
        
        // 获取所有运输中的订单
        List<Order> orders = orderRepository.findByStatus(OrderStatus.IN_TRANSIT);
        
        for (Order order : orders) {
            try {
                log.debug("处理订单: {}, 当前骑手位置: ({}, {})", 
                    order.getId(), order.getCurrentLat(), order.getCurrentLng());

                // 获取配送路线信息
                Map<String, Object> routeInfo = mapService.getDeliveryRoute(order);
                if (routeInfo == null || !routeInfo.containsKey("steps")) {
                    log.warn("订单{}的路线信息为空或没有steps数据", order.getId());
                    continue;
                }
                
                // 获取路线步骤
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> steps = (List<Map<String, Object>>) routeInfo.get("steps");
                if (steps == null || steps.isEmpty()) {
                    log.warn("订单{}的路线步骤为空", order.getId());
                    continue;
                }
                
                // 计算已经行驶的时间（小时）
                double hoursElapsed = java.time.Duration.between(order.getTransitTime(), now).toSeconds() / 3600.0;
                
                // 计算已经行驶的总距离（米）
                double distanceTraveled = hoursElapsed * COURIER_SPEED;
                
                log.debug("订单{} - 已行驶时间: {}小时, 已行驶距离: {}米", 
                    order.getId(), hoursElapsed, distanceTraveled);
                
                // 遍历路线步骤，找到当前位置
                double accumulatedDistance = 0;
                boolean positionUpdated = false;
                
                for (int i = 0; i < steps.size(); i++) {
                    Map<String, Object> step = steps.get(i);
                    if (!step.containsKey("distance")) {
                        log.warn("订单{}的第{}段路没有距离信息", order.getId(), i + 1);
                        continue;
                    }
                    
                    // 确保正确处理字符串类型的距离值
                    double stepDistance = Double.parseDouble(String.valueOf(step.get("distance")));
                    log.debug("订单{} - 第{}段路距离: {}米", order.getId(), i + 1, stepDistance);
                    
                    if (accumulatedDistance + stepDistance > distanceTraveled) {
                        // 骑手还在这一段路上
                        if (i > 0) {
                            // 使用上一段路的终点坐标（从polyline中获取最后一个坐标）
                            Map<String, Object> lastStep = steps.get(i - 1);
                            if (lastStep != null && lastStep.containsKey("polyline")) {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> polyline = (List<Map<String, Object>>) lastStep.get("polyline");
                                if (polyline != null && !polyline.isEmpty()) {
                                    Map<String, Object> endPoint = polyline.get(polyline.size() - 1);
                                    if (endPoint != null && endPoint.containsKey("lat") && endPoint.containsKey("lng")) {
                                        order.setCurrentLat(Double.parseDouble(String.valueOf(endPoint.get("lat"))));
                                        order.setCurrentLng(Double.parseDouble(String.valueOf(endPoint.get("lng"))));
                                        
                                        log.info("订单{}的骑手正在第{}段路上，当前位置更新为上一段终点: ({}, {})", 
                                            order.getId(), i + 1, order.getCurrentLat(), order.getCurrentLng());
                                        positionUpdated = true;
                                    } else {
                                        log.warn("订单{}的第{}段路终点坐标数据不完整: {}", order.getId(), i, endPoint);
                                    }
                                } else {
                                    log.warn("订单{}的第{}段路polyline为空", order.getId(), i);
                                }
                            } else {
                                log.warn("订单{}的第{}段路没有polyline信息", order.getId(), i);
                            }
                        }
                        break;
                    }
                    
                    accumulatedDistance += stepDistance;
                    
                    // 如果已经完成这一段路，使用这一段的终点坐标
                    if (step.containsKey("polyline")) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> polyline = (List<Map<String, Object>>) step.get("polyline");
                        if (polyline != null && !polyline.isEmpty()) {
                            Map<String, Object> endPoint = polyline.get(polyline.size() - 1);
                            if (endPoint != null && endPoint.containsKey("lat") && endPoint.containsKey("lng")) {
                                order.setCurrentLat(Double.parseDouble(String.valueOf(endPoint.get("lat"))));
                                order.setCurrentLng(Double.parseDouble(String.valueOf(endPoint.get("lng"))));
                                
                                log.debug("订单{}更新位置 - 第{}段路终点: ({}, {})", 
                                    order.getId(), i + 1, order.getCurrentLat(), order.getCurrentLng());
                                
                                if (accumulatedDistance >= distanceTraveled) {
                                    log.info("订单{}完成第{}段路，当前位置: ({}, {}), 已行驶距离: {}米", 
                                        order.getId(), i + 1, order.getCurrentLat(), order.getCurrentLng(), 
                                        accumulatedDistance);
                                    positionUpdated = true;
                                    break;
                                }
                            } else {
                                log.warn("订单{}的第{}段路终点坐标数据不完整: {}", order.getId(), i + 1, endPoint);
                            }
                        } else {
                            log.warn("订单{}的第{}段路polyline为空", order.getId(), i + 1);
                        }
                    } else {
                        log.warn("订单{}的第{}段路没有polyline信息", order.getId(), i + 1);
                    }
                }
                
                if (!positionUpdated && !steps.isEmpty()) {
                    // 如果已经超过总路程，将位置设为终点
                    Map<String, Object> lastStep = steps.get(steps.size() - 1);
                    if (lastStep != null && lastStep.containsKey("polyline")) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> polyline = (List<Map<String, Object>>) lastStep.get("polyline");
                        if (polyline != null && !polyline.isEmpty()) {
                            Map<String, Object> endPoint = polyline.get(polyline.size() - 1);
                            if (endPoint != null && endPoint.containsKey("lat") && endPoint.containsKey("lng")) {
                                order.setCurrentLat(Double.parseDouble(String.valueOf(endPoint.get("lat"))));
                                order.setCurrentLng(Double.parseDouble(String.valueOf(endPoint.get("lng"))));
                                
                                // 更新订单状态为已送达
                                order.setStatus(OrderStatus.DELIVERED);
                                order.setDeliveryTime(now);
                                
                                log.info("订单{}已完成配送，最终位置: ({}, {})", 
                                    order.getId(), order.getCurrentLat(), order.getCurrentLng());
                                    
                                try {
                                    emailService.sendOrderDeliveredNotification(order); // 发送订单完成通知
                                } catch (Exception e) {
                                    log.error("发送订单状态更新通知失败", e);
                                }
                            } else {
                                log.warn("订单{}的最后一段路终点坐标数据不完整: {}", order.getId(), endPoint);
                            }
                        } else {
                            log.warn("订单{}的最后一段路polyline为空", order.getId());
                        }
                    } else {
                        log.warn("订单{}的最后一段路没有polyline信息", order.getId());
                    }
                }
                
                order.setUpdateTime(now);
                orderRepository.save(order);
                log.debug("订单{}位置更新完成，当前位置: ({}, {})", 
                    order.getId(), order.getCurrentLat(), order.getCurrentLng());
                
            } catch (Exception e) {
                log.error("更新订单{}的骑手位置时发生错误: {}", order.getId(), e.getMessage());
                log.debug("错误详情", e);
            }
        }
    }

    @Override
    public Map<String, Object> getStatisticsSummary(String startDate, String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
        
        List<Order> orders = orderRepository.findByCreateTimeBetween(start, end);
        List<Order> completedOrders = orderRepository.findByCreateTimeBetweenAndStatus(start, end, OrderStatus.DELIVERED);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalOrders", orders.size());
        summary.put("completedOrders", completedOrders.size());
        summary.put("totalDistance", orders.stream()
                .mapToDouble(Order::getDeliveryDistance)
                .sum());
        
        return summary;
    }

    @Override
    public Map<String, Object> getOrderTrend(String startDate, String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
        
        List<Order> orders = orderRepository.findByCreateTimeBetween(start, end);
        
        // 按日期分组统计订单数量
        Map<LocalDate, Long> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreateTime().toLocalDate(),
                        Collectors.counting()
                ));
        
        // 按日期分组统计完成订单数量
        Map<LocalDate, Long> completedByDate = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.groupingBy(
                        order -> order.getCreateTime().toLocalDate(),
                        Collectors.counting()
                ));
        
        // 生成日期序列
        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate date = LocalDate.parse(startDate);
        LocalDate endDateParsed = LocalDate.parse(endDate);
        while (!date.isAfter(endDateParsed)) {
            dateRange.add(date);
            date = date.plusDays(1);
        }
        
        // 格式化数据
        Map<String, Object> trend = new HashMap<>();
        trend.put("dates", dateRange.stream()
                .map(d -> d.format(DateTimeFormatter.ISO_DATE))
                .collect(Collectors.toList()));
        trend.put("orders", dateRange.stream()
                .map(d -> ordersByDate.getOrDefault(d, 0L))
                .collect(Collectors.toList()));
        trend.put("completed", dateRange.stream()
                .map(d -> completedByDate.getOrDefault(d, 0L))
                .collect(Collectors.toList()));
        
        return trend;
    }

    @Override
    public Map<String, Object> getPerformanceMetrics(String startDate, String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
        
        List<Order> orders = orderRepository.findByCreateTimeBetween(start, end);
        
        Map<String, Object> metrics = new HashMap<>();
        
        // 计算平均配送时间
        double avgDeliveryTime = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED && o.getDeliveryTime() != null)
                .mapToLong(o -> o.getDeliveryTime().toEpochSecond(ZoneOffset.UTC) - o.getCreateTime().toEpochSecond(ZoneOffset.UTC))
                .average()
                .orElse(0.0) / 3600; // 转换为小时
        metrics.put("averageDeliveryTime", avgDeliveryTime);
        
        // 计算准时率
        long onTimeDeliveries = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED && o.isDeliveredOnTime())
                .count();
        double onTimeRate = orders.isEmpty() ? 0 : (double) onTimeDeliveries / orders.size();
        metrics.put("onTimeDeliveryRate", onTimeRate);
        
        // 统计各状态订单数量
        Map<OrderStatus, Long> statusDistribution = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        metrics.put("statusDistribution", statusDistribution);
        
        // 计算高峰期订单比例
        long peakHourOrders = orders.stream()
                .filter(o -> {
                    int hour = o.getCreateTime().getHour();
                    return (hour >= 11 && hour <= 13) || (hour >= 17 && hour <= 19);
                })
                .count();
        double peakHourRate = orders.isEmpty() ? 0 : (double) peakHourOrders / orders.size();
        metrics.put("peakHourOrderRate", peakHourRate);
        
        return metrics;
    }

    @Override
    public Map<String, Object> getAreaDistribution(String startDate, String endDate) {
        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
        
        List<Order> orders = orderRepository.findByCreateTimeBetween(start, end);
        
        Map<String, Object> distribution = new HashMap<>();
        
        // 按区域统计订单数量
        Map<String, Long> ordersByArea = orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getDeliveryArea,
                        Collectors.counting()
                ));
        distribution.put("ordersByArea", ordersByArea);
        
        // 按区域统计平均配送距离
        Map<String, Double> avgDistanceByArea = orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getDeliveryArea,
                        Collectors.averagingDouble(Order::getDeliveryDistance)
                ));
        distribution.put("averageDistanceByArea", avgDistanceByArea);
        
        return distribution;
    }
} 