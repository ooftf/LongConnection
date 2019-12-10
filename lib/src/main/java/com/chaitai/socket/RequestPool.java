package com.chaitai.socket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RequestPool {
    private Map<String, Call> callbackMap = new HashMap<>();

    @Nullable
    public Call findCallByRequestId(String requestId) {
        return callbackMap.get(requestId);
    }

    public void remove(String requestId) {
        callbackMap.remove(requestId);
    }

    public boolean isEmpty() {
        return callbackMap.size() == 0;
    }

    public void put(Call call) {
        callbackMap.put(call.getRequestId(), call);
    }

    public void restore(WSClient client){
        for (Map.Entry<String, Call> entry : callbackMap.entrySet()) {
            Call value = entry.getValue();
            LogUtil.e("WebSocketService", "恢复send::" + value.getRequestId());
            client.send(value.newRequest(), null);
        }
    }

    @Override
    public String toString() {
        return "RequestPool{" +
                "callbackMap=" + callbackMap +
                '}';
    }
}
