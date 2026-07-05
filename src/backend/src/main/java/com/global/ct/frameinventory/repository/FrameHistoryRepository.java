package com.global.ct.frameinventory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.global.ct.frameinventory.domain.FrameHistory;

public interface FrameHistoryRepository extends MongoRepository<FrameHistory, String> {

    Page<FrameHistory> findByFrameIdOrderByChangedAtDesc(String frameId, Pageable pageable);
}
