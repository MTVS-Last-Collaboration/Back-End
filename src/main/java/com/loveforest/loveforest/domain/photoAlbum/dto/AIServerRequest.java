package com.loveforest.loveforest.domain.photoAlbum.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AIServerRequest {
    private String base64Image;
    private Double positionX;
    private Double positionY;

    public AIServerRequest(String base64Image, Double positionX, Double positionY) {
        this.base64Image = base64Image;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    // getter/setter
    public String getBase64Image() { return base64Image; }
    public Double getPositionX() { return positionX; }
    public Double getPositionY() { return positionY; }
}
