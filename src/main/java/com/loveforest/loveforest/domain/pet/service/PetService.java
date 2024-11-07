package com.loveforest.loveforest.domain.pet.service;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.pet.dto.PetResponseDTO;

public interface PetService {
    PetResponseDTO getPetStatus(Couple couple);
    void addExperience(Couple couple, int exp);
    void createPetForCouple(Couple couple);
}
