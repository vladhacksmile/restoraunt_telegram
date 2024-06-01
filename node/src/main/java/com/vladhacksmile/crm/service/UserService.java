package com.vladhacksmile.crm.service;

import com.vladhacksmile.crm.dto.ShoppingCartDTO;
import com.vladhacksmile.crm.dto.auth.AuthDTO;
import com.vladhacksmile.crm.dto.auth.UserDTO;
import com.vladhacksmile.crm.jdbc.Role;
import com.vladhacksmile.crm.jdbc.User;
import com.vladhacksmile.crm.model.result.Result;

public interface UserService {

    Result<UserDTO> registerUser(UserDTO userDTO);

    Result<AuthDTO> authUser(AuthDTO authDTO);

    Result<UserDTO> removeUser(User authUser, Long userId);

    Result<UserDTO> getUser(User authUser, Long userId);

    Result<UserDTO> updateUser(User authUser, UserDTO userDTO);

    Result<UserDTO> updateUserRole(User authUser, Long userId, Role role);

    Result<ShoppingCartDTO> updateUserShoppingCart(User authUser, ShoppingCartDTO shoppingCartDTO);

    Result<ShoppingCartDTO> getUserShoppingCart(User authUser, Long userId);

}
