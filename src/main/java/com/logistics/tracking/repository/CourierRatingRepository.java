package com.logistics.tracking.repository;

import com.logistics.tracking.model.CourierRating;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourierRatingRepository extends MongoRepository<CourierRating, String> {
    List<CourierRating> findByCourierIdOrderByCreateTimeDesc(String courierId);
    List<CourierRating> findByCourierIdAndCreateTimeAfter(String courierId, LocalDateTime startTime);
    List<CourierRating> findByCourierIdAndCreateTimeBetween(String courierId, LocalDateTime startTime, LocalDateTime endTime);
} 