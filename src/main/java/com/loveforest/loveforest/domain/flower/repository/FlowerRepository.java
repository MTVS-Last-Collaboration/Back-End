package com.loveforest.loveforest.domain.flower.repository;

import com.loveforest.loveforest.domain.flower.entity.Flower;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface FlowerRepository extends JpaRepository<Flower, Long> {
    Optional<Flower> findByUserId(Long userId);
}
