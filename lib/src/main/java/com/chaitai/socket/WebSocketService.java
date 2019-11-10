package com.chaitai.socket;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/8
 */
public class WebSocketService {
    private static WebSocketService instance;

    public static WebSocketService getInstance() {
        if (instance == null) {
            synchronized (WebSocketService.class) {
                if (instance == null) {
                    instance = new WebSocketService();
                }
            }
        }
        return instance;
    }

    WSClient hiClient;

    boolean isLogin = false;
    private static ISocketConfigProvider provider;

    public static void init(ISocketConfigProvider provider) {
        WebSocketService.provider = provider;
    }

    private WebSocketService() {
        hiClient = new WSClient(provider.getUrl());
        hiClient.addOpenListener(new Runnable() {
            @Override
            public void run() {
                isLogin = false;
                loginRequest();
            }
        });
    }

    private void loginRequest() {
        Log.e("WebSocketService", "尝试登录");
        Request loginRequest = new Request("login");
        loginRequest.args.put("token", provider.getToken());

        hiClient.send(loginRequest, new Callback() {
            @Override
            public void success(String message) {
                Log.e("WebSocketService", "登录成功");
                isLogin = true;
                for (Runnable runnable : postOnLogin) {
                    runnable.run();
                }
                postOnLogin.clear();

                for (Map.Entry<String, Call> entry : hiClient.channelObserver.entrySet()) {
                    Call value = entry.getValue();
                    subscribe(value.request, null);
                }
            }

            @SuppressLint("CheckResult")
            @Override
            public void fail(String message) {
                Log.e("WebSocketService", "登录失败::"+message);
                isLogin = false;
                hiClient.postOnLooper(new Runnable() {
                    @Override
                    public void run() {
                        if (hiClient.checkConnection() && !isLogin) {
                            loginRequest();
                        }
                    }
                });
            }
        });
    }

    public void send(final Request request, final Callback callback) {
        if (checkStatus()) {
            hiClient.send(request, callback);
        } else {
            Log.e("WebSocketService", "send-推迟到登录后");
            postOnLogin.add(new Runnable() {
                @Override
                public void run() {
                    send(request, callback);
                }
            });
        }

    }

    boolean checkStatus() {
        if (!hiClient.checkConnection()) {
            return false;
        }
        if (!isLogin) {
            return false;
        }

        return true;
    }

    public void subscribe(final Request request, final Callback callback) {
        request.setOp("subscribe");
        if (checkStatus()) {
            hiClient.subscribe(request, callback);
        } else {
            Log.e("WebSocketService", "subscribe::推迟到登录后");
            postOnLogin.add(new Runnable() {
                @Override
                public void run() {
                    subscribe(request, callback);
                }
            });
        }
    }

    public void unSubscribe(Request request, Callback callback) {
        request.setOp("unsubscribe");
        hiClient.unSubscribe(request, callback);
    }

    ArrayList<Runnable> postOnLogin = new ArrayList<>();
}
