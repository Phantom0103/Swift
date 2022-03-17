package com.wenxia.swift.client;

import com.wenxia.swift.common.codec.KryoDecoder;
import com.wenxia.swift.common.codec.KryoEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * @author zhouw
 * @date 2022-03-17
 */
public class SwiftClientRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftClientRunner.class);

    @Autowired
    private Environment env;

    private Bootstrap bootstrap = new Bootstrap();
    private NioEventLoopGroup group = new NioEventLoopGroup();

    //@Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        Map<String, Object> map = applicationContext.getBeansWithAnnotation(RpcService.class);
//        for (Object bean : map.values()) {
//            String beanName = bean.getClass().getName();
//            rpcServiceMap.put(beanName, bean);
//            LOGGER.info("SwiftRPC server loaded service: " + beanName);
//        }
    }

    // @Override
    public void run(ApplicationArguments args) throws Exception {
        String port = env.getProperty("swift.rpc.server.port", "5022");
        //start(Integer.valueOf(port));
    }

    private Channel connect() {
        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new KryoEncoder());
                            pipeline.addLast(new KryoDecoder());
                            //pipeline.addLast(handler);
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect("", 5022).sync();
            LOGGER.info("SwiftRPC client is already connected to the host: " + "");
            return channelFuture.channel();
        } catch (Exception e) {
            group.shutdownGracefully();
            throw new RuntimeException("Failed to start SwiftRPC client", e);
        }
    }
}
