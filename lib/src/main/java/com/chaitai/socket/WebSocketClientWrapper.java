package com.chaitai.socket;

import android.util.Log;

import androidx.annotation.CallSuper;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/8
 */
public class WebSocketClientWrapper extends WebSocketClient {
    public WebSocketClientWrapper(URI serverUri) {
        super(serverUri);
    }

    public WebSocketClientWrapper(URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft);
    }

    public WebSocketClientWrapper(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    public WebSocketClientWrapper(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders) {
        super(serverUri, protocolDraft, httpHeaders);
    }

    public WebSocketClientWrapper(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
    }

    @CallSuper
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LogUtil.e("Socket", "onOpen");
    }

    @CallSuper
    @Override
    public void onMessage(String message) {
        LogUtil.e("Socket", "onMessage::" + message);
    }

    @CallSuper
    @Override
    public void onClose(int code, String reason, boolean remote) {
        LogUtil.e("Socket", "onClose::" + code + "-" + reason + "-" + remote);
    }

    @CallSuper
    @Override
    public void onError(Exception ex) {
        Log.e("Socket", "onError::" + ex.toString());
    }


    @Override
    public void send(String text) {
        LogUtil.e("Socket", "send::" + text);
        super.send(text);
    }
}
