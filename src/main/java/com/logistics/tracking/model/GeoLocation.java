package com.logistics.tracking.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocation {
    private Double longitude; // 经度
    private Double latitude;  // 纬度
    private String address;   // 地址
    private String district; // 区域
    private String city;     // 城市
    private String province; // 省份
} 