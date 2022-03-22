package com.wenxia.swift.scan;

import com.wenxia.facade.model.User;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SwiftRpcFactory<T> implements InvocationHandler {

    private Class<T> rpcInterface;

    public SwiftRpcFactory(Class<T> rpcInterface) {
        this.rpcInterface = rpcInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        System.out.println("methodName: " + methodName);
        if ("findUser".equals(methodName)) {
            User user = new User();
            user.setType(1);
            user.setUsername(String.valueOf(args[0]));
            user.setUserId("xadhwwfg");
            return user;
        } else if ("listUsers".equals(methodName)) {
            User user = new User();
            user.setType(2);
            user.setUsername("lili");
            user.setUserId("xadhwwfg");

            List<User> users = new ArrayList<>();
            users.add(user);
            return users;
        }

        return null;
    }
}
