package com.chaitai.socket;

import androidx.annotation.NonNull;

import com.alibaba.android.arouter.facade.service.SerializationService;
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * 暂时只支持一次订阅一个channel
 *
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/7
 */
public class Request {
    String op;
    Map<String, String> args = new HashMap<>();
    List<String> channel = new ArrayList<>();

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

    @NonNull
    @Override
    public String toString() {
        return ARouter.getInstance().navigation(SerializationService.class).object2Json(this);
    }

    public String getId() {
        if (channel.size() > 0) {
            return op + channel.get(0);
        }
        return op;
    }


}
