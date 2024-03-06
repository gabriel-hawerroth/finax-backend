package br.finax.enums;

import lombok.Getter;

@Getter
public enum ReleasesViewMode {
    invoice("invoice"),
    releases("releases");

    private final String value;

    ReleasesViewMode(String value) {
        this.value = value;
    }
}
