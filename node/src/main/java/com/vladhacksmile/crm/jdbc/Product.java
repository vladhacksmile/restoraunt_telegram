package com.vladhacksmile.crm.jdbc;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "Product")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Product {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Количество единиц продукта на позицию
     */
    // default 1
    @Column(name = "count", nullable = false)
    private Long count;

    /**
     * Наименование товара
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Описание товара
     */
    // nullable
    @Column(name = "description")
    private String description;

    /**
     * ID картинки
     */
    // not null
    @Column(name = "pictureId", nullable = false)
    private String pictureId;

    /**
     * Цена продукта
     */
    // not null
    @Column(name = "price", nullable = false)
    private Integer price;

    /**
     * Вес в граммах
     */
    // not null
    @Column(name = "weight", nullable = false)
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
    @Column(name = "nutritional", nullable = false)
    private Integer nutritional;

    /**
     * Белки
     */
    // not null, default 0
    @Column(name = "proteins", nullable = false)
    private Integer proteins;

    /**
     * Жиры
     */
    // not null, default 0
    @Column(name = "fats", nullable = false)
    private Integer fats;

    @Column(name = "deletedDate")
    private LocalDateTime deletedDate;
}
