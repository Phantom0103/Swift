package com.wenxia.swift.server;

import com.wenxia.swift.server.protocol.RpcRequest;
import com.wenxia.swift.server.protocol.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author zhouw
 * @date 2022-03-15
 */
@ChannelHandler.Sharable
public class SwiftServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private Map<String, Object> rpcServiceMap;

    SwiftServerHandler(Map<String, Object> rpcServiceMap) {
        this.rpcServiceMap = rpcServiceMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(rpcRequest.getId());

        try {
            Object result = handler(rpcRequest);
            response.setData(result);
        } catch (Exception e) {
            response.setCode(-1);
            response.setErrorMsg(e.toString());
        }

        channelHandlerContext.writeAndFlush(response);
    }

    private Object handler(RpcRequest request) throws Exception {
        String className = request.getClassName();
        Object rpcService = rpcServiceMap.get(className);
        if (rpcService == null) {
            throw new RuntimeException("rpc service '" + className + "' not found");
        }

        String methodName = request.getMethodName();
        Object[] parameters = request.getParameters();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Class<?> clazz = rpcService.getClass();
        Method method = clazz.getMethod(methodName, parameterTypes);

        return method.invoke(rpcService, parameters);
    }
}
