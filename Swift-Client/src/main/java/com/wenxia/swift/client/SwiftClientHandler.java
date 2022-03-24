package com.wenxia.swift.client;

import com.wenxia.swift.common.protocol.RpcRequest;
import com.wenxia.swift.common.protocol.RpcResponse;
import com.wenxia.swift.common.protocol.SwiftMessage;
import com.wenxia.swift.common.util.Kryos;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * @author zhouw
 * @date 2022-03-17
 */
@Component
@ChannelHandler.Sharable
public class SwiftClientHandler extends SimpleChannelInboundHandler<SwiftMessage> {

    private ConcurrentHashMap<String, SynchronousQueue<Object>> requests = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SwiftMessage swiftMessage) throws Exception {
        byte[] content = swiftMessage.getContent();
        RpcResponse response = Kryos.deserialize(content, RpcResponse.class);
        if (response == null) {
            throw new RuntimeException("反序列化RpcResponse异常，获取RpcResponse为空");
        }

        String requestId = response.getRequestId();
        SynchronousQueue<Object> queue = requests.get(requestId);
        queue.put(response.getData());
    }

    SynchronousQueue<Object> sendRequest(RpcRequest request, Channel channel) {
        SynchronousQueue<Object> queue = new SynchronousQueue<>();
        requests.put(request.getId(), queue);
        channel.writeAndFlush(request);
        return queue;
    }
}
