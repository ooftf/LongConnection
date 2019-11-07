package com.chaitai.socket;

import android.annotation.SuppressLint;
import android.util.Log;

import com.alibaba.android.arouter.facade.service.SerializationService;
import com.alibaba.android.arouter.launcher.ARouter;

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
                for (Call message : postOnConnected) {
                    Log.e("HiClient", "send::" + message);
                    HiClient.this.send(message);
                }
                postOnConnected.clear();
            }

            @Override
            public void onMessage(String message) {
                Log.e("HiClient", "onMessage::" + message);
                if (message.equals("pong")) {
                    // todo 服务端心跳
                } else {
                    BaseResponse response = ARouter.getInstance().navigation(SerializationService.class).parseObject(message, BaseResponse.class);
                    Call call = callbackMap.get(response.event);
                    if (call != null) {
                        call.callback.success(message);
                        callbackMap.remove(call);
                    }

                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.e("HiClient", "onClose::" + code + "-" + "reason" + "-" + remote);
                for (Map.Entry<String, Call> call : callbackMap.entrySet()) {
                    call.getValue().callback.fail(reason);
                }
                callbackMap.clear();
            }

            @Override
            public void onError(Exception ex) {
                Log.e("HiClient", "onError::" + ex.toString());
                for (Map.Entry<String, Call> call : callbackMap.entrySet()) {
                    call.getValue().callback.fail(ex.getMessage());
                }
                callbackMap.clear();
            }
        };

        Observable.interval(10, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                boolean hasObserver = callbackMap.size() > 0 || channel.size() > 0;
                if (hasObserver) {
                    client.send("ping");
                }
            }
        });
    }

    HashMap<String, Callback> channel = new HashMap<>();

    @Override
    public void subscribe(final BaseRequest key, final Callback callback) {

        Completable.complete().subscribeOn(Schedulers.io()).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                subscribeBlocking(key, callback);
            }
        });
    }

    public void subscribeBlocking(final BaseRequest request, final Callback callback) {
        sendBlocking(request, new Callback() {
            @Override
            public void success(String message) {
                channel.put(request.args.get("channel"), callback);
            }

            @Override
            public void fail(String message) {
                callback.fail(message);
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
    public void unSubscribe(BaseRequest key) {

    }

    Map<String, Call> callbackMap = new HashMap<>();

    @Override
    public void send(final BaseRequest request, final Callback callback) {
        Disposable subscribe = Completable.complete().subscribeOn(Schedulers.io()).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                sendBlocking(request, callback);
            }
        });
    }

    public void send(final Call call) {
        Disposable subscribe = Completable.complete().subscribeOn(Schedulers.io()).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                sendBlocking(call);
            }
        });
    }

    public void sendBlocking(final BaseRequest request, final Callback callback) {
        Call call = new Call();
        call.request = request;
        call.callback = callback;
        sendBlocking(call);
    }

    public void sendBlocking(Call call) {
        if (checkConnection()) {
            client.send(call.request.toString());
            callbackMap.put(call.request.op, call);
        } else {
            postOnConnected.add(call);
        }
    }


    ArrayList<Call> postOnConnected = new ArrayList<>();
}
