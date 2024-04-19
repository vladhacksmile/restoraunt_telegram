package com.vladhacksmile.crm.dao;

import com.vladhacksmile.crm.jdbc.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDAO extends PagingAndSortingRepository<Product, Long>, JpaSpecificationExecutor<Product> {

}
