package com.logistics.tracking.service;

import com.logistics.tracking.model.LogisticsTrack;
import com.logistics.tracking.model.OrderStatus;
import java.util.List;

public interface LogisticsTrackService {
    LogisticsTrack save(LogisticsTrack track);
    
    List<LogisticsTrack> findByOrderId(String orderId);
    
    List<LogisticsTrack> findByCourierId(String courierId);
    
    List<LogisticsTrack> getTracksByOperatorId(String operatorId);
    
    LogisticsTrack createStatusChangeTrack(
            String orderId,
            OrderStatus status,
            String location,
            double latitude,
            double longitude,
            String operatorId,
            String operatorName,
            String operatorType,
            String description
    );
} 