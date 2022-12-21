package cn.hncj.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @Author FanJian
 * @Date 2022/12/9 12:50
 */

public class WSServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        // websocket是基于http的
        pipeline.addLast(new HttpServerCodec());
        // 对写大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());
        // 几乎在所有的netty编程中都会用到这个handler
        pipeline.addLast(new HttpObjectAggregator(1024 * 64));
        // ======================== 以上是支持http===============================
        // ws协议路由
        // 会帮你处理1. 握手 2. close 3. 心跳（ping + pong）
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        // 自定义handler，向客户端读取和写数据
        pipeline.addLast(new ChatHandler());
    }
}

