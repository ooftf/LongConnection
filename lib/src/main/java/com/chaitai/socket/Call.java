package com.chaitai.socket;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

/**
 * 管理请求数据和callback
 */
public class Call {
    Request request;
    private Set<Callback> callbacks = new HashSet<>();
    public Set<Callback> getCallbacks() {
        return callbacks;
    }

    public void addCallback(Callback callback) {
        if (callback == null) {
            return;
        }
        this.callbacks.add(callback);
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + request.getId();
    }
}
