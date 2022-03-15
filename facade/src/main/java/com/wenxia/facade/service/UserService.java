package com.wenxia.facade.service;

import com.wenxia.facade.model.User;

import java.util.List;

/**
 * @author zhouw
 * @date 2022-03-15
 */
public interface UserService {

    User findUser(String userId);

    List<User> listUser(String source, int pageNo, int pageSize);
}
