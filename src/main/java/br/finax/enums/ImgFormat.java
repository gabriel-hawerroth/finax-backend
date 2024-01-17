package br.finax.enums;

import lombok.Getter;

@Getter
public enum ImgFormat {
    JPG("jpg");

    private final String imgFormat;

    ImgFormat(String imgFormat) {
        this.imgFormat = imgFormat;
    }
}
