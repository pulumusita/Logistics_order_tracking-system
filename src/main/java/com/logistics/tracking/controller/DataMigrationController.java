package com.logistics.tracking.controller;

import com.logistics.tracking.service.impl.DataMigrationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据迁移控制器，提供API触发数据迁移任务
 * 注意：仅供管理员使用
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/migration")
@RequiredArgsConstructor
public class DataMigrationController {
    
    private final DataMigrationServiceImpl migrationService;
    
    /**
     * 更新所有缺少配送距离的订单
     * 
     * @return 更新结果
     */
    @PostMapping("/update-order-distances")
    public ResponseEntity<Map<String, Object>> updateOrderDistances() {
        log.info("接收到更新订单配送距离的请求");
        
        long startTime = System.currentTimeMillis();
        int updatedCount = migrationService.updateOrdersWithoutDistance();
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("updatedCount", updatedCount);
        result.put("timeUsed", (endTime - startTime) / 1000 + " 秒");
        
        return ResponseEntity.ok(result);
    }
} 