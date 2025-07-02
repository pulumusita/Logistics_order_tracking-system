package com.logistics.tracking.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "logistics_tracks")
public class LogisticsTrack {
    @Id
    private String id;
    
    // 关联的订单ID
    private String orderId;
    
    // 物流状态
    private OrderStatus status;
    
    // 操作地点
    private String location;
    private double latitude;  // 纬度
    private double longitude; // 经度
    
    // 操作信息
    private String operatorId;    // 操作员ID
    private String operatorName;  // 操作员姓名
    private String operatorType;  // 操作员类型（系统/快递员/分拣员等）
    
    // 操作描述
    private String description;
    
    // 操作时间
    private LocalDateTime timestamp;
    
    // 备注信息
    private String remark;
    
    // 快递员ID
    private String courierId;
} 