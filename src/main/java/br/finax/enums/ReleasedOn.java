package br.finax.enums;

import lombok.Getter;

@Getter
public enum ReleasedOn {
    ACCOUNT("account"),
    CREDIT_CARD("credit_card");

    private final String realesedOn;

    ReleasedOn(String realesedOn) {
        this.realesedOn = realesedOn;
    }
}
