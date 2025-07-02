package com.logistics.tracking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "amap")
public class AmapConfig {
    private String key;  // 高德地图API密钥
    private String geocodeUrl = "https://restapi.amap.com/v3/geocode/geo";  // 地理编码接口
    private String regeoUrl = "https://restapi.amap.com/v3/geocode/regeo";  // 逆地理编码接口
    private String distanceUrl = "https://restapi.amap.com/v3/distance";     // 距离测量接口
    private String drivingUrl = "https://restapi.amap.com/v3/direction/driving"; // 驾车路径规划接口
    private String directionUrl;  // 路径规划URL
} 