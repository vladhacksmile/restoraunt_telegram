package com.vladhacksmile.crm.service.impl;

import com.vladhacksmile.crm.dao.*;
import com.vladhacksmile.crm.dto.OrderDTO;
import com.vladhacksmile.crm.dto.ProductDTO;
import com.vladhacksmile.crm.jdbc.*;
import com.vladhacksmile.crm.model.result.Result;
import com.vladhacksmile.crm.model.result.SearchResult;
import com.vladhacksmile.crm.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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

public class OrderServiceImpl implements OrderService {

    @Autowired
    private Clock clock;

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private OrderDAO orderDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private RestaurantDAO restaurantDAO;

    @Autowired
    private ShoppingCartDAO shoppingCartDAO;

    @Override
    @Transactional
    public Result<OrderDTO> createOrder(User authUser, OrderDTO orderDTO) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (orderDTO == null) {
            return resultWithStatus(INCORRECT_PARAMS, ORDER_IS_NULL);
        }

        if (orderDTO.getId() != null) {
            return resultWithStatus(INCORRECT_PARAMS, ORDER_ID_IS_NOT_NULL);
        }

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

        orderDTO.setOrderStatus(OrderStatus.NEW);
        orderDTO.setOrderStatusChanged(now);
        orderDTO.setOrderDate(now);

        orderDAO.save(order);

        ShoppingCart shoppingCart = shoppingCartDAO.findByUserId(orderDTO.getUserId()).orElse(null);
        if (shoppingCart != null) {
            shoppingCart.setOrderItems(Collections.emptyList());
            shoppingCartDAO.save(shoppingCart);
        }

        return resultOk(convert(order));
    }

    @Override
    @Transactional
    public Result<OrderDTO> updateOrderStatus(User authUser, Long orderId, OrderStatus orderStatus) {
        LocalDateTime now = LocalDateTime.now(clock);
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

        if (Objects.equals(order.getOrderStatus(), orderStatus)) {
            return resultWithStatus(INCORRECT_STATE, SAME_STATUSES);
        }

        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            return resultWithStatus(INCORRECT_STATE, ORDER_CANCELLED);
        }

        order.setOrderStatus(orderStatus);
        order.setOrderStatusChanged(now);
        orderDAO.save(order);

        return resultOk(convert(order));
    }

    @Override
    public Result<OrderDTO> getOrder(User authUser, Long orderId) {
        if (orderId == null) {
            return resultWithStatus(INCORRECT_PARAMS, ORDER_ID_IS_NULL);
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

    private OrderDTO convert(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setUserId(order.getUserId());
        orderDTO.setRestaurantId(order.getRestaurantId());
        orderDTO.setOrderItems(order.getOrderItems());
        orderDTO.setOrderStatus(order.getOrderStatus());
        orderDTO.setOrderStatusChanged(order.getOrderStatusChanged());
        orderDTO.setOrderDate(order.getOrderDate());

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

        return order;
    }

    private Result<?> validate(OrderDTO orderDTO) {
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
