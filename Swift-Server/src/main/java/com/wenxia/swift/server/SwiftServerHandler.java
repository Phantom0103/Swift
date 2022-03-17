package com.wenxia.swift.server;

import com.wenxia.swift.server.kryo.Kryos;
import com.wenxia.swift.server.protocol.RpcRequest;
import com.wenxia.swift.server.protocol.RpcResponse;
import com.wenxia.swift.server.protocol.SwiftMessage;
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
            throw new RuntimeException("Deserialize RpcRequest exception");
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
            throw new RuntimeException("rpc service '" + className + "' not found");
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
        LOGGER.error("An error occurred in SwiftServerHandler", cause);
        ctx.close();
    }
}
