package cn.hncj.netty;

import cn.hncj.enums.MsgActionEnum;
import cn.hncj.service.UserService;
import cn.hncj.utils.JsonUtils;
import cn.hncj.utils.SpringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import tk.mybatis.spring.mapper.SpringBootBindUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.hncj.enums.MsgActionEnum.*;

/**
 * @Author FanJian
 * @Date 2022/12/9 18:28
 */

public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

   private static ChannelGroup users =
            new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * TextWebSocketFrame : 在netty中，是专门用于处理文本的对象，frame是消息载体
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // 获取客户端传输过来的消息
        String content = msg.text();
        Channel currentChannel = ctx.channel();
        // 1. 获取客户端发的消息
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        Integer action = dataContent.getAction();
        // 2. 判断消息的类型
        if (action.equals(CONNECT.type)) {
            // 2.1 当websocket第一次open的时候，初始化channel，把用户的channel和userid关联起来
            String sendUserId = dataContent.getChatMsg().getSenderId();
            UserChannelRel.put(sendUserId,currentChannel);

            for (Channel c : users) {
                System.out.println(c.id().asLongText());
            }
            UserChannelRel.output();
        } else if (action.equals(CHAT.type)) {
            // 2.2 聊天类型的消息，把聊天记录保存在数据库，同时标记消息的签收状态[未签收]
            ChatMsg chatMsg = dataContent.getChatMsg();
            String msgText = chatMsg.getMsg();
            String sendUserId = chatMsg.getSenderId();
            String receiverId = chatMsg.getReceiverId();
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            String msgId = userService.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);


            // 发送消息
            // 从全局中获取接收方的channel
            Channel channel = UserChannelRel.get(receiverId);
            if (channel == null) {
                // TODO 用户离线，推送消息
            } else {
                // 当channel不为空的时候，从ChannelGroup中去查找对应的channel是否存在
                Channel findChannel = users.find(channel.id());
                if (findChannel != null) {
                    channel.writeAndFlush(new TextWebSocketFrame(
                            JsonUtils.objectToJson(chatMsg)
                    ));
                } else {
                    // 推送消息
                }
            }

        } else if (action.equals(SIGNED.type)) {
            // 2.3 签收类型的消息，针对具体的消息进行签收，修改数据库中对应消息的牵手状态[已签收]
            UserService userService = (UserService) SpringUtil.getBean("userServiceImpl");
            String msgIdStr = dataContent.getExtand();
            String[] msgIds = msgIdStr.split(",");
            List<String> msgIdList = new ArrayList<>();
            for (String msgId : msgIds) {
                if (StringUtils.isNotBlank(msgId)) {
                    msgIdList.add(msgId);
                }
            }

            if (msgIdList != null && !msgIdList.isEmpty() && msgIdList.size() > 0) {
                // 批量签收
                userService.updateMsgSigned(msgIdList);
            }

        } else if (action.equals(KEEPALIVE.type)) {
            // 2.4 心跳类型
        }

        // 3.
    }


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        users.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // 这个方法执行会自动删除对应的channel
        users.remove(ctx.channel());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        users.remove(ctx.channel());
    }
}

