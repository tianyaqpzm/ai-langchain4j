package com.zhuchl.ailangchain4j.module.timekeeper.repository;

import com.zhuchl.ailangchain4j.module.timekeeper.entity.TimeLimitedEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeLimitedEventRepository extends MongoRepository<TimeLimitedEvent, String> {
}
