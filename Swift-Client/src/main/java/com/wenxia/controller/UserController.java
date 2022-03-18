package com.wenxia.controller;

import com.wenxia.facade.model.User;
import com.wenxia.facade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhouw
 * @date 2022-03-18
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/findUser")
    public User findUser(String userId) {
        return userService.findUser(userId);
    }

    @GetMapping("/listUsers")
    public List<User> listUsers() {
        return userService.listUsers("os", 1, 10);
    }
}
