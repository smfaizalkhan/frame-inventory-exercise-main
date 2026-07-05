package com.global.ct.frameinventory.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.global.ct.frameinventory.domain.Frame;

public interface FrameRepository extends MongoRepository<Frame, String> {

    boolean existsByFrameReference(String frameReference);

    Optional<Frame> findByFrameReference(String frameReference);
}
