package com.logistics.tracking.controller;

import com.logistics.tracking.model.Order;
import com.logistics.tracking.model.OrderStatus;
import com.logistics.tracking.model.GeoLocation;
import com.logistics.tracking.service.OrderService;
import com.logistics.tracking.service.MapService;
import com.logistics.tracking.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import org.springframework.http.HttpStatus;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MapService mapService;
    private final EmailService emailService;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        log.debug("获取所有订单");
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
        log.debug("创建订单: {}", order);
        Order createdOrder = orderService.createOrder(order);
        log.debug("订单创建成功: {}", createdOrder);
        return ResponseEntity.ok(createdOrder);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    @PutMapping("/{orderId}/courier")
    public ResponseEntity<Order> assignCourier(
            @PathVariable String orderId,
            @RequestParam String courierId) {
        return ResponseEntity.ok(orderService.assignCourier(orderId, courierId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        log.debug("获取订单详情: {}", orderId);
        return orderService.getOrderById(orderId)
                .map(order -> {
                    log.debug("找到订单: {}", order);
                    return ResponseEntity.ok(order);
                })
                .orElseGet(() -> {
                    log.warn("订单不存在: {}", orderId);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<List<Order>> getOrdersByEmail(@PathVariable String email) {
        return ResponseEntity.ok(orderService.getOrdersByEmail(email));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<?> getOrderTracking(@PathVariable String orderId) {
        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();
            Map<String, Object> response = new HashMap<>();

            // 获取发件人和收件人的地理位置
            GeoLocation senderLocation = mapService.geocode(order.getSenderAddress());
            GeoLocation receiverLocation = mapService.geocode(order.getReceiverAddress());
            
            // 获取路线信息
            Map<String, Object> routeInfo = mapService.getDeliveryRoute(order);
            double totalDistance = Double.parseDouble(String.valueOf(routeInfo.get("distance")));
            
            // 计算剩余距离
            double distanceFinish = totalDistance;
            if (order.getPickupTime() != null && !order.getStatus().equals(OrderStatus.DELIVERED)) {
                // 计算已经过去的时间（分钟）
                long elapsedMinutes = ChronoUnit.MINUTES.between(order.getPickupTime(), LocalDateTime.now());
                // 假设配送速度为80km/h，转换为米/分钟
                double speed = (80.0 * 1000) / 60; // 米/分钟
                double traveledDistance = elapsedMinutes * speed;
                
                // 计算剩余距离
                distanceFinish = Math.max(0, totalDistance - traveledDistance);
            } else if (order.getStatus().equals(OrderStatus.DELIVERED)) {
                distanceFinish = 0;
            }

            response.put("order", order);
            response.put("senderLocation", senderLocation);
            response.put("receiverLocation", receiverLocation);
            response.put("distanceFinsh", distanceFinish); // 接口返回剩余距离
            response.put("totalDistance", totalDistance); // 接口返回总距离

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取订单跟踪信息失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "获取订单跟踪信息失败"));
        }
    }

    @PutMapping("/{orderId}/dispatch")
    public ResponseEntity<Order> dispatchOrder(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(
            orderService.dispatchOrder(
                orderId,
                request.get("courierId"),
                request.get("courierName")
            )
        );
    }

    @GetMapping("/check-status")
    public ResponseEntity<Map<String, Boolean>> checkOrderStatus() {
        orderService.checkAndUpdateOrderStatus();
        Map<String, Boolean> result = new HashMap<>();
        result.put("updated", true);
        return ResponseEntity.ok(result);
    }
} 