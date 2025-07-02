package com.logistics.tracking.controller;

import com.logistics.tracking.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取统计数据摘要
     * @param startDate 开始日期 (yyyy-MM-dd)
     * @param endDate 结束日期 (yyyy-MM-dd)
     * @return 统计数据，包含总订单数、完成订单数和总配送距离
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getStatisticsSummary(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(statisticsService.getStatisticsSummary(startDate, endDate));
    }

    /**
     * 获取配送员排行榜
     * @param startDate 开始日期 (yyyy-MM-dd)
     * @param endDate 结束日期 (yyyy-MM-dd)
     * @param limit 返回的排行数量，默认为10
     * @return 配送员排行数据列表
     */
    @GetMapping("/courier-rankings")
    public ResponseEntity<List<Map<String, Object>>> getCourierRankings(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statisticsService.getCourierRankings(startDate, endDate, limit));
    }
} 