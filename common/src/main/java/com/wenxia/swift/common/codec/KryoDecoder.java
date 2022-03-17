package com.wenxia.swift.common.codec;

import com.wenxia.swift.common.protocol.SwiftMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @author zhouw
 * @date 2022-03-16
 */
public class KryoDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readInt();
        byte[] content = new byte[length];
        in.readBytes(content);

        SwiftMessage msg = new SwiftMessage();
        msg.setLength(length);
        msg.setContent(content);

        out.add(msg);
    }
}
