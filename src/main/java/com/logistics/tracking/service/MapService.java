package com.logistics.tracking.service;

import com.logistics.tracking.model.GeoLocation;
import com.logistics.tracking.model.Order;
import java.util.Map;

public interface MapService {
    /**
     * 地理编码：将地址转换为经纬度坐标
     */
    GeoLocation geocode(String address);

    /**
     * 计算两个地点之间的距离（单位：米）
     */
    double calculateDistance(String startAddress, String endAddress);

    /**
     * 计算两个经纬度坐标之间的距离（单位：米）
     */
    double calculateDistance(double startLat, double startLng, double endLat, double endLng);

    /**
     * 获取配送路线信息
     */
    Map<String, Object> getDeliveryRoute(Order order);

    /**
     * 逆地理编码：将经纬度转换为地址
     */
    String reverseGeocode(double latitude, double longitude);

    /**
     * 获取地址的经纬度坐标
     */
    Map<String, Double> getLocation(String address);
} 