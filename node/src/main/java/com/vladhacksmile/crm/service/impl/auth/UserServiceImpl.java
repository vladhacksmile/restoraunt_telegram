package com.vladhacksmile.crm.service.impl.auth;

import com.vladhacksmile.crm.dao.ShoppingCartDAO;
import com.vladhacksmile.crm.dao.UserDAO;
import com.vladhacksmile.crm.dto.OrderDTO;
import com.vladhacksmile.crm.dto.ShoppingCartDTO;
import com.vladhacksmile.crm.dto.auth.AuthDTO;
import com.vladhacksmile.crm.dto.auth.UserDTO;
import com.vladhacksmile.crm.gpt.TelegramEmoji;
import com.vladhacksmile.crm.jdbc.*;
import com.vladhacksmile.crm.model.result.Result;
import com.vladhacksmile.crm.service.UserService;
import com.vladhacksmile.crm.utils.jwt.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.vladhacksmile.crm.model.result.Result.resultOk;
import static com.vladhacksmile.crm.model.result.Result.resultWithStatus;
import static com.vladhacksmile.crm.model.result.status.Status.*;
import static com.vladhacksmile.crm.model.result.status.StatusDescription.*;
import static com.vladhacksmile.crm.utils.AuthUtils.checkAccess;
import static com.vladhacksmile.crm.utils.EntityUtils.setIfUpdated;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private ShoppingCartDAO shoppingCartDAO;

    @Override
    @Transactional
    public Result<UserDTO> registerUser(UserDTO userDTO) {
        if (userDTO == null) {
            return resultWithStatus(INCORRECT_PARAMS, USER_IS_NULL);
        }

        if (userDTO.getId() != null) {
            return resultWithStatus(INCORRECT_PARAMS, USER_ID_IS_NULL);
        }

        Result<?> validationResult = validation(userDTO);
        if (validationResult.isError()) {
            return validationResult.cast();
        }

        User user = convert(userDTO);

        if (userDAO.existsByMail(user.getMail())) {
            return resultWithStatus(INCORRECT_PARAMS, USER_MAIL_EXISTS);
        }

        if (userDAO.existsByPhoneNumber(user.getPhoneNumber())) {
            return resultWithStatus(INCORRECT_PARAMS, USER_PHONE_NUMBER_EXISTS);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.CLIENT);
        userDAO.save(user);

        // У каждого пользователя должна быть своя единственная корзина, так мы можем ее поддерживать в телеграмме, например
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(user.getId());
        shoppingCartDAO.save(shoppingCart);

        return resultOk(convert(user));
    }

    @Override
    public Result<AuthDTO> authUser(AuthDTO authDTO) {
        if (authDTO == null) {
            return resultWithStatus(INCORRECT_PARAMS, AUTH_IS_NULL);
        }

        if (StringUtils.isEmpty(authDTO.getPhoneNumber())) {
            return resultWithStatus(INCORRECT_PARAMS, AUTH_PHONE_NUMBER_IS_NULL);
        }

        if (StringUtils.isEmpty(authDTO.getPassword())) {
            return resultWithStatus(INCORRECT_PARAMS, AUTH_PASSWORD_IS_NULL);
        }

        User user = userDAO.findByPhoneNumber(authDTO.getPhoneNumber()).orElse(null);
        if (user == null) {
            return resultWithStatus(NOT_FOUND, USER_NOT_FOUND);
        }

        if (!Objects.equals(passwordEncoder.encode(authDTO.getPassword()), user.getPassword())) {
            return resultWithStatus(INCORRECT_PARAMS, AUTH_PASSWORD_INCORRECT);
        }

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        authDTO.getPhoneNumber(),
                        authDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User userDetails = (User) authentication.getPrincipal();
        if (userDetails == null) {
            return resultWithStatus(INTERNAL_ERROR, USER_IS_NULL);
        }

        String jwt = jwtUtils.generateJwtToken(authentication);
        if (StringUtils.isEmpty(jwt)) {
            return resultWithStatus(INTERNAL_ERROR, COULD_NOT_GENERATE_JWT_TOKEN);
        }

        // Пароль скроем в целях безопасности
        authDTO.setJwtToken(jwt);
        authDTO.setPassword(null);

        return resultOk(authDTO);
    }

    @Override
    @Transactional
    public Result<UserDTO> removeUser(User authUser, Long userId) {
        if (userId == null) {
            return resultWithStatus(INCORRECT_PARAMS, USER_ID_IS_NULL);
        }

        User user = userDAO.findById(userId).orElse(null);
        if (user == null) {
            return resultWithStatus(NOT_FOUND, USER_NOT_FOUND);
        }

        Result<?> checkAccessResult = checkAccess(authUser, user);
        if (checkAccessResult.isError()) {
            return checkAccessResult.cast();
        }

        shoppingCartDAO.findByUserId(userId).ifPresent(shoppingCart -> shoppingCartDAO.delete(shoppingCart));

        userDAO.delete(user);

        return resultOk(convert(user));
    }

    @Override
    public Result<UserDTO> getUser(User authUser, Long userId) {
        if (userId == null) {
            return resultWithStatus(INCORRECT_PARAMS, USER_ID_IS_NULL);
        }

        User user = userDAO.findById(userId).orElse(null);
        if (user == null) {
            return resultWithStatus(NOT_FOUND, USER_NOT_FOUND);
        }

        Result<?> checkAccessResult = checkAccess(authUser, user);
        if (checkAccessResult.isError()) {
            return checkAccessResult.cast();
        }

        return resultOk(convert(user));
    }

    @Override
    @Transactional
    public Result<UserDTO> updateUser(User authUser, UserDTO userDTO) {
        if (userDTO == null) {
            return resultWithStatus(INCORRECT_PARAMS, USER_IS_NULL);
        }

        if (userDTO.getId() == null) {
            return resultWithStatus(INCORRECT_PARAMS, USER_ID_IS_NULL);
        }

        Result<?> validationResult = validation(userDTO);
        if (validationResult.isError()) {
            return validationResult.cast();
        }

        User user = userDAO.findById(userDTO.getId()).orElse(null);
        if (user == null) {
            return resultWithStatus(NOT_FOUND, USER_NOT_FOUND);
        }

        Result<?> checkAccessResult = checkAccess(authUser, user);
        if (checkAccessResult.isError()) {
            return checkAccessResult.cast();
        }

        setIfUpdated(userDTO.getName(), user.getName(), user::setName);
        setIfUpdated(userDTO.getSurname(), user.getSurname(), user::setSurname);
        setIfUpdated(userDTO.getPatronymic(), user.getPatronymic(), user::setPatronymic);

        if (!Objects.equals(userDTO.getPhoneNumber(), user.getPhoneNumber())) {
            // todo всякие проверки подтверждение и т п
            if (userDAO.existsByPhoneNumber(userDTO.getPhoneNumber())) {
                return resultWithStatus(INCORRECT_PARAMS, USER_PHONE_NUMBER_EXISTS);
            }
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }

        if (!Objects.equals(userDTO.getMail(), user.getMail())) {
            // todo всякие проверки подтверждение и т п
            if (userDAO.existsByMail(userDTO.getMail())) {
                return resultWithStatus(INCORRECT_PARAMS, USER_MAIL_EXISTS);
            }
            user.setMail(userDTO.getMail());
        }

        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        if (!Objects.equals(encodedPassword, user.getPassword())) {
            // todo всякие проверки подтверждение и т п
            user.setPassword(encodedPassword);
        }

        userDAO.save(user);

        return resultOk(convert(user));
    }

    @Override
    @Transactional
    public Result<UserDTO> updateUserRole(User authUser, Long userId, Role role) {
        LocalDateTime now = LocalDateTime.now();
        if (userId == null) {
            return resultWithStatus(INCORRECT_PARAMS, USER_ID_IS_NULL);
        }

        if (role == null) {
            return resultWithStatus(INCORRECT_PARAMS, ROLE_IS_NULL);
        }

        User user = userDAO.findById(userId).orElse(null);
        if (user == null) {
            return resultWithStatus(NOT_FOUND, USER_NOT_FOUND);
        }

        Result<?> checkAccessResult = checkAccess(authUser, user);
        if (checkAccessResult.isError()) {
            return checkAccessResult.cast();
        }

        if (Objects.equals(user.getRole(), role)) {
            return resultWithStatus(INCORRECT_STATE, SAME_ROLE);
        }

        user.setRole(role);
        userDAO.save(user);

        return resultOk(convert(user));
    }

    @Transactional
    public Result<ShoppingCartDTO> updateUserShoppingCart(User authUser, ShoppingCartDTO shoppingCartDTO) {
        if (shoppingCartDTO.getId() == null && shoppingCartDTO.getUserId() == null) {
            return resultWithStatus(INCORRECT_PARAMS, SHOPPING_CART_ID_AND_CLIENT_ID_IS_NULL);
        }

        ShoppingCart shoppingCart;
        if (shoppingCartDTO.getId() != null) {
            shoppingCart = shoppingCartDAO.findById(shoppingCartDTO.getId()).orElse(null);
        } else {
            shoppingCart = shoppingCartDAO.findByUserId(shoppingCartDTO.getUserId()).orElse(null);
        }

        if (shoppingCart == null) {
            return resultWithStatus(NOT_FOUND, SHOPPING_CART_NOT_FOUND);
        }

        Result<UserDTO> getUserResult = getUser(authUser, shoppingCartDTO.getUserId());
        if (getUserResult.isError()) {
            return getUserResult.cast();
        }

        shoppingCart.setOrderItems(shoppingCartDTO.getOrderItems());

        return resultOk(convert(shoppingCart));
    }

    @Override
    public Result<ShoppingCartDTO> getUserShoppingCart(User authUser, Long userId) {
        Result<UserDTO> getUserResult = getUser(authUser, userId);
        if (getUserResult.isError()) {
            return getUserResult.cast();
        }

        ShoppingCart shoppingCart = shoppingCartDAO.findByUserId(userId).orElse(null);
        if (shoppingCart == null) {
            return resultWithStatus(NOT_FOUND, SHOPPING_CART_NOT_FOUND);
        }

        return resultOk(convert(shoppingCart));
    }

    private ShoppingCartDTO convert(ShoppingCart shoppingCart) {
        ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
        shoppingCartDTO.setId(shoppingCart.getId());
        shoppingCartDTO.setUserId(shoppingCart.getUserId());
        shoppingCartDTO.setOrderItems(shoppingCart.getOrderItems());

        return shoppingCartDTO;
    }

    // todo: поиск с фильтром
    private User convert(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setName(userDTO.getName());
        user.setSurname(userDTO.getSurname());
        user.setPatronymic(userDTO.getPatronymic());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setMail(userDTO.getMail());
        user.setPassword(userDTO.getPassword());
        user.setRole(userDTO.getRole());
        return user;
    }

    private UserDTO convert(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setSurname(user.getSurname());
        userDTO.setPatronymic(user.getPatronymic());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setMail(user.getMail());
        userDTO.setPassword(user.getPassword());
        userDTO.setRole(user.getRole());
        return userDTO;
    }

    private Result<?> validation(UserDTO userDTO) {
        if (StringUtils.isEmpty(userDTO.getName())) {
            return resultWithStatus(INCORRECT_PARAMS, USER_NAME_IS_NULL);
        }

        if (StringUtils.isEmpty(userDTO.getPhoneNumber())) {
            return resultWithStatus(INCORRECT_PARAMS, USER_PHONE_NUMBER_IS_NULL);
        }

        if (StringUtils.isEmpty(userDTO.getMail())) {
            return resultWithStatus(INCORRECT_PARAMS, USER_MAIL_IS_NULL);
        }

        if (StringUtils.isEmpty(userDTO.getPassword())) {
            return resultWithStatus(INCORRECT_PARAMS, USER_PASSWORD_IS_NULL);
        }

        return resultOk();
    }
}