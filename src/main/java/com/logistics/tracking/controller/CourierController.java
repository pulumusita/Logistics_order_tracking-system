package com.logistics.tracking.controller;

import com.logistics.tracking.model.Courier;
import com.logistics.tracking.model.CourierRating;
import com.logistics.tracking.service.CourierPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/couriers")
@RequiredArgsConstructor
public class CourierController {

    private final CourierPerformanceService courierService;

    @PostMapping
    public ResponseEntity<Courier> createCourier(@RequestBody Courier courier) {
        return ResponseEntity.ok(courierService.createCourier(courier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Courier> updateCourier(@PathVariable String id, @RequestBody Courier courier) {
        courier.setId(id);
        return ResponseEntity.ok(courierService.updateCourier(courier));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourier(@PathVariable String id) {
        courierService.deleteCourier(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Courier> getCourier(@PathVariable String id) {
        Courier courier = courierService.getCourierById(id);
        return courier != null ? ResponseEntity.ok(courier) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Courier>> getAllActiveCouriers() {
        return ResponseEntity.ok(courierService.getAllActiveCouriers());
    }

    @PostMapping("/{id}/ratings")
    public ResponseEntity<CourierRating> addRating(@PathVariable String id, @RequestBody CourierRating rating) {
        rating.setCourierId(id);
        return ResponseEntity.ok(courierService.addRating(rating));
    }

    @GetMapping("/{id}/ratings")
    public ResponseEntity<List<CourierRating>> getCourierRatings(@PathVariable String id) {
        return ResponseEntity.ok(courierService.getCourierRatings(id));
    }

    @GetMapping("/{id}/daily-stats")
    public ResponseEntity<Map<String, Object>> getDailyStats(@PathVariable String id) {
        return ResponseEntity.ok(courierService.getDailyStats(id));
    }

    @GetMapping("/{id}/monthly-stats")
    public ResponseEntity<Map<String, Object>> getMonthlyStats(@PathVariable String id) {
        return ResponseEntity.ok(courierService.getMonthlyStats(id));
    }

    @GetMapping("/{id}/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceReport(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(courierService.getPerformanceReport(id, startTime, endTime));
    }

    @PutMapping("/{id}/online-status")
    public ResponseEntity<Void> updateOnlineStatus(@PathVariable String id, @RequestParam boolean online) {
        courierService.updateOnlineStatus(id, online);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/online")
    public ResponseEntity<List<Courier>> getOnlineCouriers() {
        return ResponseEntity.ok(courierService.getOnlineCouriers());
    }

    @GetMapping("/work-area/{area}")
    public ResponseEntity<List<Courier>> getCouriersByWorkArea(@PathVariable String area) {
        return ResponseEntity.ok(courierService.getCouriersByWorkArea(area));
    }

    @PutMapping("/{id}/work-area")
    public ResponseEntity<Void> updateWorkArea(@PathVariable String id, @RequestParam String workArea) {
        courierService.updateWorkArea(id, workArea);
        return ResponseEntity.ok().build();
    }
} 