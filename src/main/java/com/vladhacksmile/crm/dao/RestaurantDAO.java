package com.vladhacksmile.crm.dao;

import com.vladhacksmile.crm.jdbc.Order;
import com.vladhacksmile.crm.jdbc.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantDAO extends JpaRepository<Restaurant, Long> {

}