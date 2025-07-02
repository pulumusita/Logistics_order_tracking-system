package com.logistics.tracking.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.tracking.config.AmapConfig;
import com.logistics.tracking.model.GeoLocation;
import com.logistics.tracking.model.Order;
import com.logistics.tracking.service.MapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapServiceImpl implements MapService {

    @Value("${amap.key}")
    private String amapKey;

    @Value("${amap.geocodeUrl}")
    private String geocodeUrl;

    @Value("${amap.regeoUrl}")
    private String regeoUrl;

    @Value("${amap.routeUrl}")
    private String routeUrl;

    private final AmapConfig amapConfig;
    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String GEOCODE_CACHE_PREFIX = "geocode:";
    private static final long CACHE_DURATION = 24; // 缓存24小时

    @Override
    @Cacheable(value = "geocodeCache", key = "#address")
    public Map<String, Double> getLocation(String address) {
        String cacheKey = GEOCODE_CACHE_PREFIX + address;
        
        // 先从Redis缓存中获取
        @SuppressWarnings("unchecked")
        Map<String, Double> cachedLocation = (Map<String, Double>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedLocation != null) {
            log.debug("从缓存中获取地理编码 - 地址: {}", address);
            return cachedLocation;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(amapConfig.getGeocodeUrl())
                    .queryParam("key", amapConfig.getKey())
                    .queryParam("address", address)
                    .build()
                    .toUriString();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "1".equals(response.get("status"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> geocode = ((java.util.List<Map<String, Object>>) response.get("geocodes")).get(0);
                String[] location = ((String) geocode.get("location")).split(",");
                
                Map<String, Double> result = new HashMap<>();
                result.put("longitude", Double.parseDouble(location[0]));
                result.put("latitude", Double.parseDouble(location[1]));

                // 将结果存入Redis缓存
                redisTemplate.opsForValue().set(cacheKey, result, CACHE_DURATION, TimeUnit.HOURS);
                log.debug("地理编码结果已缓存 - 地址: {}", address);

                return result;
            } else {
                String errorCode = response != null ? (String) response.get("infocode") : "unknown";
                log.error("地理编码请求失败: {}", errorCode);
                return null;
            }
        } catch (Exception e) {
            log.error("地理编码请求异常", e);
            return null;
        }
    }

    @Override
    public GeoLocation geocode(String address) {
        Map<String, Double> location = getLocation(address);
        if (location != null) {
            return GeoLocation.builder()
                    .latitude(location.get("latitude"))
                    .longitude(location.get("longitude"))
                    .address(address)
                    .build();
        }
        return null;
    }

    @Override
    public double calculateDistance(String startAddress, String endAddress) {
        Map<String, Double> startLocation = getLocation(startAddress);
        Map<String, Double> endLocation = getLocation(endAddress);

        if (startLocation != null && endLocation != null) {
            return calculateDistance(
                    startLocation.get("latitude"),
                    startLocation.get("longitude"),
                    endLocation.get("latitude"),
                    endLocation.get("longitude")
            );
        }

        log.error("获取地理编码失败 - 起点: {}, 终点: {}", 
            startLocation != null ? "成功" : "失败",
            endLocation != null ? "成功" : "失败");
        return 0;
    }

    @Override
    public double calculateDistance(double startLat, double startLng, double endLat, double endLng) {
        final int R = 6371; // 地球半径（千米）

        double latDistance = Math.toRadians(endLat - startLat);
        double lngDistance = Math.toRadians(endLng - startLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000; // 转换为米
    }

    @Override
    @Cacheable(value = "routeCache", key = "#order.id")
    public Map<String, Object> getDeliveryRoute(Order order) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(amapConfig.getDirectionUrl())
                    .queryParam("key", amapConfig.getKey())
                    .queryParam("origin", getLocationString(order.getSenderAddress()))
                    .queryParam("destination", getLocationString(order.getReceiverAddress()))
                    .build()
                    .toUriString();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && "1".equals(response.get("status"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> route = (Map<String, Object>) response.get("route");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> paths = (List<Map<String, Object>>) route.get("paths");
                if (paths != null && !paths.isEmpty()) {
                    Map<String, Object> path = paths.get(0);
                    
                    // 获取总距离
                    double distance = Double.parseDouble(String.valueOf(path.get("distance")));
                    path.put("distance", distance);
                    
                    // 缓存路线信息
                    String cacheKey = "route:" + order.getId();
                    redisTemplate.opsForValue().set(cacheKey, path, CACHE_DURATION, TimeUnit.HOURS);
                    
                    return path;
                }
            }
            return new HashMap<>();
        } catch (Exception e) {
            log.error("获取配送路线失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    private String getLocationString(String address) {
        Map<String, Double> location = getLocation(address);
        if (location != null) {
            return location.get("longitude") + "," + location.get("latitude");
        }
        return "";
    }

    @Override
    public String reverseGeocode(double latitude, double longitude) {
        try {
            String url = String.format("%s?location=%f,%f&key=%s",
                regeoUrl, longitude, latitude, amapKey
            );
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (!"1".equals(root.get("status").asText())) {
                log.error("逆地理编码请求失败: {}", root.get("info").asText());
                return null;
            }

            return root.get("regeocode").get("formatted_address").asText();
        } catch (Exception e) {
            log.error("逆地理编码异常: {}", e.getMessage(), e);
            return null;
        }
    }
} 