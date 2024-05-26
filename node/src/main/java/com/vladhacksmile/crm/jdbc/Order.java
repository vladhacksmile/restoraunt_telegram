package com.vladhacksmile.crm.jdbc;

import com.vladhacksmile.crm.converter.OrderItemConverter;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
     * Общая
     */
    @Column(name = "totalAmount")
    private Integer totalAmount;

    @Column(name = "telegramPaymentChargeId")
    private String telegramPaymentChargeId;

    @Column(name = "providerPaymentChargeId")
    private String providerPaymentChargeId;

    @Column(name = "shippingOptionId")
    private String shippingOptionId;
}
