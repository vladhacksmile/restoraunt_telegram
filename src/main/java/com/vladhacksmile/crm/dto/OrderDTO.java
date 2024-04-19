package com.vladhacksmile.crm.dto;

import com.vladhacksmile.crm.converter.OrderItemConverter;
import com.vladhacksmile.crm.jdbc.OrderItem;
import com.vladhacksmile.crm.jdbc.OrderStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OrderDTO {

    /**
     * Идентификатор
     */
    private Long id;

    /**
     * Идентификатор пользователя
     */
    private Long userId;

    /**
     * Идентификатор ресторана
     */
    private Long restaurantId;

    /**
     * Список заказанных позиций
     */
    private List<OrderItem> orderItems;

    /**
     * Статус заказа
     */
    private OrderStatus orderStatus;

    /**
     * Дата смена статуса
     */
    private LocalDateTime orderStatusChanged;

    /**
     * Дата заказа
     */
    private LocalDateTime orderDate;
}
