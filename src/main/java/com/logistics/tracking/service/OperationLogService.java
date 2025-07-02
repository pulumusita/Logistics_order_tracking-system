package com.logistics.tracking.service;

import com.logistics.tracking.model.OperationLog;
import java.util.List;
import java.time.LocalDateTime;

public interface OperationLogService {
    // 保存操作日志
    OperationLog save(OperationLog log);
    
    // 根据ID查询日志
    OperationLog findById(String id);
    
    // 根据操作类型查询日志
    List<OperationLog> findByOperationType(String operationType);
    
    // 根据操作人ID查询日志
    List<OperationLog> findByOperatorId(String operatorId);
    
    // 根据目标ID查询日志
    List<OperationLog> findByTargetId(String targetId);
    
    // 根据时间范围查询日志
    List<OperationLog> findByTimeRange(LocalDateTime start, LocalDateTime end);
} 