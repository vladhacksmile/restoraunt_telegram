package com.vladhacksmile.crm.dao;

import com.vladhacksmile.crm.jdbc.Order;
import com.vladhacksmile.crm.jdbc.ShoppingCart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDAO extends JpaRepository<Order, Long> {

    Page<Order> findAllByUserId(Long userId, Pageable pageable);

}