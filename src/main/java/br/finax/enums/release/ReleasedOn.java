package br.finax.enums.release;

import lombok.Getter;

@Getter
public enum ReleasedOn {
    ACCOUNT("account"),
    CREDIT_CARD("credit_card");

    private final String value;

    ReleasedOn(String value) {
        this.value = value;
    }
}
