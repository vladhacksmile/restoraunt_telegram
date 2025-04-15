package com.vladhacksmile.crm.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ProductDTO {

    /**
     * Идентификатор продукта
     */
    private Long id;

    /**
     * Количество единиц продукта на позицию
     */
    // default 1
    private Long count;

    /**
     * Наименование товара
     */
    private String name;

    /**
     * Описание товара
     */
    // nullable
    private String description;

    /**
     * ID картинки
     */
    // not null
    private String pictureId;

    /**
     * Цена продукта
     */
    // not null
    private Integer price;

    /**
     * Вес в граммах
     */
    // not null
    private Integer weight;

    /**
     * Калории
     */
    // not null, default 0
    private Integer calories;

    /**
     * Углеводы
     */
    // not null, default 0
    private Integer nutritional;

    /**
     * Белки
     */
    // not null, default 0
    private Integer proteins;

    /**
     * Жиры
     */
    private Integer fats;
}
