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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/7
 */
public abstract class WSClient {
    public static final String PONG = "pong";
    private boolean isLogin = false;
    WebSocketClient client;
    /**
     * 对订阅后事件的监听者
     */
    private SubscribePool subscribePool = new SubscribePool();
    /**
     * 发送请求事件响应的监听者
     */
    private RequestPool requestPool = new RequestPool();
    /**
     * 监听  连接打开
     */
    private ArrayList<Runnable> openListener = new ArrayList<>();
    /**
     * 监听  连接打开
     */
    private ArrayList<Runnable> closeListener = new ArrayList<>();
    /**
     * 监听  连接打开
     */
    private ArrayList<Runnable> errorListener = new ArrayList<>();
    /**
     * 将事件发送到 轮询器中
     */
    private ArrayList<Runnable> postOnLooper = new ArrayList<>();
    /**
     * 将事件发送到连接成功
     */
    private ArrayList<Runnable> postOnConnected = new ArrayList<>();

    @SuppressLint("CheckResult")
    public WSClient(String url) {

        URI uri = URI.create(url);

        client = new WebSocketClientWrapper(uri, new Draft_6455()) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                super.onOpen(handshakedata);
                restoreRequest();
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

                if (PONG.equals(message)) {
                    // todo 服务端心跳
                } else {
                    Response response = ARouter.getInstance().navigation(SerializationService.class).parseObject(message, Response.class);
                    LogUtil.e("删除前" + requestPool);
                    LogUtil.e("收到::" + response.getChannelId());
                    Call call = requestPool.findCallByRequestId(Call.genRequestId(response));
                    LogUtil.e("搜索到" + call);
                    if (call != null) {
                        for (Callback callback : call.getCallbacks()) {
                            if (callback == null) {
                                continue;
                            }
                            if (response.error == 0) {
                                callback.success(message);
                            } else {
                                callback.fail(message);
                            }

                        }
                        requestPool.remove(Call.genRequestId(response));
                    }

                    subscribePool.onResponse(response.getChannelId(), message);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                super.onClose(code, reason, remote);
                isLogin = false;
                for (Runnable runnable : closeListener) {
                    runnable.run();
                }

                //reconnect();
            }

            @Override
            public void onError(Exception ex) {
                super.onError(ex);
                isLogin = false;
                for (Runnable runnable : errorListener) {
                    runnable.run();
                }
                //reconnect();
            }
        };

