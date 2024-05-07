package com.vladhacksmile.crm.controller;

import com.vladhacksmile.crm.dto.OrderDTO;
import com.vladhacksmile.crm.dto.ProductDTO;
import com.vladhacksmile.crm.dto.ResponseMapper;
import com.vladhacksmile.crm.jdbc.OrderStatus;
import com.vladhacksmile.crm.jdbc.User;
import com.vladhacksmile.crm.model.result.Result;
import com.vladhacksmile.crm.model.result.SearchResult;
import com.vladhacksmile.crm.service.OrderService;
import com.vladhacksmile.crm.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Result<OrderDTO>> createOrder(@AuthenticationPrincipal User authUser, @RequestBody OrderDTO orderDTO) {
        return ResponseMapper.map(orderService.createOrder(authUser, orderDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<OrderDTO>> getOrder(@AuthenticationPrincipal User authUser, @PathVariable Long orderId) {
        return ResponseMapper.map(orderService.getOrder(authUser, orderId));
    }

    @GetMapping
    public ResponseEntity<Result<SearchResult<OrderDTO>>> getAllOrdersByUser(@AuthenticationPrincipal User authUser,
                                                                       @RequestParam(name = "page_num", defaultValue = "1") int pageNum,
                                                                       @RequestParam(name = "page_size", defaultValue = "10") int pageSize,
                                                                       @RequestParam(name = "user_id") long userId) {
        return ResponseMapper.map(orderService.getAllOrdersByUser(authUser, pageNum, pageSize, userId));
    }

    @PatchMapping
    public ResponseEntity<Result<OrderDTO>> updateOrderStatus(@AuthenticationPrincipal User authUser,
                                                              @RequestParam(name = "order_id") long orderId,
                                                              @RequestParam(name = "order_status") OrderStatus orderStatus) {
        return ResponseMapper.map(orderService.updateOrderStatus(authUser, orderId, orderStatus));
    }

}
