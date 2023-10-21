package br.finax.finax.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "user_configs")
public class UserConfigs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String theme;

    @Column(name = "adding_material_goods_to_patrimony")
    private Boolean addingMaterialGoodsToPatrimony;

    private String language;

    private String currency;
}
