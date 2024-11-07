package com.loveforest.loveforest.domain.pet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "팻 이름 변경 요청 DTO")
public class PetNameUpdateRequestDTO {

    @Schema(description = "변경할 팻 이름", example = "몽이")
    @NotBlank(message = "팻 이름은 필수입니다.")
    @Size(min = 1, max = 20, message = "팻 이름은 1-20자 사이여야 합니다.")
    private String name;
}
