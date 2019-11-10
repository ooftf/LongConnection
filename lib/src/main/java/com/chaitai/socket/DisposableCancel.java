package com.chaitai.socket;

public class DisposableCancel {
    WSClient client;
    String id;

    public DisposableCancel(WSClient client, String id) {
        this.client = client;
        this.id = id;
    }

    public void cancel() {
        client.cancel(id);
    }
}
