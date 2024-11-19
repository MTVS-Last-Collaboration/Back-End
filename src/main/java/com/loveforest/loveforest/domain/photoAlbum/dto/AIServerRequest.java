package com.loveforest.loveforest.domain.photoAlbum.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AIServerRequest {
    private String base64Image;
    private int positionX;
    private int positionY;

    public AIServerRequest(String base64Image, int positionX, int positionY) {
        this.base64Image = base64Image;
        this.positionX = positionX;
        this.positionY = positionY;
    }
}
