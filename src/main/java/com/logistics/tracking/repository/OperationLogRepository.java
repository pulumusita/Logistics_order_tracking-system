package com.logistics.tracking.repository;

import com.logistics.tracking.model.OperationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface OperationLogRepository extends MongoRepository<OperationLog, String> {
    // 根据操作类型查询日志
    List<OperationLog> findByOperationType(String operationType);
    
    // 根据操作人ID查询日志
    List<OperationLog> findByOperatorIdOrderByOperationTimeDesc(String operatorId);
    
    // 根据目标ID查询日志
    List<OperationLog> findByTargetIdOrderByOperationTimeDesc(String targetId);
    
    // 根据时间范围查询日志
    List<OperationLog> findByOperationTimeBetweenOrderByOperationTimeDesc(LocalDateTime start, LocalDateTime end);
} 