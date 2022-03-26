package com.wenxia.swift.common.codec;

import com.wenxia.swift.common.protocol.SwiftMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author zhouw
 * @date 2022-03-16
 */
public class SwiftMessageEncoder extends MessageToByteEncoder<SwiftMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, SwiftMessage msg, ByteBuf out) throws Exception {
        out.writeInt(msg.getLength());
        out.writeBytes(msg.getContent());
    }
}
