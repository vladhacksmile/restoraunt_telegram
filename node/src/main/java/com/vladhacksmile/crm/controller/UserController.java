package com.vladhacksmile.crm.controller;

import com.vladhacksmile.crm.utils.ResponseMapper;
import com.vladhacksmile.crm.dto.ShoppingCartDTO;
import com.vladhacksmile.crm.dto.auth.AuthDTO;
import com.vladhacksmile.crm.dto.auth.UserDTO;
import com.vladhacksmile.crm.jdbc.user.User;
import com.vladhacksmile.crm.model.result.Result;
import com.vladhacksmile.crm.service.auth.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Result<UserDTO>> register(@RequestBody UserDTO userDTO) {
        return ResponseMapper.map(userService.registerUser(userDTO));
    }


    @PostMapping("/auth")
    public ResponseEntity<Result<AuthDTO>> auth(@RequestBody AuthDTO authDTO) {
        return ResponseMapper.map(userService.authUser(authDTO));
    }

    @PutMapping
    public ResponseEntity<Result<UserDTO>> updateUser(@AuthenticationPrincipal User authUser, @RequestBody UserDTO userDTO) {
        return ResponseMapper.map(userService.updateUser(authUser, userDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<UserDTO>> removeUser(@AuthenticationPrincipal User authUser, @PathVariable Long id) {
        return ResponseMapper.map(userService.removeUser(authUser, id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<UserDTO>> getUser(@AuthenticationPrincipal User authUser, @PathVariable Long id) {
        return ResponseMapper.map(userService.getUser(authUser, id));
    }

    @PutMapping("/shopping_cart")
    public ResponseEntity<Result<ShoppingCartDTO>> updateUserShoppingCart(@AuthenticationPrincipal User authUser, @RequestBody ShoppingCartDTO shoppingCartDTO) {
        return ResponseMapper.map(userService.updateUserShoppingCart(authUser, shoppingCartDTO));
    }

    @GetMapping("/shopping_cart/{user_id}")
    public ResponseEntity<Result<ShoppingCartDTO>> getUserShoppingCart(@AuthenticationPrincipal User authUser, @PathVariable(name = "user_id") Long userId) {
        return ResponseMapper.map(userService.getUserShoppingCart(authUser, userId));
    }
}
