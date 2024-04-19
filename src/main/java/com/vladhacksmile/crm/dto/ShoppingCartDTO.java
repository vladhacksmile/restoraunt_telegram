package com.vladhacksmile.crm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladhacksmile.crm.converter.OrderItemConverter;
import com.vladhacksmile.crm.jdbc.OrderItem;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Convert;
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
