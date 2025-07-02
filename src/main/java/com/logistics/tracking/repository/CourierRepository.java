package com.logistics.tracking.repository;

import com.logistics.tracking.model.Courier;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourierRepository extends MongoRepository<Courier, String> {
    List<Courier> findByActiveTrue();
    List<Courier> findByOnlineTrue();
    List<Courier> findByWorkAreaAndActiveTrue(String workArea);
} 