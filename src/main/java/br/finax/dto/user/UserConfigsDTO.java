package br.finax.dto.user;

import br.finax.enums.user_configs.UserConfigsReleasesViewMode;
import br.finax.enums.user_configs.UserConfigsTheme;
import br.finax.models.UserConfigs;

public record UserConfigsDTO(
        Long id,
        UserConfigsTheme theme,
        boolean addingMaterialGoodsToPatrimony,
        String language,
        String currency,
        UserConfigsReleasesViewMode releasesViewMode,
        boolean emailNotifications
) {
    public static UserConfigsDTO fromEntity(UserConfigs userConfigs) {
        return new UserConfigsDTO(
                userConfigs.getId(),
                userConfigs.getTheme(),
                userConfigs.isAddingMaterialGoodsToPatrimony(),
                userConfigs.getLanguage(),
                userConfigs.getCurrency(),
                userConfigs.getReleasesViewMode(),
                userConfigs.isEmailNotifications()
        );
    }

    public UserConfigs toEntity() {
        var entity = new UserConfigs();
        entity.setId(this.id);
        entity.setTheme(this.theme);
        entity.setAddingMaterialGoodsToPatrimony(this.addingMaterialGoodsToPatrimony);
        entity.setLanguage(this.language);
        entity.setCurrency(this.currency);
        entity.setReleasesViewMode(this.releasesViewMode);
        entity.setEmailNotifications(this.emailNotifications);
        return entity;
    }
}
