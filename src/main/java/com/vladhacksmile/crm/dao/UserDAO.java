package com.vladhacksmile.crm.dao;

import com.vladhacksmile.crm.jdbc.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDAO extends JpaRepository<User, Long> {

    Optional<User> findByMail(String mail);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Boolean existsByMail(String mail);

    Boolean existsByPhoneNumber(String phoneNumber);

}