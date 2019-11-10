package com.chaitai.socket;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Call {
    Request request;
    Set<Callback> callback = new HashSet<>();
    String response;

    @NonNull
    @Override
    public String toString() {
        return super.toString() + request.getId();
    }
}
