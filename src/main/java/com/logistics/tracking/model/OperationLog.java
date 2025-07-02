package com.logistics.tracking.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * 敏感操作日志实体类
 * 用于记录系统中的敏感操作，如订单删除等
 */
@Data
@Document(collection = "operation_logs")
public class OperationLog {
    @Id
    private String id;
    
    // 操作类型
    private String operationType;
    
    // 操作描述
    private String operationDescription;
    
    // 操作时间
    private LocalDateTime operationTime;
    
    // 操作人信息
    private String operatorId;
    private String operatorName;
    
    // 目标对象信息
    private String targetId;
    private String targetType;
    
    // 详细信息（可存储JSON数据）
    private String details;
    
    // 操作结果
    private boolean success;
    
    // IP地址
    private String ip;
} 