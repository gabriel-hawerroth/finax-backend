package br.finax.models;

import br.finax.enums.user.UserTheme;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private UserTheme theme;

    @Column(name = "adding_material_goods_to_patrimony", nullable = false)
    private boolean addingMaterialGoodsToPatrimony;

    @NotBlank
    @Column(nullable = false, length = 5)
    private String language;

    @NotBlank
    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "releases_view_mode", nullable = false, length = 8)
    private String releasesViewMode;
}
