package com.logistics.tracking.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "courier_ratings")
public class CourierRating {
    @Id
    private String id;
    
    // 关联信息
    private String courierId;
    private String orderId;
    private String userId;  // 评分用户ID（寄件人或收件人）
    
    // 评分信息
    private int rating;           // 评分（1-5分）
    private String comment;       // 评价内容
    private boolean isComplaint;  // 是否是投诉
    
    // 时间信息
    private LocalDateTime createTime;
} 