        Observable.interval(10, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                LogUtil.e("Socket-interval", requestPool + "&" + subscribePool);
                boolean hasObserver = isNeedConnection();
                if (hasObserver) {
                    if (checkConnection()) {
                        try {
                            client.send("ping");
                        } catch (Exception e) {
                            LogUtil.e("发送新跳异常" + e.toString());
                            e.printStackTrace();
                        }

                    }
                }
                for (Runnable runnable : postOnLooper) {
                    runnable.run();
                }
                postOnLooper.clear();
            }
        });
    }


    public void reconnect() {
        client.reconnect();
    }

    public WSClient setLogin(boolean login) {
        isLogin = login;
        return this;
    }

    public boolean isLogin() {
        return isLogin;
    }

    protected boolean isNeedConnection() {
        return !requestPool.isEmpty() || !subscribePool.isEmpty();
    }

    public void subscribe(final Request request, final Callback callback) {
        final String channel = request.getChannelId();
        if (TextUtils.isEmpty(channel)) {
            if (callback != null) {
                callback.fail("subscribe no channel");
            }
            return;
        }

        Call call = subscribePool.findCallByChannel(request.getChannelId());

        if (call == null) {
            sendSubscribe(request, callback);
        } else if (callback == null) {
            send(request, null);
        } else if (call.equalsCurrent(request)) {
            call.addCallback(callback);
        } else {
            sendSubscribe(request, callback);
        }

    }

    /**
     * 发送订阅请求
     *
     * @param request
     * @param callback
     */
    private void sendSubscribe(final Request request, final Callback callback) {
        send(request, new Callback() {
            @Override
            public void success(String message) {
                Call call = subscribePool.findCallByChannel(request.getChannelId());
                if (call == null) {
                    call = new Call(request);
                }
                if (callback != null) {
                    call.addCallback(callback);
                }
                subscribePool.put(call);
            }

            @Override
            public void fail(String message) {
                if (callback != null) {
                    callback.fail(message);
                }
            }
        });
    }

    /**
     * 检查连接，如果连接有问题，则尝试恢复连接
     *
     * @return
     */
    public boolean checkConnection() {
        LogUtil.e("checkConnection" + "&isClosed::" + client.isClosed() + "&isOpen()::" + client.isOpen());
        if (client.isClosed()) {
            try {
                LogUtil.e("尝试重新连接");
                client.reconnect();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    LogUtil.e("尝试建立连接");
                    client.connect();
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
            return false;
        } else if (!client.isOpen()) {
            try {
                LogUtil.e("尝试建立连接");
                client.connect();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    LogUtil.e("尝试重新连接");
                    client.reconnect();
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
            return false;
        } else if (client.isClosing()) {
            return false;
        }
        return true;
    }

    public void unsubscribe(final String channel, final Callback callback) {
        if (TextUtils.isEmpty(channel)) {
            if (callback != null) {
                callback.fail("subscribe no channel");
            }
            return;
        }
        Call call = subscribePool.findCallByChannel(channel);
        if (call == null) {
            return;
        }

        call.getCallbacks().remove(callback);

        if (!call.getCallbacks().isEmpty()) {
            return;
        }
        subscribePool.remove(channel);
        send(new Request(WebSocketService.UNSUBSCRIBE, channel), new Callback() {
            @Override
            public void success(String message) {

            }

            @Override
            public void fail(String message) {

            }
        });
    }

    boolean isEmpty(List<Call> calls) {
        for (Call call : calls) {
            if (!call.getCallbacks().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String genCallbackId(String requestId, Callback callback) {
        if (callback == null) {
            return requestId;
        } else {
            return callback.hashCode() + callback.toString() + requestId;
        }

    }


    public DisposableConsole send(final Request request, final Callback callback) {
        Call call = requestPool.findCallByRequestId(Call.genRequestId(request));
        if (call == null) {
            if (checkConnection()) {
                if (request.isNeedLogin() && !isLogin) {
                    postRequest(request, callback);
                } else {
                    call = new Call(request);
                    call.addCallback(callback);
                    try {
                        client.send(call.getRequestString());
                        requestPool.put(call);
                    } catch (Exception e) {
                        LogUtil.e("发送数据出现异常");
                        e.printStackTrace();
                        postRequest(request, callback);
                    }
                }
            } else {
                postRequest(request, callback);
            }
        } else {
            call.addCallback(callback);
            if (callback == null) {
                try {
                    client.send(call.getRequestString());
                } catch (Exception e) {
                    LogUtil.e("发送数据出现异常");
                    e.printStackTrace();
                    postRequest(request, callback);
                }

            }
        }
        return new DisposableConsole(this, genCallbackId(Call.genRequestId(request), callback));
    }

    public abstract void postRequest(Request request, Callback callback);

    public void postOnConnected(Runnable runnable) {
        postOnConnected.add(runnable);
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

    private void restoreRequest() {
        subscribePool.restore(this);
        requestPool.restore(this);
    }

    public void cancel(String id) {
        Iterator<Map.Entry<String, Call>> iteratorMap = subscribePool.getPool().entrySet().iterator();
        while (iteratorMap.hasNext()) {
            Map.Entry<String, Call> entry = iteratorMap.next();
            Iterator<Callback> iterator = entry.getValue().getCallbacks().iterator();
            while (iterator.hasNext()) {
                Callback next = iterator.next();
                if (id.equals(genCallbackId(entry.getValue().getRequestId(), next))) {
                    next.fail("cancel");
                    iterator.remove();
                    if (entry.getValue().getCallbacks().size() == 0) {
                        iteratorMap.remove();
                    }
                    return;
                }
            }
        }
    }

}
