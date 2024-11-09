package com.loveforest.loveforest.domain.shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "아이템 구매 응답 DTO")
public class PurchaseResponseDTO {
    @Schema(description = "구매 ID", example = "1")
    private Long purchaseId;

    @Schema(description = "구매한 아이템 이름", example = "클래식 소파")
    private String itemName;

    @Schema(description = "지불한 가격", example = "1000")
    private int pricePaid;

    @Schema(description = "구매 일시", example = "2024-03-15T14:30:00")
    private LocalDateTime purchasedAt;
}