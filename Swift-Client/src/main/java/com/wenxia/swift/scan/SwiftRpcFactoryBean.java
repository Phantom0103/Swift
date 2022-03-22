package com.wenxia.swift.scan;

import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class SwiftRpcFactoryBean<T> implements FactoryBean<T> {

    private Class<T> rpcInterface;

    public SwiftRpcFactoryBean() {
        // intentionally empty
    }

    public SwiftRpcFactoryBean(Class<T> rpcInterface) {
        this.rpcInterface = rpcInterface;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getObject() throws Exception {
        SwiftRpcFactory<T> factory = new SwiftRpcFactory<>(rpcInterface);
        return (T) Proxy.newProxyInstance(rpcInterface.getClassLoader(), new Class[]{rpcInterface}, factory);
    }

    @Override
    public Class<?> getObjectType() {
        return rpcInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Class<T> getRpcInterface() {
        return rpcInterface;
    }

    public void setRpcInterface(Class<T> rpcInterface) {
        this.rpcInterface = rpcInterface;
    }
}
