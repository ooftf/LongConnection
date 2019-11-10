package com.chaitai.socket;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.android.arouter.facade.service.SerializationService;
import com.alibaba.android.arouter.launcher.ARouter;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
public class WSClient {
    public static final String CHANNEL = "channel";
    WebSocketClient client;

    @SuppressLint("CheckResult")
    public WSClient(String url) {

        URI uri = URI.create(url);

        client = new WebSocketClientWapper(uri, new Draft_6455()) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                super.onOpen(handshakedata);
                for (Runnable runnable : openListener) {
                    runnable.run();
                }
                for (Runnable runnable : postOnConnected) {
                    runnable.run();
                }
                postOnConnected.clear();
            }

            @Override
            public void onMessage(String message) {
                super.onMessage(message);

                if (message.equals("pong")) {
                    // todo 服务端心跳
                } else {
                    Response response = ARouter.getInstance().navigation(SerializationService.class).parseObject(message, Response.class);
                    Log.e("Socket-122", "删除前callbackMap::" + callbackMap);
                    Log.e("Socket-123", "callbackMap::" + response.getRequestId());
                    Call call = callbackMap.get(response.getRequestId());
                    Log.e("Socket-124", "搜索到" + call);
                    if (call != null) {
                        for (Callback callback : call.callback) {
                            if (response.error == 0) {
                                callback.success(message);
                            } else {
                                callback.fail(message);
                            }

                        }
                        callbackMap.remove(response.getRequestId());
                    }
                    Log.e("Socket-125", "删除后callbackMap::" + callbackMap);
                    Call callbacks = channelObserver.get(response.channel);
                    if (callbacks != null) {
                        for (Callback callback : callbacks.callback) {
                            callback.success(message);
                        }
                    }
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                super.onClose(code, reason, remote);
                //reconnect();
            }

            @Override
            public void onError(Exception ex) {
                super.onError(ex);
                //reconnect();
            }
        };

        Observable.interval(10, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                Log.e("Socket-interval", "callbackMap::" + callbackMap + "  channelObserver::" + channelObserver);
                boolean hasObserver = callbackMap.size() > 0 || channelObserver.size() > 0;
                if (hasObserver) {
                    client.send("ping");
                }
                for (Runnable runnable : postOnLooper) {
                    runnable.run();
                }
                postOnLooper.clear();
            }
        });
    }

    HashMap<String, Call> channelObserver = new HashMap<>();

    @SuppressLint("CheckResult")
    public void subscribe(final Request request, final Callback callback) {

        Completable.complete().subscribeOn(Schedulers.io()).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                subscribeBlocking(request, callback);
            }
        });
    }

    public void subscribeBlocking(final Request request, final Callback callback) {
        final String channel = request.getArgs().get(CHANNEL);
        if (TextUtils.isEmpty(channel)) {
            callback.fail("subscribe no channel");
            return;
        }

        sendBlocking(request, new Callback() {
            @Override
            public void success(String message) {
                Call callbacks = channelObserver.get(channel);
                if (callbacks == null) {
                    callbacks = new Call();
                    callbacks.request = request;
                }
                if (callback != null) {
                    callbacks.callback.add(callback);
                }
                WSClient.this.channelObserver.put(channel, callbacks);
            }

            @Override
            public void fail(String message) {
                callback.fail(message);
            }
        });

    }


    public boolean checkConnection() {
        Log.e("Socket-checkConnection", "checkConnection" + "  isClosed::" + client.isClosed() + "   client.isOpen()::" + client.isOpen());
        if (client.isClosed()) {
            try {
                return client.reconnectBlocking();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } else if (!client.isOpen()) {
            try {
                return client.connectBlocking();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        } else if (client.isClosing()) {
            return false;
        }
        return true;
    }

    public void unSubscribe(final Request request, final Callback callback) {
        final String channel = request.args.get(CHANNEL);
        if (TextUtils.isEmpty(channel)) {
            callback.fail("subscribe no channel");
            return;
        }
        Call call = channelObserver.get(channel);
        if (call == null) {
            return;
        }
        call.callback.remove(callback);
        if (call.callback.size() > 0) {
            return;
        } else {
            channelObserver.remove(channel);
        }
        send(request, new Callback() {
            @Override
            public void success(String message) {

            }

            @Override
            public void fail(String message) {

            }
        });
    }

    Map<String, Call> callbackMap = new HashMap<>();

    public DisposableCancel send(final Request request, final Callback callback) {

        Disposable subscribe = Completable.complete().subscribeOn(Schedulers.io()).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                sendBlocking(request, callback);
            }
        });
        return new DisposableCancel(this, genCallbackId(request, callback));
    }

    private String genCallbackId(Request request, Callback callback) {
        return callback.hashCode() + callback.toString() + request.getId();
    }


    public void sendBlocking(final Request request, final Callback callback) {
        Call call = callbackMap.get(request.getId());
        if (call == null) {
            call = new Call();
            call.request = request;
            call.callback.add(callback);
            if (checkConnection()) {
                client.send(ARouter.getInstance().navigation(SerializationService.class).object2Json(call.request));
                callbackMap.put(call.request.getId(), call);
            } else {
                Log.e("WSClient", "推迟到链接后");
                postOnConnected.add(new Runnable() {
                    @Override
                    public void run() {
                        sendBlocking(request, callback);
                    }
                });
            }
        } else {
            call.callback.add(callback);
        }

    }

    ArrayList<Runnable> openListener = new ArrayList<>();

    public void addOpenListener(Runnable runnable) {
        openListener.add(runnable);
    }

    public void postOnLooper(Runnable runnable) {
        postOnLooper.add(runnable);
    }

    ArrayList<Runnable> postOnLooper = new ArrayList<>();
    ArrayList<Runnable> postOnConnected = new ArrayList<>();

    public void cancel(String id) {
        for (Map.Entry<String, Call> entry : channelObserver.entrySet()) {
            Iterator<Callback> iterator = entry.getValue().callback.iterator();
            while (iterator.hasNext()) {
                Callback next = iterator.next();
                if (id.equals(genCallbackId(entry.getValue().request, next))) {
                    next.fail("cancel");
                    iterator.remove();
                    if (entry.getValue().callback.size() == 0) {
                        channelObserver.remove(entry.getKey());
                    }
                    return;
                }
            }


        }
    }
}
