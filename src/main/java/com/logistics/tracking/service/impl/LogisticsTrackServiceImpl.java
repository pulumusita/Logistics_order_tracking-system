package com.logistics.tracking.service.impl;

/**
 * 物流轨迹服务实现类
 * 
 * 该类负责处理物流轨迹相关的业务逻辑，包括：
 * 1. 创建和保存物流轨迹记录
 * 2. 根据订单ID查询物流轨迹
 * 3. 根据快递员ID查询物流轨迹
 * 4. 根据操作员ID查询物流轨迹
 * 5. 创建订单状态变更的轨迹记录
 * 
 * 物流轨迹记录了订单在配送过程中的每一个状态变更和位置信息，
 * 可用于向客户展示订单的实时位置和配送进度。
 * 
 * 系统中的轨迹数据来源：
 * - 系统自动生成（如订单创建、分配快递员等）
 * - 快递员手动更新（如取件、配送中等）
 * - 客户操作（如签收确认）
 * 
 * 轨迹数据包含以下核心信息：
 * - 订单ID：关联的订单
 * - 状态：当前物流状态
 * - 位置信息：经纬度和地址描述
 * - 操作人信息：谁触发了这条轨迹记录
 * - 时间戳：状态变更的时间点
 * - 描述信息：对当前状态的补充说明
 */


import com.logistics.tracking.model.LogisticsTrack;
import com.logistics.tracking.model.OrderStatus;
import com.logistics.tracking.repository.LogisticsTrackRepository;
import com.logistics.tracking.service.LogisticsTrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LogisticsTrackServiceImpl implements LogisticsTrackService {

    @Autowired
    private LogisticsTrackRepository trackRepository;

    @Override
    public LogisticsTrack save(LogisticsTrack track) {
        track.setTimestamp(LocalDateTime.now());
        return trackRepository.save(track);
    }

    @Override
    public List<LogisticsTrack> findByOrderId(String orderId) {
        return trackRepository.findByOrderIdOrderByTimestampDesc(orderId);
    }

    @Override
    public List<LogisticsTrack> findByCourierId(String courierId) {
        return trackRepository.findByCourierIdOrderByTimestampDesc(courierId);
    }

    @Override
    public List<LogisticsTrack> getTracksByOperatorId(String operatorId) {
        return trackRepository.findByOperatorIdOrderByTimestampDesc(operatorId);
    }

    @Override
    public LogisticsTrack createStatusChangeTrack(
            String orderId,
            OrderStatus status,
            String location,
            double latitude,
            double longitude,
            String operatorId,
            String operatorName,
            String operatorType,
            String description
    ) {
        LogisticsTrack track = new LogisticsTrack();
        track.setOrderId(orderId);
        track.setStatus(status);
        track.setLocation(location);
        track.setLatitude(latitude);
        track.setLongitude(longitude);
        track.setOperatorId(operatorId);
        track.setOperatorName(operatorName);
        track.setOperatorType(operatorType);
        track.setDescription(description);
        track.setTimestamp(LocalDateTime.now());
        
        return trackRepository.save(track);
    }
} 