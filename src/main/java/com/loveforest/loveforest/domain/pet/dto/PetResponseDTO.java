package com.loveforest.loveforest.domain.pet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Pet의 현재 상태를 나타내는 응답 DTO")
public class PetResponseDTO {

    @Schema(description = "팻 이름", example = "몽이")
    private String name;

    @Schema(description = "현재 Pet의 레벨", example = "5", minimum = "1", maximum = "20")
    private int level;

    @Schema(description = "현재 레벨 내에서의 Pet 경험치", example = "50", minimum = "0", maximum = "100")
    private int experience;
}