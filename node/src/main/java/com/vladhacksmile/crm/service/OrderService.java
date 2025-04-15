package com.vladhacksmile.crm.service;

import com.vladhacksmile.crm.dto.OrderDTO;
import com.vladhacksmile.crm.jdbc.order.OrderStatus;
import com.vladhacksmile.crm.jdbc.user.User;
import com.vladhacksmile.crm.model.result.Result;
import com.vladhacksmile.crm.model.result.SearchResult;

public interface OrderService {

    Result<OrderDTO> createOrder(User authUser, OrderDTO orderDTO);

    Result<OrderDTO> updateOrderStatus(User authUser, Long orderId, OrderStatus orderStatus);

    Result<OrderDTO> updateOrderPaymentInfo(User authUser, Long orderId, String paymentInfo);

    Result<OrderDTO> getOrder(User authUser, Long id);

    Result<SearchResult<OrderDTO>> getAllOrdersByUser(User authUser, int pageNum, int pageSize, Long userId);

    Result<SearchResult<OrderDTO>> getAllOrders(User authUser, int pageNum, int pageSize);
}
