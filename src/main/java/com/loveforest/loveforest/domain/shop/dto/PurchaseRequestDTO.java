package com.loveforest.loveforest.domain.shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "아이템 구매 요청 DTO")
public class PurchaseRequestDTO {
    @Schema(description = "구매할 아이템 ID", example = "1", required = true)
    @NotNull(message = "아이템 ID는 필수입니다.")
    private Long itemId;
}