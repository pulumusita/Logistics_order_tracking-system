package com.logistics.tracking.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "couriers")
public class Courier {
    @Id
    private String id;
    
    // 基本信息
    private String name;            // 姓名
    private String phone;           // 电话
    private String idCard;          // 身份证号
    private String workArea;        // 工作区域
    private boolean online;         // 在线状态
    private boolean active;         // 是否激活
    
    // 统计数据
    private int completedOrders;    // 已完成订单数
    private int complaintsCount;    // 投诉数量
    private double averageRating;   // 平均评分
    private double totalDistance;   // 总配送距离（公里）
    
    // 当日统计
    private int dailyDeliveries;    // 当日配送单数
    private double dailyDistance;   // 当日配送距离
    private double dailyRating;     // 当日平均评分
    
    // 月度统计
    private int monthlyDeliveries;  // 月度配送单数
    private double monthlyDistance; // 月度配送距离
    private double monthlyRating;   // 月度平均评分
    
    // 时间记录
    private LocalDateTime createTime;    // 创建时间
    private LocalDateTime updateTime;    // 更新时间
    private LocalDateTime lastOnlineTime;// 最后在线时间
} 