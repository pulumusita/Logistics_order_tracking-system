package com.logistics.tracking.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {
    /**
     * 获取统计数据摘要
     * @param startDate 开始日期 (yyyy-MM-dd)
     * @param endDate 结束日期 (yyyy-MM-dd)
     * @return 统计数据，包含总订单数、完成订单数和总配送距离
     */
    Map<String, Object> getStatisticsSummary(String startDate, String endDate);

    /**
     * 获取配送员排行榜
     * @param startDate 开始日期 (yyyy-MM-dd)
     * @param endDate 结束日期 (yyyy-MM-dd)
     * @param limit 返回的排行数量
     * @return 配送员排行数据列表
     */
    List<Map<String, Object>> getCourierRankings(String startDate, String endDate, int limit);
} 