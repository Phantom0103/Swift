package com.wenxia.swift.server;

import com.wenxia.swift.common.codec.SwiftMessageDecoder;
import com.wenxia.swift.common.codec.SwiftMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.wenxia.swift.common.SystemPro.DEFAULT_SERVER_PORT;

/**
 * @author zhouw
 * @date 2022-03-14
 */
@Component
public class SwiftServerRunner implements ApplicationRunner, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftServerRunner.class);

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    @Autowired
    private Environment env;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final Map<String, Object> rpcServiceMap = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> map = applicationContext.getBeansWithAnnotation(RpcService.class);
        for (Object bean : map.values()) {
            Class<?>[] rpcInterfaces = bean.getClass().getInterfaces();
            for (Class<?> rpcInterface : rpcInterfaces) {
                String rpcInterfaceName = rpcInterface.getName();
                rpcServiceMap.put(rpcInterfaceName, bean);
                LOGGER.info("??????RPC????????????{}", rpcInterfaceName);
            }
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String portV = env.getProperty("swift.rpc.server.port");
        int port = StringUtils.isBlank(portV) ? DEFAULT_SERVER_PORT : Integer.parseInt(portV);
        String serverName = env.getProperty("swift.rpc.server.name");
        start(port, serverName);
    }

    private void start(int port, String serverName) {
        if (isRunning.get()) {
            LOGGER.info("SwiftRPC????????????????????????");
            return;
        }

        final NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup(16);
        try {
            SwiftServerHandler handler = new SwiftServerHandler(rpcServiceMap);
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new SwiftMessageDecoder());
                            pipeline.addLast(new SwiftMessageEncoder());
                            pipeline.addLast(handler);
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            isRunning.set(true);

            String rpcServer = "127.0.0.1:" + port;
            registerRpcServer(serverName, rpcServer);

            LOGGER.info("SwiftRPC??????????????????????????????{}", port);

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            isRunning.set(false);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

            throw new RuntimeException("??????SwiftRPC????????????", e);
        }
    }

    private void registerRpcServer(String serverName, String rpcServer) throws Exception {
        // ????????????rpc server???????????????redis???
        redisTemplate.opsForHash().put("rpc-server", serverName, rpcServer);
    }
}
