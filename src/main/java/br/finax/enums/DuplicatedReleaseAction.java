package br.finax.enums;

import lombok.Getter;

@Getter
public enum DuplicatedReleaseAction {
    ALL("all"),
    NEXTS("nexts"),
    JUST_THIS("just-this"),
    UNNECESSARY("");

    private final String value;

    DuplicatedReleaseAction(String value) {
        this.value = value;
    }
}
