package com.wenxia.facade.service;

import com.wenxia.facade.model.User;
import com.wenxia.swift.common.annotation.SwiftRpcService;

import java.util.List;

/**
 * @author zhouw
 * @date 2022-03-15
 */
@SwiftRpcService(server = "rpc-user-server")
public interface UserService {

    User findUser(String userId);

    List<User> listUsers(String source, int pageNo, int pageSize);
}
