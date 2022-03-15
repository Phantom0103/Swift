package com.wenxia.service;

import com.wenxia.facade.model.User;
import com.wenxia.facade.service.UserService;
import com.wenxia.swift.server.protocol.RpcService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouw
 * @date 2022-03-15
 */
@RpcService
public class UserServiceImpl implements UserService {

    @Override
    public User findUser(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }

        User user = new User();
        user.setUserId(userId);
        user.setUsername("张三");
        user.setType(1);

        return user;
    }

    @Override
    public List<User> listUser(String source, int pageNo, int pageSize) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setUserId("user-" + i);
            user.setUsername("张三");
            user.setType(1);
            users.add(user);
        }

        return users;
    }
}
