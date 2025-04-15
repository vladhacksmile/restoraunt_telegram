package com.vladhacksmile.crm.dao;

import com.vladhacksmile.crm.jdbc.user.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDAO extends CrudRepository<User, Long> {

    Optional<User> findByMail(String mail);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Boolean existsByMail(String mail);

    Boolean existsByPhoneNumber(String phoneNumber);

}