package com.chaitai.socket;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/7
 */
public class Response {
    String event;
    String msg;
    String channel;
    int error;

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getRequestId() {
        return event + channel;
    }
}
