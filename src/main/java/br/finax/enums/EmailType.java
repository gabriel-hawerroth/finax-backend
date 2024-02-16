package br.finax.enums;

public enum EmailType {
    ACTIVATE_ACCOUNT("activate-account"),
    CHANGE_PASSWORD("permit-change-password");

    private final String value;

    EmailType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
