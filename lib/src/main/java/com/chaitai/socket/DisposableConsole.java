package com.chaitai.socket;

/**
 * @author 99474
 */
public class DisposableConsole {
    WSClient client;
    String id;

    public DisposableConsole(WSClient client, String id) {
        this.client = client;
        this.id = id;
    }

    public void cancel() {
        client.cancel(id);
    }
}
