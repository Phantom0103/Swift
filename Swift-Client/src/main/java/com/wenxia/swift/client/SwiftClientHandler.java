package com.wenxia.swift.client;

import com.wenxia.swift.common.protocol.SwiftMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author zhouw
 * @date 2022-03-17
 */
public class SwiftClientHandler extends SimpleChannelInboundHandler<SwiftMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SwiftMessage swiftMessage) throws Exception {

    }
}
