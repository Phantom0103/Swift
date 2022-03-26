package com.wenxia.swift.client;

import com.wenxia.swift.common.codec.SwiftMessageDecoder;
import com.wenxia.swift.common.codec.SwiftMessageEncoder;
import com.wenxia.swift.common.protocol.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouw
 * @date 2022-03-17
 */
@Component
public class SwiftClientRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftClientRunner.class);

    @Value("${swift.rpc.servers}")
    private List<String> rpcServers;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SwiftClientHandler swiftClientHandler;

    private final Map<String, Channel> channels = new HashMap<>();

    private Bootstrap bootstrap = new Bootstrap();
    private NioEventLoopGroup group = new NioEventLoopGroup();

    public SwiftClientRunner() {
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast(new SwiftMessageEncoder());
                        pipeline.addLast(new SwiftMessageDecoder());
                        pipeline.addLast(swiftClientHandler);
                    }
                });
    }

    @Override
    public void run(ApplicationArguments args) {
        if (rpcServers == null || rpcServers.isEmpty()) {
            LOGGER.warn("没有配置RPC服务端");
            return;
        }

        Map<Object, Object> map = redisTemplate.opsForHash().entries("rpc-server");
        if (map == null || map.isEmpty()) {
            LOGGER.warn("RPC服务注册列表为空");
            return;
        }

        for (String rpcServer : rpcServers) {
            String v = (String) map.get(rpcServer);
            if (v == null) {
                LOGGER.warn("RPC服务'{}'没有注册", rpcServer);
                continue;
            }

            try {
                String[] address = v.split(":");
                InetSocketAddress remoteAddress = new InetSocketAddress(address[0], Integer.parseInt(address[1]));
                Channel channel = connect(bootstrap, remoteAddress);
                channels.put(rpcServer, channel);
            } catch (Exception e) {
                LOGGER.error("连接RPC服务端'{}'失败", rpcServer);
                e.printStackTrace();
            }
        }
    }

    private Channel connect(Bootstrap bootstrap, SocketAddress remoteAddress) throws Exception {
        return bootstrap.connect(remoteAddress).sync().channel();
    }

    private synchronized Channel connect(String rpcServer) {
        Channel channel = channels.get(rpcServer);
        if (channel != null) {
            return channel;
        }

        String v = (String) redisTemplate.opsForHash().get("rpc-server", rpcServer);
        if (v == null) {
            LOGGER.warn("RPC服务'{}'没有注册", rpcServer);
            return null;
        }

        try {
            String[] address = v.split(":");
            InetSocketAddress remoteAddress = new InetSocketAddress(address[0], Integer.parseInt(address[1]));
            channel = connect(bootstrap, remoteAddress);
            channels.put(rpcServer, channel);
        } catch (Exception e) {
            LOGGER.error("连接RPC服务端'{}'失败", rpcServer);
            e.printStackTrace();
        }

        return channel;
    }

    private Channel getChannel(String rpcServer) {
        Channel channel = channels.get(rpcServer);
        if (channel == null) {
            channel = connect(rpcServer);
        }

        return channel;
    }

    public Object sendRequest(RpcRequest request, String rpcServer) throws InterruptedException, IOException {
        Channel channel = getChannel(rpcServer);
        if (channel != null && channel.isActive()) {
            SynchronousQueue<Object> queue = swiftClientHandler.sendRequest(request, channel);
            return queue.poll(60, TimeUnit.SECONDS);
        }

        LOGGER.error("获取RPC服务器'{}'链接失败", rpcServer);
        return null;
    }

    @PreDestroy
    public void destroy() {
        LOGGER.info("RPC客户端退出");
        group.shutdownGracefully();
    }
}
