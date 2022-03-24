package com.wenxia.swift.scan;

import com.wenxia.swift.client.SwiftClientRunner;
import com.wenxia.swift.common.annotation.SwiftRpcService;
import com.wenxia.swift.common.protocol.RpcRequest;
import com.wenxia.swift.common.util.ApplicationContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class SwiftRpcFactory<T> implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftRpcFactory.class);

    private Class<T> rpcInterface;

    SwiftRpcFactory(Class<T> rpcInterface) {
        this.rpcInterface = rpcInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String className = rpcInterface.getName();
        String methodName = method.getName();
        // 过滤一些无效的代理方法
        if ("toString".equals(methodName) && isEmpty(args)) {
            return className + "@" + rpcInterface.hashCode();
        } else if ("hashCode".equals(methodName) && isEmpty(args)) {
            return rpcInterface.hashCode();
        } else if ("equals".equals(methodName) && args != null && args.length == 1) {
            return rpcInterface == args[0];
        }

        SwiftRpcService rpcService = rpcInterface.getAnnotation(SwiftRpcService.class);
        String rpcServer = rpcService.server();
        RpcRequest request = new RpcRequest();
        request.setId(UUID.randomUUID().toString());
        request.setClassName(rpcInterface.getName());
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());

        SwiftClientRunner swiftClient = ApplicationContextHolder.getApplicationContext().getBean(SwiftClientRunner.class);
        return swiftClient.sendRequest(request, rpcServer);
    }

    private boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }
}
