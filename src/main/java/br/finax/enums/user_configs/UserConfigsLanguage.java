package br.finax.enums.user_configs;

import lombok.Getter;

@Getter
public enum UserConfigsLanguage {
    PT_BR("pt-BR"),
    EN_US("en-US"),
    ES_CO("es-CO"),
    DE_DE("de-DE");

    private final String language;

    UserConfigsLanguage(String language) {
        this.language = language;
    }
}
