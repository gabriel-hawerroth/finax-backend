package br.finax.enums;

public enum ImgFormat {
    JPG("jpg");

    private final String imgFormat;

    ImgFormat(String imgFormat) {
        this.imgFormat = imgFormat;
    }

    public String getFormat() {
        return imgFormat;
    }
}
