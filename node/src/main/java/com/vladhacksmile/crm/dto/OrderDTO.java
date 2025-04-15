package com.vladhacksmile.crm.dto;

import com.vladhacksmile.crm.jdbc.order.OrderItem;
import com.vladhacksmile.crm.jdbc.order.OrderStatus;
import lombok.*;

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

    /**
     * Общая сумма (Telegram)
     */
    private Integer totalAmount;

    /**
     * Платежный id Telegram
     */
    private String telegramPaymentChargeId;

    /**
     * Платежный id провайдера
     */
    private String providerPaymentChargeId;

    /**
     * Опциональный id Telegram
     */
    private String shippingOptionId;

    /**
     * Комментарий к заказу
     */
    private String comment;

    /**
     * Платежная информация
     */
    private String paymentInfo;
}
