package com.loveforest.loveforest.domain.shop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "구매한 아이템 조회 응답 DTO")
public class PurchasedItemResponseDTO {
    @Schema(description = "아이템 ID", example = "1")
    private Long itemId;

    @Schema(description = "아이템 타입", example = "FURNITURE")
    private String itemType;

    @Schema(description = "아이템 이름", example = "클래식 소파")
    private String itemName;

    @Schema(description = "구매 일시", example = "2024-03-15T14:30:00")
    private LocalDateTime purchasedAt;

    @Schema(description = "지불한 가격", example = "1000")
    private int pricePaid;

    // 가구 전용 필드
    @Schema(description = "가구 너비", example = "2")
    private Integer width;

    @Schema(description = "가구 높이", example = "3")
    private Integer height;

    // 벽지, 바닥 전용 필드
    @Schema(description = "아이템 번호", example = "1")
    private Integer number;
}