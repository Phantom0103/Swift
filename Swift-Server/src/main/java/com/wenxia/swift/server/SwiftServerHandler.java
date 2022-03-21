package com.wenxia.swift.server;

import com.wenxia.swift.common.protocol.RpcRequest;
import com.wenxia.swift.common.protocol.RpcResponse;
import com.wenxia.swift.common.protocol.SwiftMessage;
import com.wenxia.swift.common.util.Kryos;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author zhouw
 * @date 2022-03-15
 */
@ChannelHandler.Sharable
public class SwiftServerHandler extends SimpleChannelInboundHandler<SwiftMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftServerHandler.class);

    private Map<String, Object> rpcServiceMap;

    SwiftServerHandler(Map<String, Object> rpcServiceMap) {
        this.rpcServiceMap = rpcServiceMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SwiftMessage msg) throws Exception {
        byte[] content = msg.getContent();
        RpcRequest request = Kryos.deserialize(content, RpcRequest.class);

        if (request == null) {
            throw new RuntimeException("反序列化RpcRequest异常，获取RpcRequest为空");
        }

        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getId());

        try {
            Object result = handler(request);
            response.setData(result);
        } catch (Exception e) {
            response.setCode(-1);
            response.setErrorMsg(e.toString());
        }

        ctx.writeAndFlush(response);
    }

    private Object handler(RpcRequest request) throws Exception {
        String className = request.getClassName();
        Object rpcService = rpcServiceMap.get(className);
        if (rpcService == null) {
            throw new RuntimeException("未找到RPC服务类：" + className);
        }

        String methodName = request.getMethodName();
        Object[] parameters = request.getParameters();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Class<?> clazz = rpcService.getClass();
        Method method = clazz.getMethod(methodName, parameterTypes);

        return method.invoke(rpcService, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("SwiftServerHandler发生异常，关闭链接", cause);
        ctx.close();
    }
}
