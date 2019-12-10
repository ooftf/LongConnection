package com.chaitai.socket;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.alibaba.android.arouter.facade.service.SerializationService;
import com.alibaba.android.arouter.launcher.ARouter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 管理请求数据和callback
 */
public class Call {
    private String op;
    private Map<String, String> args = new HashMap<>();
    private String channel;
    private boolean needLogin;
    private Set<Callback> callbacks = new HashSet<>();

    public String getChannel() {
        return channel;
    }

    public Call setChannel(String channel) {
        this.channel = channel;
        return this;
    }


    public boolean equalsCurrent(Request request) {
        if (!Objects.equals(request.getOp(), op)) {
            return false;
        }
        if (!Objects.equals(request.getChannelId(), channel)) {
            return false;
        }
        if (request.isNeedLogin() != needLogin) {
            return false;
        }
        if (!Objects.equals(request.getArgs().toString(), args.toString())) {
            return false;
        }
        return true;
    }

    public Call(Request request) {
        this.op = request.getOp();
        this.channel = request.getChannelId();
        copyMap(request.getArgs(), args);
        needLogin = request.isNeedLogin();
    }

    private <K, V> void copyMap(Map<K, V> src, Map<K, V> dest) {
        for (Map.Entry<K, V> entry : src.entrySet()) {
            dest.put(entry.getKey(), entry.getValue());
        }
    }


    public Set<Callback> getCallbacks() {
        return callbacks;
    }

    public void addCallback(Callback callback) {
        if (callback == null) {
            return;
        }
        this.callbacks.add(callback);
    }

    public Request newRequest() {
        Request request = new Request(op, channel);
        copyMap(args, request.getArgs());
        request.setNeedLogin(needLogin);
        return request;
    }

    String getRequestString() {
        return ARouter.getInstance().navigation(SerializationService.class).object2Json(new ReqeustStringBean(op, args, channel));
    }

    String getRequestId() {
        return op + "&" + channel;
    }

    public static String genRequestId(Request request) {
        return request.getOp() + "&" + request.getChannelId();
    }

    public static String genRequestId(Response response) {
        return response.event + "&" + response.getChannelId();
    }


    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }

    @Keep
    private class ReqeustStringBean {
        String op;
        Map<String, String> args = new HashMap<>();
        List<String> channel = new ArrayList<>();

        public ReqeustStringBean(String op, Map<String, String> args, String channel) {
            this.op = op;
            this.args = args;
            this.channel.add(channel);
        }
    }
}
