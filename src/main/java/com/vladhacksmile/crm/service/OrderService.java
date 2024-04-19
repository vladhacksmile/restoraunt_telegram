package com.vladhacksmile.crm.service;

import com.vladhacksmile.crm.dto.OrderDTO;
import com.vladhacksmile.crm.dto.ShoppingCartDTO;
import com.vladhacksmile.crm.dto.auth.AuthDTO;
import com.vladhacksmile.crm.dto.auth.UserDTO;
import com.vladhacksmile.crm.jdbc.OrderStatus;
import com.vladhacksmile.crm.jdbc.User;
import com.vladhacksmile.crm.model.result.Result;
import com.vladhacksmile.crm.model.result.SearchResult;

public interface OrderService {

    Result<OrderDTO> createOrder(User authUser, OrderDTO orderDTO);

    Result<OrderDTO> updateOrderStatus(User authUser, Long orderId, OrderStatus orderStatus);

    Result<OrderDTO> getOrder(User authUser, Long orderId);

    Result<SearchResult<OrderDTO>> getAllOrdersByUser(User authUser, int pageNum, int pageSize, Long userId);
}
