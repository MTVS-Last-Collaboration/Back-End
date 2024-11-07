package com.loveforest.loveforest.domain.pet.repository;

import com.loveforest.loveforest.domain.pet.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {
    Optional<Pet> findByCoupleId(Long id);
}
