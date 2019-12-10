package com.chaitai.socket;

import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/12/10
 */
public class SubscribePool {
    private HashMap<String, Call> pool = new HashMap<>();

    public HashMap<String, Call> getPool() {
        return pool;
    }


    @Nullable
    public Call findCallByChannel(String channel) {
        return pool.get(channel);
    }

    boolean isEmpty() {
        return pool.size() == 0;
    }


    void put(Call call) {
        pool.put(call.getChannel(), call);
    }

    void remove(String channel) {
        pool.remove(channel);
    }

    void onResponse(String channel, String message) {
        LogUtil.e("onResponse::" + toString());
        Call callByChannel = findCallByChannel(channel);
        if (callByChannel != null) {
            for (Callback callback : callByChannel.getCallbacks()) {
                if (callback == null) {
                    continue;
                }
                callback.success(message);
            }
        }
    }

    void restore(WSClient client) {
        for (Map.Entry<String, Call> entry : getPool().entrySet()) {
            Call value = entry.getValue();
            Log.e("WebSocketService", "恢复subscribe::" + value.getRequestId());
            client.subscribe(value.newRequest(), null);
        }
    }

    @Override
    public String toString() {
        return "SubscribePool{" +
                "channelObserver=" + pool +
                '}';
    }
}
