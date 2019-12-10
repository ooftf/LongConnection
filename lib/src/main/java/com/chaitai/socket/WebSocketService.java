package com.chaitai.socket;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.ArrayList;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/8
 */
public class WebSocketService {
    public static final String OP_LOGIN = "login";
    public static final String UNSUBSCRIBE = "unsubscribe";
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
    private static ISocketConfigProvider provider;

    public static void init(ISocketConfigProvider provider, boolean debug) {
        WebSocketService.provider = provider;
        LogUtil.isDebug = debug;
    }

    private WebSocketService() {
        hiClient = new WSClient(provider.getUrl()) {
            @Override
            protected boolean isNeedConnection() {
                return super.isNeedConnection() || postOnLogin.size() > 0;
            }

            @Override
            public void postRequest(final Request request, final Callback callback) {
                if (request.isNeedLogin()) {
                    postOnLogin(new Runnable() {
                        @Override
                        public void run() {
                            send(request, callback);
                        }
                    });
                } else {
                    hiClient.postOnConnected(new Runnable() {
                        @Override
                        public void run() {
                            send(request, callback);
                        }
                    });
                }
            }


        };
        hiClient.addOpenListener(new Runnable() {
            @Override
            public void run() {
                loginRequest();
            }
        });
    }

    private void loginRequest() {
        if (hiClient.isLogin()) {
            return;
        }
        LogUtil.e("WebSocketService", "尝试登录");
        Request loginRequest = new Request("login");
        loginRequest.setNeedLogin(false);
        loginRequest.getArgs().put("token", provider.getToken());

        hiClient.send(loginRequest, new Callback() {
            @Override
            public void success(String message) {
                LogUtil.e("WebSocketService", "登录成功");
                hiClient.setLogin(true);
                for (Runnable runnable : postOnLogin) {
                    runnable.run();
                }
                postOnLogin.clear();
            }

            @SuppressLint("CheckResult")
            @Override
            public void fail(String message) {
                Log.e("WebSocketService", "登录失败::" + message);
                hiClient.setLogin(false);
                hiClient.postOnLooper(new Runnable() {
                    @Override
                    public void run() {
                        loginRequest();
                    }
                });
            }
        });
    }

    public void send(final Request request, final Callback callback) {
        hiClient.send(request, callback);
    }

    void postOnLogin(Runnable runnable) {
        postOnLogin.add(runnable);
    }

    public void subscribe(final Request request, final Callback callback) {
        request.setOp("subscribe");
        hiClient.subscribe(request, callback);
    }

    public void unsubscribe(String channel, Callback callback) {
        hiClient.unsubscribe(channel, callback);
    }

    ArrayList<Runnable> postOnLogin = new ArrayList<>();
}
