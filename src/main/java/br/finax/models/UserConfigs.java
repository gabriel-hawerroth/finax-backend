package br.finax.models;

import br.finax.enums.user_configs.UserConfigsReleasesViewMode;
import br.finax.enums.user_configs.UserConfigsTheme;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "user_configs")
public class UserConfigs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserConfigsTheme theme;

    @Column(name = "adding_material_goods_to_patrimony", nullable = false)
    private boolean addingMaterialGoodsToPatrimony;

    @NotBlank
    @Column(nullable = false, length = 5)
    private String language;

    @NotBlank
    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "releases_view_mode", nullable = false)
    private UserConfigsReleasesViewMode releasesViewMode;

    @Column(name = "email_notifications", nullable = false, columnDefinition = "bool default true")
    private boolean emailNotifications;
}
