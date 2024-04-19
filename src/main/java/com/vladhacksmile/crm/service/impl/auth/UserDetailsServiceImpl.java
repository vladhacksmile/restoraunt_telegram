package com.vladhacksmile.crm.service.impl.auth;

import com.vladhacksmile.crm.dao.UserDAO;
import com.vladhacksmile.crm.jdbc.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserDAO userRepository;

    @Override
    @Transactional
    public User loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with phoneNumber: " + phoneNumber));
    }
}