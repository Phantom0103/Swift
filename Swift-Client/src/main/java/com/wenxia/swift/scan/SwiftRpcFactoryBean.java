package com.wenxia.swift.scan;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Proxy;

public class SwiftRpcFactoryBean<T> implements FactoryBean<T> {

    private Class<T> rpcInterface;

    @Autowired
    SwiftRpcFactory<T> factory;

    public SwiftRpcFactoryBean(Class<T> rpcInterface) {
        this.rpcInterface = rpcInterface;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(rpcInterface.getClassLoader(), new Class[]{rpcInterface}, factory);
    }

    @Override
    public Class<?> getObjectType() {
        return rpcInterface;
    }
}
