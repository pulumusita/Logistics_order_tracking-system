package com.logistics.tracking.controller;

import com.logistics.tracking.model.LogisticsTrack;
import com.logistics.tracking.model.OrderStatus;
import com.logistics.tracking.service.LogisticsTrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logistics/track")
public class LogisticsTrackController {

    @Autowired
    private LogisticsTrackService trackService;

    @GetMapping("/{orderId}")
    public List<LogisticsTrack> getTrackByOrderId(@PathVariable String orderId) {
        return trackService.findByOrderId(orderId);
    }

    @PostMapping
    public LogisticsTrack createTrack(@RequestBody LogisticsTrack track) {
        return trackService.save(track);
    }

    @GetMapping("/courier/{courierId}")
    public List<LogisticsTrack> getTrackByCourierId(@PathVariable String courierId) {
        return trackService.findByCourierId(courierId);
    }

    @PostMapping("/status-change")
    public LogisticsTrack createStatusChangeTrack(
            @RequestParam String orderId,
            @RequestParam OrderStatus status,
            @RequestParam String location,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam String operatorId,
            @RequestParam String operatorName,
            @RequestParam String operatorType,
            @RequestParam String description) {
        
        return trackService.createStatusChangeTrack(
                orderId, status, location, latitude, longitude,
                operatorId, operatorName, operatorType, description);
    }

    @GetMapping("/operator/{operatorId}")
    public List<LogisticsTrack> getTracksByOperatorId(@PathVariable String operatorId) {
        return trackService.getTracksByOperatorId(operatorId);
    }
} 