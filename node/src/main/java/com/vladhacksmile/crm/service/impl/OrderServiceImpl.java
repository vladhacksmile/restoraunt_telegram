package com.vladhacksmile.crm.service.impl;

import com.vladhacksmile.crm.dao.*;
import com.vladhacksmile.crm.dto.OrderDTO;
import com.vladhacksmile.crm.dto.ShoppingCartDTO;
import com.vladhacksmile.crm.utils.TelegramEmoji;
import com.vladhacksmile.crm.jdbc.*;
import com.vladhacksmile.crm.jdbc.order.Order;
import com.vladhacksmile.crm.jdbc.order.OrderItem;
import com.vladhacksmile.crm.jdbc.order.OrderStatus;
import com.vladhacksmile.crm.jdbc.user.Role;
import com.vladhacksmile.crm.jdbc.user.TelegramUser;
import com.vladhacksmile.crm.jdbc.user.User;
import com.vladhacksmile.crm.model.result.Result;
import com.vladhacksmile.crm.model.result.SearchResult;
import com.vladhacksmile.crm.service.OrderService;
import com.vladhacksmile.crm.service.auth.UserService;
import com.vladhacksmile.crm.service.dispatchers.ProducerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.vladhacksmile.crm.model.result.Result.resultOk;
import static com.vladhacksmile.crm.model.result.Result.resultWithStatus;
import static com.vladhacksmile.crm.model.result.SearchResult.makeSearchResult;
import static com.vladhacksmile.crm.model.result.status.Status.*;
import static com.vladhacksmile.crm.model.result.status.StatusDescription.*;
import static com.vladhacksmile.crm.utils.AuthUtils.checkAccess;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private ProducerService producerService;

    @Autowired
    private OrderDAO orderDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private RestaurantDAO restaurantDAO;

    @Autowired
    private ShoppingCartDAO shoppingCartDAO;

    @Autowired
    private UserService userService;

    @Autowired
    private TelegramUserDAO telegramUserDAO;

    @Override
    @Transactional
    public Result<OrderDTO> createOrder(User authUser, OrderDTO orderDTO) {
        LocalDateTime now = LocalDateTime.now();
        if (orderDTO == null) {
            return resultWithStatus(INCORRECT_PARAMS, ORDER_IS_NULL);
        }

        if (orderDTO.getId() != null) {
            return resultWithStatus(INCORRECT_PARAMS, ORDER_ID_IS_NOT_NULL);
        }

        Result<ShoppingCartDTO> getUserShoppingCartResult = userService.getUserShoppingCart(authUser, orderDTO.getUserId());
        if (getUserShoppingCartResult.isError()) {
            return getUserShoppingCartResult.cast();
        }

        orderDTO.setOrderItems(getUserShoppingCartResult.getObject().getOrderItems());

        Result<?> validateResult = validate(orderDTO);
        if (validateResult.isError()) {
            return validateResult.cast();
        }

        User user = userDAO.findById(orderDTO.getUserId()).orElse(null);
        if (user == null) {
            return resultWithStatus(NOT_FOUND, USER_NOT_FOUND);
        }

        Result<?> checkAccessResult = checkAccess(authUser, user);
        if (checkAccessResult.isError()) {
            return checkAccessResult.cast();
        }

        Restaurant restaurant = restaurantDAO.findById(orderDTO.getRestaurantId()).orElse(null);
        if (restaurant == null) {
            return resultWithStatus(NOT_FOUND, RESTAURANT_NOT_FOUND);
        }

        Order order = convert(orderDTO);
        for (OrderItem orderItem: order.getOrderItems()) {
            Product product = productDAO.findById(orderItem.getProductId()).orElse(null);
            if (product == null) {
                return resultWithStatus(NOT_FOUND, PRODUCT_NOT_FOUND);
            }

            // Цена ставится всегда из продукта (за 1 единицу)
            orderItem.setPrice(product.getPrice());
        }

        order.setOrderStatus(OrderStatus.NEW);
        order.setOrderStatusChanged(now);
        order.setOrderDate(now);

        orderDAO.save(order);

        ShoppingCart shoppingCart = shoppingCartDAO.findByUserId(order.getUserId()).orElse(null);
        if (shoppingCart != null) {
            shoppingCart.setOrderItems(Collections.emptyList());
            shoppingCartDAO.save(shoppingCart);
        }

        return resultOk(convert(order));
    }

    @Override
    @Transactional
    public Result<OrderDTO> updateOrderStatus(User authUser, Long orderId, OrderStatus orderStatus) {
        LocalDateTime now = LocalDateTime.now();
        if (orderId == null) {
            return resultWithStatus(INCORRECT_PARAMS, ORDER_ID_IS_NULL);
        }

        if (orderStatus == null) {
            return resultWithStatus(INCORRECT_PARAMS, ORDER_STATUS_IS_NULL);
        }

        Order order = orderDAO.findById(orderId).orElse(null);
        if (order == null) {
            return resultWithStatus(NOT_FOUND, ORDER_NOT_FOUND);
        }

        User user = userDAO.findById(order.getUserId()).orElse(null);
        if (user == null) {
            return resultWithStatus(NOT_FOUND, USER_NOT_FOUND);
        }

        Result<?> checkAccessResult = checkAccess(authUser, user);
        if (checkAccessResult.isError()) {
            return checkAccessResult.cast();
        }

        if (order.getMakerId() != null && !Objects.equals(authUser.getId(), order.getMakerId())) {
            return resultWithStatus(INCORRECT_STATE, ORDER_BUSY);
        }

        if (Objects.equals(order.getOrderStatus(), orderStatus)) {
            return resultWithStatus(INCORRECT_STATE, SAME_STATUSES);
        }

        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            return resultWithStatus(INCORRECT_STATE, ORDER_CANCELLED);
        }

        if (order.getOrderStatus() == OrderStatus.GIVEN) {
            return resultWithStatus(INCORRECT_STATE, ORDER_GIVEN);
        }

        if (order.getOrderStatus() == OrderStatus.NEW) {
            order.setMakerId(authUser.getId());
        }
        order.setOrderStatus(orderStatus);
        order.setOrderStatusChanged(now);
        orderDAO.save(order);

        TelegramUser telegramUser = telegramUserDAO.findByUserId(order.getUserId()).orElse(null);
        if (telegramUser != null) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(telegramUser.getChatId());
            sendMessage.setText(TelegramEmoji.BURGER + " Статус вашего заказа №" + order.getId() +
                    " обновлен! Новый статус заказа " + order.getOrderStatus() + "!" +
                    (order.getOrderStatus() == OrderStatus.READY ? (" Вы можете забрать его на кассе!" +
                            (authUser.getRole() != Role.CLIENT && StringUtils.isNotEmpty(order.getComment()) ?
                                    (" Комментарий к заказу: " + order.getComment()) : "")) : ""));
            producerService.producerAnswer(sendMessage);
        }

        return resultOk(convert(order));
    }

    @Override
    @Transactional
    public Result<OrderDTO> updateOrderPaymentInfo(User authUser, Long orderId, String paymentInfo) {
        if (orderId == null) {
            return resultWithStatus(INCORRECT_PARAMS, ORDER_ID_IS_NULL);
        }

        if (StringUtils.isEmpty(paymentInfo)) {
            return resultWithStatus(INCORRECT_PARAMS, PAYMENT_INFO_IS_NULL);
        }

        Order order = orderDAO.findById(orderId).orElse(null);
        if (order == null) {
            return resultWithStatus(NOT_FOUND, ORDER_NOT_FOUND);
        }

        User user = userDAO.findById(order.getUserId()).orElse(null);
        if (user == null) {
            return resultWithStatus(NOT_FOUND, USER_NOT_FOUND);
        }

        Result<?> checkAccessResult = checkAccess(authUser, user);
        if (checkAccessResult.isError()) {
            return checkAccessResult.cast();
        }

        order.setPaymentInfo(paymentInfo);
        orderDAO.save(order);

        return resultOk(convert(order));
    }

    @Override
    public Result<OrderDTO> getOrder(User authUser, Long id) {
        if (id == null) {
            return resultWithStatus(INCORRECT_PARAMS, ORDER_ID_IS_NULL);
        }

        Order order = orderDAO.findById(id).orElse(null);
        if (order == null) {
            return resultWithStatus(NOT_FOUND, ORDER_NOT_FOUND);
        }

        User user = userDAO.findById(order.getUserId()).orElse(null);
        if (user == null) {
            return resultWithStatus(NOT_FOUND, USER_NOT_FOUND);
        }

        Result<?> checkAccessResult = checkAccess(authUser, user);
        if (checkAccessResult.isError()) {
            return checkAccessResult.cast();
        }

        return resultOk(convert(order));
    }

    @Override
    public Result<SearchResult<OrderDTO>> getAllOrdersByUser(User authUser, int pageNum, int pageSize, Long userId) {
        if (pageNum < 1) {
            return resultWithStatus(INCORRECT_PARAMS, PAGE_NUM_MUST_BE_POSITIVE);
        }

        if (pageSize < 1) {
            return resultWithStatus(INCORRECT_PARAMS, PAGE_SIZE_MUST_BE_POSITIVE);
        }

        if (userId == null) {
            return resultWithStatus(INCORRECT_PARAMS, USER_ID_IS_NULL);
        }

        User user = userDAO.findById(userId).orElse(null);
        if (user == null) {
            return resultWithStatus(NOT_FOUND, USER_NOT_FOUND);
        }

        Result<?> checkAccessResult = checkAccess(authUser, user);
        if (checkAccessResult.isError()) {
            return checkAccessResult.cast();
        }

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<Order> orderPage = orderDAO.findAllByUserId(userId, pageable);
        List<Order> orders = orderPage.stream().toList();
        if (CollectionUtils.isEmpty(orders)) {
            return resultWithStatus(NOT_FOUND, ORDER_NOT_FOUND);
        }

        return resultOk(makeSearchResult(orders.stream().map(this::convert).collect(Collectors.toList()),
                orders.size(), orderPage.getTotalPages(), orderPage.getTotalElements()));
    }

    @Override
    public Result<SearchResult<OrderDTO>> getAllOrders(User authUser, int pageNum, int pageSize) {
        if (pageNum < 1) {
            return resultWithStatus(INCORRECT_PARAMS, PAGE_NUM_MUST_BE_POSITIVE);
        }

        if (pageSize < 1) {
            return resultWithStatus(INCORRECT_PARAMS, PAGE_SIZE_MUST_BE_POSITIVE);
        }

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        Page<Order> orderPage = orderDAO.findAll(pageable);
        List<Order> orders = orderPage.stream().toList();
        if (CollectionUtils.isEmpty(orders)) {
            return resultWithStatus(NOT_FOUND, ORDER_NOT_FOUND);
        }

        return resultOk(makeSearchResult(orders.stream().map(this::convert).collect(Collectors.toList()),
                orders.size(), orderPage.getTotalPages(), orderPage.getTotalElements()));
    }

    private OrderDTO convert(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setUserId(order.getUserId());
        orderDTO.setRestaurantId(order.getRestaurantId());
        orderDTO.setOrderItems(order.getOrderItems());
        orderDTO.setOrderStatus(order.getOrderStatus());
        orderDTO.setOrderStatusChanged(order.getOrderStatusChanged());
        orderDTO.setOrderDate(order.getOrderDate());
        orderDTO.setTotalAmount(order.getTotalAmount());
        orderDTO.setTelegramPaymentChargeId(order.getTelegramPaymentChargeId());
        orderDTO.setProviderPaymentChargeId(order.getProviderPaymentChargeId());
        orderDTO.setShippingOptionId(order.getShippingOptionId());
        orderDTO.setComment(order.getComment());
        orderDTO.setPaymentInfo(order.getPaymentInfo());
        return orderDTO;
    }

    private Order convert(OrderDTO orderDTO) {
        Order order = new Order();
        order.setId(orderDTO.getId());
        order.setUserId(orderDTO.getUserId());
        order.setRestaurantId(orderDTO.getRestaurantId());
        order.setOrderItems(orderDTO.getOrderItems());
        order.setOrderStatus(orderDTO.getOrderStatus());
        order.setOrderStatusChanged(orderDTO.getOrderStatusChanged());
        order.setOrderDate(orderDTO.getOrderDate());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setTelegramPaymentChargeId(orderDTO.getTelegramPaymentChargeId());
        order.setProviderPaymentChargeId(orderDTO.getProviderPaymentChargeId());
        order.setShippingOptionId(orderDTO.getShippingOptionId());
        order.setComment(orderDTO.getComment());
        order.setPaymentInfo(orderDTO.getPaymentInfo());
        return order;
    }

    private static <T> Result<T> validate(OrderDTO orderDTO) {
        if (orderDTO == null) {
            return resultWithStatus(INCORRECT_PARAMS, ORDER_IS_NULL);
        }

        if (orderDTO.getUserId() == null) {
            return resultWithStatus(INCORRECT_PARAMS, USER_ID_IS_NULL);
        }

        if (orderDTO.getRestaurantId() == null) {
            return resultWithStatus(INCORRECT_PARAMS, RESTAURANT_ID_IS_NULL);
        }

        List<OrderItem> orderItemList = orderDTO.getOrderItems();
        if (CollectionUtils.isEmpty(orderItemList)) {
            return resultWithStatus(INCORRECT_PARAMS, ORDER_ITEMS_IS_EMPTY);
        }

        for (OrderItem orderItem: orderItemList) {
            if (orderItem.getProductId() == null) {
                return resultWithStatus(INCORRECT_PARAMS, PRODUCT_ID_IS_NULL);
            }

            if (orderItem.getCount() < 0) {
                return resultWithStatus(INCORRECT_PARAMS, INCORRECT_ORDER_ITEM_COUNT);
            }
        }

        return resultOk();
    }
}
