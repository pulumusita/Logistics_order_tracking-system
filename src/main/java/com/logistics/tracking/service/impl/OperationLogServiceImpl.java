package com.logistics.tracking.service.impl;

import com.logistics.tracking.model.OperationLog;
import com.logistics.tracking.repository.OperationLogRepository;
import com.logistics.tracking.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Override
    public OperationLog save(OperationLog log) {
        if (log.getOperationTime() == null) {
            log.setOperationTime(LocalDateTime.now());
        }
        return operationLogRepository.save(log);
    }

    @Override
    public OperationLog findById(String id) {
        Optional<OperationLog> logOpt = operationLogRepository.findById(id);
        return logOpt.orElse(null);
    }

    @Override
    public List<OperationLog> findByOperationType(String operationType) {
        return operationLogRepository.findByOperationType(operationType);
    }

    @Override
    public List<OperationLog> findByOperatorId(String operatorId) {
        return operationLogRepository.findByOperatorIdOrderByOperationTimeDesc(operatorId);
    }

    @Override
    public List<OperationLog> findByTargetId(String targetId) {
        return operationLogRepository.findByTargetIdOrderByOperationTimeDesc(targetId);
    }

    @Override
    public List<OperationLog> findByTimeRange(LocalDateTime start, LocalDateTime end) {
        return operationLogRepository.findByOperationTimeBetweenOrderByOperationTimeDesc(start, end);
    }
} 