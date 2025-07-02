package com.logistics.tracking.controller;

import com.logistics.tracking.model.Order;
import com.logistics.tracking.model.GeoLocation;
import com.logistics.tracking.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @GetMapping("/geocode")
    public ResponseEntity<Map<String, Double>> geocodeAddress(@RequestParam String address) {
        return ResponseEntity.ok(mapService.getLocation(address));
    }

    @GetMapping("/geocode/detail")
    public ResponseEntity<GeoLocation> geocodeAddressDetail(@RequestParam String address) {
        GeoLocation location = mapService.geocode(address);
        return location != null ? ResponseEntity.ok(location) : ResponseEntity.notFound().build();
    }

    @GetMapping("/distance")
    public ResponseEntity<Double> calculateDistance(
            @RequestParam String startAddress,
            @RequestParam String endAddress) {
        return ResponseEntity.ok(mapService.calculateDistance(startAddress, endAddress));
    }

    @GetMapping("/distance/coordinates")
    public ResponseEntity<Double> calculateDistanceByCoordinates(
            @RequestParam double startLat,
            @RequestParam double startLng,
            @RequestParam double endLat,
            @RequestParam double endLng) {
        return ResponseEntity.ok(mapService.calculateDistance(startLat, startLng, endLat, endLng));
    }

    @GetMapping("/route/{orderId}")
    public ResponseEntity<Map<String, Object>> getDeliveryRoute(@PathVariable String orderId) {
        Order order = new Order(); // 这里需要通过OrderService获取订单信息
        return ResponseEntity.ok(mapService.getDeliveryRoute(order));
    }

    @GetMapping("/reverse-geocode")
    public ResponseEntity<String> reverseGeocode(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return ResponseEntity.ok(mapService.reverseGeocode(latitude, longitude));
    }
} 