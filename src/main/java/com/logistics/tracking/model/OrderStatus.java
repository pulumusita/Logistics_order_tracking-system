package com.logistics.tracking.model;

public enum OrderStatus {
    PENDING("待处理"),
    DISPATCHED("已派发"),
    PICKED_UP("已取件"),
    IN_TRANSIT("运输中"),
    DELIVERED("已送达");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 