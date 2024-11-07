package com.loveforest.loveforest.domain.pet.service.impl;

import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.pet.dto.PetResponseDTO;
import com.loveforest.loveforest.domain.pet.entity.Pet;
import com.loveforest.loveforest.domain.pet.exception.MaxLevelReachedException;
import com.loveforest.loveforest.domain.pet.exception.PetNotFoundException;
import com.loveforest.loveforest.domain.pet.repository.PetRepository;
import com.loveforest.loveforest.domain.pet.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;

    @Override
    public PetResponseDTO getPetStatus(Couple couple) {
        Pet pet = petRepository.findByCoupleId(couple.getId())
                .orElseThrow(PetNotFoundException::new);
        return new PetResponseDTO(pet.getName(), pet.getLevel(), pet.getExperience());
    }

    @Override
    @Transactional
    public void addExperience(Couple couple, int exp) {
        Pet pet = petRepository.findByCoupleId(couple.getId())
                .orElseThrow(PetNotFoundException::new);

        // 최대 레벨 확인
        if (pet.getLevel() >= 20) {
            throw new MaxLevelReachedException();
        }

        // 경험치 추가 및 레벨업 처리
        pet.addExperience(exp);
        if (pet.getExperience() >= 100) {
            pet.incrementLevel();
            pet.resetExperience();
        }

        petRepository.save(pet);
    }

    @Override
    @Transactional
    public void createPetForCouple(Couple couple) {
        Pet pet = new Pet(couple);
        petRepository.save(pet);
    }

    @Override
    @Transactional
    public PetResponseDTO updatePetName(Long coupleId, String newName) {
        Pet pet = petRepository.findByCoupleId(coupleId)
                .orElseThrow(PetNotFoundException::new);

        pet.updateName(newName);
        petRepository.save(pet);

        return new PetResponseDTO(pet.getName(), pet.getLevel(), pet.getExperience());
    }
}