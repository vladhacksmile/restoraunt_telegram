package com.vladhacksmile.crm.jdbc;

import com.vladhacksmile.crm.converter.OrderItemConverter;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity(name = "ShoppingCart")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ShoppingCart {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Идентификатор пользователя
     */
    @Column(name = "userId", nullable = false)
    private Long userId;

    /**
     * Список позиций в корзине
     */
    @Column(name = "orderItems")
    @Convert(converter = OrderItemConverter.class)
    private List<OrderItem> orderItems;

}
