package com.vladhacksmile.crm.jdbc.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Позиция заказа
 */
@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    /**
     * ID продукта
     */
    @JsonProperty("productId")
    private Long productId;

    /**
     * Количество
     */
    @JsonProperty("count")
    private int count;

    /**
     * Цена позиции по чеку (даже включая акции, промо и т п)
     */
    @JsonProperty("price")
    private long price;
}