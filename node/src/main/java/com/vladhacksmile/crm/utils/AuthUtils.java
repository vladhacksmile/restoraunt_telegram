package com.vladhacksmile.crm.utils;

import com.vladhacksmile.crm.jdbc.Role;
import com.vladhacksmile.crm.jdbc.User;
import com.vladhacksmile.crm.model.result.Result;

import java.util.Objects;

import static com.vladhacksmile.crm.model.result.Result.resultOk;
import static com.vladhacksmile.crm.model.result.Result.resultWithStatus;
import static com.vladhacksmile.crm.model.result.status.Status.ACCESS_DENIED;
import static com.vladhacksmile.crm.model.result.status.StatusDescription.*;

public class AuthUtils {

    public static Result<?> checkAccess(User authUser, User user) {
        if (authUser == null) {
            return resultWithStatus(ACCESS_DENIED, AUTH_USER_IS_NULL);
        }

        if (user == null) {
            return resultWithStatus(ACCESS_DENIED, USER_IS_NULL);
        }

        if (authUser.getRole() != Role.ADMIN && authUser.getRole() != Role.MAKER) {
            if (!Objects.equals(authUser.getId(), user.getId())) {
                return resultWithStatus(ACCESS_DENIED, DIFFERENT_USER_IDS);
            }
        }

        return resultOk();
    }

}
