package cn.hncj.netty;


import java.io.Serializable;

/**
 * @Author FanJian
 * @Date 2022-12-26 20:48
 */

public class DataContent implements Serializable {

    private static final long serialVersionUID = 4285480089620344703L;

    /**
     * 动作类型
     */
    private Integer action;
    private ChatMsg chatMsg;
    /**
     * 扩展字段
     */
    private String extand;

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public ChatMsg getChatMsg() {
        return chatMsg;
    }

    public void setChatMsg(ChatMsg chatMsg) {
        this.chatMsg = chatMsg;
    }

    public String getExtand() {
        return extand;
    }

    public void setExtand(String extand) {
        this.extand = extand;
    }
}
