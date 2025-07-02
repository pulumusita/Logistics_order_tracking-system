package com.logistics.tracking.repository;

import com.logistics.tracking.model.LogisticsTrack;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogisticsTrackRepository extends MongoRepository<LogisticsTrack, String> {
    // 根据订单ID查询物流轨迹，按操作时间倒序排列
    List<LogisticsTrack> findByOrderIdOrderByTimestampDesc(String orderId);
    
    // 根据操作员ID查询物流轨迹
    List<LogisticsTrack> findByOperatorIdOrderByTimestampDesc(String operatorId);
    
    // 根据快递员ID查询物流轨迹
    List<LogisticsTrack> findByCourierIdOrderByTimestampDesc(String courierId);
} 