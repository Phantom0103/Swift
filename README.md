## 用Netty实现一个简单的RPC框架
Netty是基于事件驱动的，异步的NIO网络应用框架，可以用来开发高性能，高可靠性的网络IO程序。市面上很多RPC框架底层IO通信都是用Netty实现的，这个项目的主要目的是通过实践加强对Netty应用的掌握，另外也可以通过这个项目来加深对RPC的理解。
### RPC是什么
RPC是远程过程调用（Remote Procedure Call）的缩写形式。是跨应用之间调用的一个解决方案，通过在客户端和服务端之间建立TCP链接，发送请求序列化反序列化交换数据，让调用者像使用本地API一样简单。经常会有人纠结为什么不用HTTP，其实HTTP也是实现RPC的一种方式。
### 实现过程
#### 协议
在说到服务端和客户端实现之前，先定义好相关的RPC协议。  
*SwiftMessage*，pipeline中流转的消息POJO：
```java
public class SwiftMessage {

    private int length;
    private byte[] content;

    public SwiftMessage(int length, byte[] content) {
        this.length = length;
        this.content = content;
    }
    
    // 省略setter、getter
}
```
*RpcRequest*，*RpcResponse*，handler中处理的请求响应对象：
```java
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 2104861261275175620L;

    private String id;
    private String className;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] parameterTypes;
}
```
```java
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = -1921327887856337850L;

    private String requestId;
    private int code;
    private String errorMsg;
    private Object data;
}
```
*ByteBuf*与*SwiftMessage*转换的编码器和解码器：
```java
public class SwiftMessageEncoder extends MessageToByteEncoder<SwiftMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, SwiftMessage msg, ByteBuf out) throws Exception {
        out.writeInt(msg.getLength());
        out.writeBytes(msg.getContent());
    }
}
```
```java
public class SwiftMessageDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readInt();
        byte[] content = new byte[length];
        in.readBytes(content);
        out.add(new SwiftMessage(length, content));
    }
}
```
#### 服务端
其实RPC Server简单些，只需要做下面几件事就可以：
1. 启动服务，把自己注册到RPC服务中心（让客户端可以找到能建立连接）
```java
@Component
public class SwiftServerRunner implements ApplicationRunner, ApplicationContextAware {

    private void start(int port, String serverName) {
        if (isRunning.get()) {
            LOGGER.info("SwiftRPC服务已经在运行中");
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

            LOGGER.info("SwiftRPC服务已经启动，端口：{}", port);

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            isRunning.set(false);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

            throw new RuntimeException("启动SwiftRPC服务失败", e);
        }
    }

    private void registerRpcServer(String serverName, String rpcServer) throws Exception {
        // 简单的将rpc server列表保存到redis中
        redisTemplate.opsForHash().put("rpc-server", serverName, rpcServer);
    }
}
```
2. 监听客户端的连接，接收请求数据
3. 执行具体的业务实现方法，把结果写回到与客户端的连接中
```java
@ChannelHandler.Sharable
public class SwiftServerHandler extends SimpleChannelInboundHandler<SwiftMessage> {
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SwiftMessage msg) throws Exception {
        byte[] content = msg.getContent();
        RpcRequest request = Kryos.deserialize(content, RpcRequest.class);

        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getId());

        try {
            Object result = handler(request);
            response.setData(result);
        } catch (Exception e) {
            response.setCode(-1);
            response.setErrorMsg(e.toString());
        }

        byte[] resContent = Kryos.serialize(response);
        SwiftMessage message = new SwiftMessage(resContent.length, resContent);
        ctx.writeAndFlush(message);
    }

    private Object handler(RpcRequest request) throws Exception {
        String className = request.getClassName();
        Object rpcService = rpcServiceMap.get(className);
        if (rpcService == null) {
            throw new RuntimeException("未找到RPC服务类：" + className);
        }

        // 通过反射调用业务层
        String methodName = request.getMethodName();
        Object[] parameters = request.getParameters();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Class<?> clazz = rpcService.getClass();
        Method method = clazz.getMethod(methodName, parameterTypes);

        return method.invoke(rpcService, parameters);
    }
}
```
#### 客户端
客户端稍微复杂些，因为客户端需要接收外部的调用请求（比如一个rest接口发来的请求），根据不同的请求选择不同的RPC服务端发送请求，最后还需要等待返回的结果。
1. 注册RPC接口。因为要想通过注入的方式调用service的方法，就需要将service注册成Spring的bean
```java
@Autowired
private UserService userService;
```
但是UserService具体的实现类在服务端，所以需要自定义一个ComponentScan，扫描所有的RPC接口，注册成Spring的bean，并设置对应的FactoryBean来完成实例化工作，在FactoryBean中用动态代理来生成每个接口的代理类执行各个方法。所以最终的RPC调用是在这个代理类的invoke方法中发起的。
```java
public class SwiftRpcFactory<T> implements InvocationHandler {

    private Class<T> rpcInterface;
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String className = rpcInterface.getName();
        String methodName = method.getName();

        SwiftRpcService rpcService = rpcInterface.getAnnotation(SwiftRpcService.class);
        String rpcServer = rpcService.server();
        RpcRequest request = new RpcRequest();
        request.setId(UUID.randomUUID().toString());
        request.setClassName(rpcInterface.getName());
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());

        SwiftClientRunner swiftClient = ApplicationContextHolder.getApplicationContext().getBean(SwiftClientRunner.class);
        return swiftClient.sendRequest(request, rpcServer);
    }
}
```
2. 选择对应RPC Server的连接，发送请求，并等待结果返回
```java
public Object sendRequest(RpcRequest request, String rpcServer) throws InterruptedException, IOException {
    Channel channel = getChannel(rpcServer);
    if (channel != null && channel.isActive()) {
        // 发送请求后阻塞等待返回结果
        SynchronousQueue<Object> queue = swiftClientHandler.sendRequest(request, channel);
        return queue.poll(60, TimeUnit.SECONDS);
    }

    return null;
}
```
```java
@Component
@ChannelHandler.Sharable
public class SwiftClientHandler extends SimpleChannelInboundHandler<SwiftMessage> {

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
}
```