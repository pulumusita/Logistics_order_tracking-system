package com.logistics.tracking.controller;

import com.logistics.tracking.model.OperationLog;
import com.logistics.tracking.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志控制器
 * 提供查询敏感操作日志的接口
 */
@Slf4j
@RestController
@RequestMapping("/api/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    /**
     * 获取所有操作日志
     */
    @GetMapping
    public ResponseEntity<List<OperationLog>> getAllLogs() {
        log.debug("获取所有操作日志");
        return ResponseEntity.ok(operationLogService.findByTimeRange(
            LocalDateTime.now().minusMonths(1),  // 默认查询最近一个月的日志
            LocalDateTime.now()
        ));
    }

    /**
     * 根据ID获取日志详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<OperationLog> getLogById(@PathVariable String id) {
        log.debug("获取日志详情: {}", id);
        OperationLog log = operationLogService.findById(id);
        if (log != null) {
            return ResponseEntity.ok(log);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 根据操作类型查询日志
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<OperationLog>> getLogsByType(@PathVariable String type) {
        log.debug("根据类型查询日志: {}", type);
        return ResponseEntity.ok(operationLogService.findByOperationType(type));
    }

    /**
     * 根据操作人ID查询日志
     */
    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<List<OperationLog>> getLogsByOperator(@PathVariable String operatorId) {
        log.debug("根据操作人查询日志: {}", operatorId);
        return ResponseEntity.ok(operationLogService.findByOperatorId(operatorId));
    }

    /**
     * 根据目标ID查询日志
     */
    @GetMapping("/target/{targetId}")
    public ResponseEntity<List<OperationLog>> getLogsByTarget(@PathVariable String targetId) {
        log.debug("根据目标ID查询日志: {}", targetId);
        return ResponseEntity.ok(operationLogService.findByTargetId(targetId));
    }

    /**
     * 根据时间范围查询日志
     */
    @GetMapping("/time-range")
    public ResponseEntity<List<OperationLog>> getLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.debug("根据时间范围查询日志: {} - {}", start, end);
        return ResponseEntity.ok(operationLogService.findByTimeRange(start, end));
    }
} 