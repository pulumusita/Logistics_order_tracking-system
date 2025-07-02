package com.logistics.tracking.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    
    // 基本信息
    private String customerId;        // 客户ID
    private String courierId;         // 快递员ID
    private String courierName;       // 快递员姓名
    private String courierPhone;      // 快递员电话
    private String trackingNumber;    // 运单号
    private OrderStatus status;       // 订单状态
    
    // 地址信息
    private String senderName;        // 发件人姓名
    private String senderPhone;       // 发件人电话
    private String senderAddress;     // 发件人地址
    @Email
    private String senderEmail;       // 发件人邮箱
    private String receiverName;      // 收件人姓名
    private String receiverPhone;     // 收件人电话
    private String receiverAddress;   // 收件人地址
    @Email
    private String receiverEmail;     // 收件人邮箱
    private String deliveryArea;      // 配送区域
    
    // 配送信息
    private GeoLocation currentLocation;  // 当前位置
    private Double currentLat;         // 当前纬度
    private Double currentLng;         // 当前经度
    private Double deliveryDistance;      // 配送距离
    private Double deliveryFee;           // 配送费用
    private Integer weight;               // 包裹重量(克)
    private String packageType;           // 包裹类型
    
    // 支付信息
    private Double amount;            // 订单金额
    private String paymentMethod;     // 支付方式
    private boolean isPaid;           // 是否已支付
    
    // 评价信息
    private Integer rating;           // 评分(1-5)
    private String ratingComment;     // 评价内容
    private Boolean isDeliveredOnTime; // 是否准时送达
    private Double totalDistance;      // 总配送距离
    private Double averageRating;      // 平均评分
    
    // 时间信息
    private LocalDateTime createTime;           // 创建时间
    private LocalDateTime updateTime;           // 更新时间
    private LocalDateTime paymentTime;          // 支付时间
    private LocalDateTime assignTime;           // 分配时间
    private LocalDateTime pickupTime;           // 取件时间
    private LocalDateTime deliveryTime;         // 送达时间
    private LocalDateTime estimatedDeliveryTime; // 预计送达时间
    private LocalDateTime dispatchTime;         // 派发时间
    private LocalDateTime transitTime;          // 开始运输时间
    
    // 其他信息
    private String remarks;           // 备注
    private String signatureImage;    // 签名图片
    
    public boolean isDeliveredOnTime() {
        if (status != OrderStatus.DELIVERED || deliveryTime == null || estimatedDeliveryTime == null) {
            return false;
        }
        return !deliveryTime.isAfter(estimatedDeliveryTime);
    }
} 