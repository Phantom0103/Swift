package com.wenxia.swift.client;

import com.wenxia.swift.common.protocol.RpcRequest;
import com.wenxia.swift.common.protocol.RpcResponse;
import com.wenxia.swift.common.protocol.SwiftMessage;
import com.wenxia.swift.common.util.Kryos;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * @author zhouw
 * @date 2022-03-17
 */
@Component
@ChannelHandler.Sharable
public class SwiftClientHandler extends SimpleChannelInboundHandler<SwiftMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftClientHandler.class);

    private final ConcurrentHashMap<String, SynchronousQueue<Object>> requests = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SwiftMessage swiftMessage) throws Exception {
        byte[] content = swiftMessage.getContent();
        RpcResponse response = Kryos.deserialize(content, RpcResponse.class);
        String requestId = response.getRequestId();
        SynchronousQueue<Object> queue = requests.get(requestId);
        queue.put(response.getData());
    }

    SynchronousQueue<Object> sendRequest(RpcRequest request, Channel channel) throws IOException {
        SynchronousQueue<Object> queue = new SynchronousQueue<>();
        requests.put(request.getId(), queue);

        byte[] content = Kryos.serialize(request);
        SwiftMessage message = new SwiftMessage(content.length, content);
        channel.writeAndFlush(message);

        return queue;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("SwiftClientHandler发生异常，关闭链接", cause);
        ctx.close();
    }
}
