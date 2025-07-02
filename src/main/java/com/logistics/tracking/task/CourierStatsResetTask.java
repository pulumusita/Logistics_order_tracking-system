package com.logistics.tracking.task;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourierStatsResetTask {

    private final MongoTemplate mongoTemplate;

    // 每天凌晨0点重置每日统计数据
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyStats() {
        Update update = new Update()
            .set("dailyDeliveries", 0)
            .set("dailyDistance", 0.0);
        mongoTemplate.updateMulti(new Query(), update, "couriers");
    }

    // 每月1号凌晨0点重置每月统计数据
    @Scheduled(cron = "0 0 0 1 * ?")
    public void resetMonthlyStats() {
        Update update = new Update()
            .set("monthlyDeliveries", 0)
            .set("monthlyDistance", 0.0);
        mongoTemplate.updateMulti(new Query(), update, "couriers");
    }
} 