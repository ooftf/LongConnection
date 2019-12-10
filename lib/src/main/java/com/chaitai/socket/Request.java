package com.chaitai.socket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 暂时只支持一次订阅一个channel
 *
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/7
 */
public class Request {
    private boolean needLogin = true;
    private String op;
    private Map<String, String> args = new HashMap<>();
    private List<String> channel = new ArrayList<>();


    public Request() {
    }

    public Request(String op) {
        this.op = op;
    }

    public Request(String op, String channel) {
        this.op = op;
        this.channel.add(channel);
    }

    public String getOp() {
        return op;
    }

    public Request setOp(String op) {
        this.op = op;
        return this;
    }

    public boolean isNeedLogin() {
        return needLogin;
    }

    public Request setNeedLogin(boolean needLogin) {
        this.needLogin = needLogin;
        return this;
    }

    public String getChannelId() {
        if (channel.size() > 0) {
            return channel.get(0);
        } else {
            return "";
        }

    }


    public Request setChannel(String channel) {
        this.channel.clear();
        this.channel.add(channel);
        return this;
    }


    public Map<String, String> getArgs() {
        return args;
    }


}
