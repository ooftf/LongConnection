package com.chaitai.socket;

import androidx.annotation.NonNull;

import com.alibaba.android.arouter.facade.service.SerializationService;
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/7
 */
public class Request {
    String op;
    Map<String, String> args = new HashMap<>();

    public Request() {
    }

    public Request(String op) {
        this.op = op;
    }

    public Request(String op, String channel) {
        this.op = op;
        this.args.put(WSClient.CHANNEL, channel);
    }

    public String getOp() {
        return op;
    }

    public Request setOp(String op) {
        this.op = op;
        return this;
    }
    public Request setChannel(String channel) {
        this.args.put(WSClient.CHANNEL, channel);
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
        return op + args.get(WSClient.CHANNEL);
    }
}
