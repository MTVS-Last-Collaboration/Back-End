package com.loveforest.loveforest.domain.room.dto;

import com.loveforest.loveforest.domain.room.entity.Floor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "바닥 정보 DTO")
@Getter
@Builder
public class FloorDTO {
    @Schema(description = "바닥 ID", example = "1")
    private Long id;

    @Schema(description = "바닥 이름", example = "원목 바닥")
    private String name;

    @Schema(description = "바닥 번호", example = "1")
    private int floorNumber;

    public static FloorDTO from(Floor floor) {
        if (floor == null) {
            return null;
        }

        return FloorDTO.builder()
                .id(floor.getId())
                .name(floor.getName())
                .floorNumber(floor.getFloorNumber())
                .build();
    }
}
