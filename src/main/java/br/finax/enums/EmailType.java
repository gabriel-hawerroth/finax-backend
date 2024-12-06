package br.finax.enums;

import lombok.Getter;

@Getter
public enum EmailType {
    ACTIVATE_ACCOUNT("activate-account"),
    CHANGE_PASSWORD("permit-change-password"),
    SYSTEM_UPDATE("system-update");

    private final String value;

    EmailType(String value) {
        this.value = value;
    }
}
