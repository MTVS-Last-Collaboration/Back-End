package com.loveforest.loveforest.domain.pet.controller;

import com.loveforest.loveforest.domain.auth.dto.LoginInfo;
import com.loveforest.loveforest.domain.couple.entity.Couple;
import com.loveforest.loveforest.domain.couple.service.CoupleService;
import com.loveforest.loveforest.domain.pet.dto.PetResponseDTO;
import com.loveforest.loveforest.domain.pet.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
@Tag(name = "Pet API", description = "Pet 성장과 상태 관리 API")
public class PetController {

    private final PetService petService;
    private final CoupleService coupleService;


    /**
     * 커플의 팻 상태 조회
     */
    @Operation(summary = "펫 상태 조회", description = "커플의 팻 상태를 조회합니다. " +
            "레벨과 경험치 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "팻 상태 조회 성공"),
            @ApiResponse(responseCode = "404", description = "커플 또는 팻을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근")
    })
    @GetMapping
    public ResponseEntity<PetResponseDTO> getPetStatus(@AuthenticationPrincipal LoginInfo loginInfo) {

        Couple couple = coupleService.getCoupleByUserId(loginInfo.getUserId());
        PetResponseDTO petStatus = petService.getPetStatus(couple);

        return ResponseEntity.ok(petStatus);
    }

    /**
     * 팻 경험치 추가 (1일 1문답 완료 시 호출)
     */
    @Operation(summary = "펫 경험치 추가", description = "커플이 1일 1문답을 완료하여 팻에 경험치를 추가합니다. " +
            "팻이 경험치 100을 채우면 자동으로 레벨이 상승합니다. 최대 레벨에 도달한 경우 오류가 발생합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "경험치 추가 성공"),
            @ApiResponse(responseCode = "404", description = "팻을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "400", description = "팻의 최대 레벨에 도달하여 경험치를 추가할 수 없습니다."),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근")
    })
    @PostMapping("/add-exp")
    public ResponseEntity<PetResponseDTO> addPetExperience(@AuthenticationPrincipal LoginInfo loginInfo) {

        Couple couple = coupleService.getCoupleByUserId(loginInfo.getUserId());
        petService.addExperience(couple, 10); // 경험치 10 추가
        PetResponseDTO updatedPetStatus = petService.getPetStatus(couple);

        return ResponseEntity.ok(updatedPetStatus);
    }
}
