package com.chaitai.socket;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/7
 */
public class Response {
    String event;
    String msg;
    List<String> channel = new ArrayList<>();
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

    public Response setChannel(List<String> channel) {
        this.channel = channel;
        return this;
    }

    public String getChannelId() {
        if (channel.size() > 0) {
            return channel.get(0);
        } else {
            return "";
        }

    }
}
