package com.vladhacksmile.crm.jdbc.order;

import com.vladhacksmile.crm.converter.OrderItemConverter;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "UserOrder")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Order {

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
     * Идентификатор исполнителя
     */
    @Column(name = "makerId")
    private Long makerId;

    /**
     * Идентификатор ресторана
     */
    @Column(name = "restaurantId", nullable = false)
    private Long restaurantId;

    /**
     * Список заказанных позиций
     */
    @Column(name = "orderItems")
    @Convert(converter = OrderItemConverter.class)
    private List<OrderItem> orderItems;

    /**
     * Статус заказа
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "orderStatus", nullable = false)
    private OrderStatus orderStatus;

    /**
     * Дата смена статуса
     */
    private LocalDateTime orderStatusChanged;

    /**
     * Дата заказа
     */
    @Column(name = "orderDate", nullable = false)
    private LocalDateTime orderDate;

    /**
     * Общая сумма
     */
    @Column(name = "totalAmount")
    private Integer totalAmount;

    /**
     * Платежный id Telegram
     */
    @Column(name = "telegramPaymentChargeId")
    private String telegramPaymentChargeId;

    /**
     * Платежный id провайдера
     */
    @Column(name = "providerPaymentChargeId")
    private String providerPaymentChargeId;

    /**
     * Опциональный id Telegram
     */
    @Column(name = "shippingOptionId")
    private String shippingOptionId;

    /**
     * Комментарий к заказу
     */
    @Column(name = "comment")
    private String comment;

    /**
     * Платежная информация
     */
    @Column(name = "paymentInfo")
    private String paymentInfo;

    /**
     * Дополнительная информация
     */
    @Column(name = "info")
    private String info;
}
