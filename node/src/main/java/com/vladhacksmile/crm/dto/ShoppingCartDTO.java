package com.vladhacksmile.crm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladhacksmile.crm.jdbc.order.OrderItem;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ShoppingCartDTO {

    /**
     * Идентификатор
     */
    @JsonProperty
    private Long id;

    /**
     * Идентификатор пользователя
     */
    @JsonProperty
    private Long userId;

    /**
     * Список позиций в корзине
     */
    @JsonProperty("orderItems")
    private List<OrderItem> orderItems;
}
