package com.vladhacksmile.crm.dao;

import com.vladhacksmile.crm.jdbc.user.Role;
import com.vladhacksmile.crm.jdbc.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleDAO extends JpaRepository<UserRole, Long> {

    Optional<UserRole> findByName(Role role);
}