package com.chaitai.socket;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/7
 */
public class HiClient implements IHiClient {
    WebSocketClient client;

    @SuppressLint("CheckResult")
    public HiClient(String url) {


        URI uri = URI.create(url);
        client = new WebSocketClient(uri, new Draft_6455()) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.e("HiClient", "onOpen");
                for (String message : postSend) {
                    Log.e("HiClient", "send::" + message);
                    client.send(message);
                }
            }

            @Override
            public void onMessage(String message) {
                Log.e("HiClient", "onMessage::" + message);
                if (message.equals("pong")) {
                    // todo 服务端心跳
                } else {
                    for (Map.Entry<String, Callback> entry : callbackMap.entrySet()) {
                        if (entry.getKey().equals(message)) {
                            callbackMap.remove(message);
                        }

                    }
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.e("HiClient", "onClose::" + code + "-" + "reason" + "-" + remote);
            }

            @Override
            public void onError(Exception ex) {
                Log.e("HiClient", "onError::" + ex.toString());
            }
        };

        Observable.interval(10, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                boolean hasObserver = callbackMap.size() > 0 || map.size() > 0;
                if (hasObserver) {
                    if (checkConnection()) {
                        client.send("ping");
                    }
                }
            }
        });
    }

    HashMap<String, Callback> map = new HashMap<>();

    @Override
    public void subscribe(final String key, final Callback callback) {

        Completable.complete().subscribeOn(Schedulers.io()).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                if(checkConnection()){
                    map.put(key, callback);
                }else{
                    callback.fail("connection error");
                }
            }
        });
    }

    private boolean checkConnection() {
        Log.e("HiClient", "checkConnection" + "  isClosed::" + client.isClosed() + "   client.isOpen()::" + client.isOpen());
        if (!client.isOpen()) {
            try {
                return client.connectBlocking(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } else if (client.isClosed()) {
            try {
                return client.reconnectBlocking();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } else if (client.isClosing()) {
            return false;
        }
        return true;
    }

    @Override
    public void unSubscribe(String key) {

    }

    Map<String, Callback> callbackMap = new HashMap<>();

    @Override
    public void send(final String string, final Callback callback) {
        Disposable subscribe = Completable.complete().subscribeOn(Schedulers.io()).subscribe(new Action() {
            @Override
            public void run() throws Exception {

              sendBlocking(string,callback);
            }
        });
    }

    public void sendBlocking(final String string, final Callback callback) {
        if (checkConnection()) {
            client.send(string);
            callbackMap.put(string, callback);
        } else {
            callback.fail("connection error");
        }
    }



    ArrayList<String> postSend = new ArrayList<>();

}
