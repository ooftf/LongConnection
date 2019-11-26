package com.chaitai.socket;

import android.annotation.SuppressLint;
import android.text.TextUtils;

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

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/7
 */
public class WSClient {
    WebSocketClient client;
    /**
     * 对订阅后事件的监听者
     */
    HashMap<String, Call> channelObserver = new HashMap<>();
    /**
     * 发送请求事件响应的监听者
     */
    Map<String, Call> callbackMap = new HashMap<>();
    /**
     * 监听  连接打开
     */
    ArrayList<Runnable> openListener = new ArrayList<>();
    /**
     * 监听  连接打开
     */
    ArrayList<Runnable> closeListener = new ArrayList<>();
    /**
     * 监听  连接打开
     */
    ArrayList<Runnable> errorListener = new ArrayList<>();
    /**
     * 将事件发送到 轮询器中
     */
    ArrayList<Runnable> postOnLooper = new ArrayList<>();
    /**
     * 将事件发送到连接成功
     */
    ArrayList<Runnable> postOnConnected = new ArrayList<>();

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
                    LogUtil.e("Socket-122", "删除前callbackMap::" + callbackMap);
                    LogUtil.e("Socket-123", "callbackMap::" + response.getRequestId());
                    Call call = callbackMap.get(response.getRequestId());
                    LogUtil.e("Socket-124", "搜索到" + call);
                    if (call != null) {
                        for (Callback callback : call.callback) {
                            if (callback == null) {
                                continue;
                            }
                            if (response.error == 0) {
                                callback.success(message);
                            } else {
                                callback.fail(message);
                            }

                        }
                        callbackMap.remove(response.getRequestId());
                    }
                    LogUtil.e("Socket-125", "删除后callbackMap::" + callbackMap);
                    Call callbacks = channelObserver.get(response.getChannelId());
                    if (callbacks != null) {
                        for (Callback callback : callbacks.callback) {
                            if (callback == null) {
                                continue;
                            }
                            callback.success(message);
                        }
                    }
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                super.onClose(code, reason, remote);
                for (Runnable runnable : closeListener) {
                    runnable.run();
                }
                for (Call call : callbackMap.values()) {
                    call.needSend = true;
                }

                for (Call call : channelObserver.values()) {
                    call.needSend = true;
                }
                //reconnect();
            }

            @Override
            public void onError(Exception ex) {
                super.onError(ex);
                for (Runnable runnable : errorListener) {
                    runnable.run();
                }
                for (Call call : callbackMap.values()) {
                    call.needSend = true;
                }

                for (Call call : channelObserver.values()) {
                    call.needSend = true;
                }
                //reconnect();
            }
        };

        Observable.interval(10, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                LogUtil.e("Socket-interval", "callbackMap::" + callbackMap + "  channelObserver::" + channelObserver);
                boolean hasObserver = isNeedConnection();
                if (hasObserver) {
                    if (checkConnection()) {
                        client.send("ping");
                    }
                }
                for (Runnable runnable : postOnLooper) {
                    runnable.run();
                }
                postOnLooper.clear();
            }
        });
    }

    protected boolean isNeedConnection() {
        return callbackMap.size() > 0 || channelObserver.size() > 0;
    }


   /* @SuppressLint("CheckResult")
    public void subscribe(final Request request, final Callback callback) {

        Completable.complete().subscribeOn(Schedulers.io()).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                subscribeBlocking(request, callback);
            }
        });
    }*/

    public void subscribe(final Request request, final Callback callback) {
        final String channel = request.getChannelId();
        if (TextUtils.isEmpty(channel)) {
            callback.fail("subscribe no channel");
            return;
        }

        send(request, new Callback() {
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
        LogUtil.e("Socket-checkConnection", "checkConnection" + "  isClosed::" + client.isClosed() + "   client.isOpen()::" + client.isOpen());
        if (client.isClosed()) {
            try {
                client.reconnect();
            } catch (Exception e) {
                try {
                    client.connect();
                } catch (Exception e1) {
                    e.printStackTrace();
                }
                e.printStackTrace();
            }
            return false;
        } else if (!client.isOpen()) {
            try {
                client.connect();
            } catch (Exception e) {
                try {
                    client.reconnect();
                } catch (Exception e1) {
                    e.printStackTrace();
                }
                e.printStackTrace();
            }
            return false;
        } else if (client.isClosing()) {
            return false;
        }
        return true;
    }

    public void unsubscribe(final Request request, final Callback callback) {
        final String channel = request.getChannelId();
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
        }

        channelObserver.remove(channel);
        send(request, new Callback() {
            @Override
            public void success(String message) {

            }

            @Override
            public void fail(String message) {

            }
        });
    }

    private String genCallbackId(Request request, Callback callback) {
        if (callback == null) {
            return request.getId();
        } else {
            return callback.hashCode() + callback.toString() + request.getId();
        }

    }


    public DisposableConsole send(final Request request, final Callback callback) {
        Call call = callbackMap.get(request.getId());
        if (call == null) {
            if (checkConnection()) {
                call = new Call();
                call.request = request;
                call.callback.add(callback);
                try {
                    client.send(ARouter.getInstance().navigation(SerializationService.class).object2Json(call.request));
                    callbackMap.put(call.request.getId(), call);
                } catch (Exception e) {
                    if (!WebSocketService.OP_LOGIN.equals(request.getOp())) {
                        throw e;
                    } else {
                        postOnConnected.add(new Runnable() {
                            @Override
                            public void run() {
                                send(request, callback);
                            }
                        });
                    }
                }
            } else {
                LogUtil.e("WSClient", "Socket未连接，推迟到连接后");
                postOnConnected.add(new Runnable() {
                    @Override
                    public void run() {
                        send(request, callback);
                    }
                });
            }
        } else {
            call.callback.add(callback);
            if (call.needSend) {
                client.send(ARouter.getInstance().navigation(SerializationService.class).object2Json(call.request));
            }
        }
        return new DisposableConsole(this, genCallbackId(request, callback));
    }


    public void addOpenListener(Runnable runnable) {
        openListener.add(runnable);
    }

    public void addCloseListener(Runnable runnable) {
        closeListener.add(runnable);
    }

    public void addErrorListener(Runnable runnable) {
        errorListener.add(runnable);
    }

    public void removeOpenListener(Runnable runnable) {
        openListener.remove(runnable);
    }

    public void removeCloseListener(Runnable runnable) {
        closeListener.remove(runnable);
    }

    public void removeErrorListener(Runnable runnable) {
        errorListener.remove(runnable);
    }

    public void postOnLooper(Runnable runnable) {
        postOnLooper.add(runnable);
    }


    public void cancel(String id) {
        Iterator<Map.Entry<String, Call>> iteratorMap = channelObserver.entrySet().iterator();
        while (iteratorMap.hasNext()) {
            Map.Entry<String, Call> entry = iteratorMap.next();
            Iterator<Callback> iterator = entry.getValue().callback.iterator();
            while (iterator.hasNext()) {
                Callback next = iterator.next();
                if (id.equals(genCallbackId(entry.getValue().request, next))) {
                    next.fail("cancel");
                    iterator.remove();
                    if (entry.getValue().callback.size() == 0) {
                        iteratorMap.remove();
                    }
                    return;
                }
            }
        }
    }

}
