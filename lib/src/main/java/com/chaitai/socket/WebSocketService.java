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
    public static final String OP_LOGIN = "login";
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
        };
        hiClient.addOpenListener(new Runnable() {
            @Override
            public void run() {
                isLogin = false;
                loginRequest();
            }
        });
        hiClient.addCloseListener(new Runnable() {
            @Override
            public void run() {
                isLogin = false;
            }
        });
     /*   hiClient.addErrorListener(new Runnable() {
            @Override
            public void run() {
                isLogin = false;
            }
        });*/
    }

    private void loginRequest() {
        LogUtil.e("WebSocketService", "尝试登录");
        Request loginRequest = new Request("login");
        loginRequest.args.put("token", provider.getToken());

        hiClient.send(loginRequest, new Callback() {
            @Override
            public void success(String message) {
                LogUtil.e("WebSocketService", "登录成功");
                isLogin = true;
                for (Map.Entry<String, Call> entry : hiClient.channelObserver.entrySet()) {
                    Call value = entry.getValue();
                    Log.e("WebSocketService", "登录后恢复subscribe::" + value.request.getId());
                    subscribe(value.request, null);
                }

                for (Map.Entry<String, Call> entry : hiClient.callbackMap.entrySet()) {
                    Call value = entry.getValue();
                    if (OP_LOGIN.equals(value.request.getOp())) {
                        continue;
                    }
                    LogUtil.e("WebSocketService", "登录后恢复send::" + value.request.getId());
                    send(value.request, null);
                }

                for (Runnable runnable : postOnLogin) {
                    runnable.run();
                }
                postOnLogin.clear();
            }

            @SuppressLint("CheckResult")
            @Override
            public void fail(String message) {
                Log.e("WebSocketService", "登录失败::" + message);
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
            try {
                hiClient.send(request, callback);
            } catch (Exception e) {
                postOnLogin.add(new Runnable() {
                    @Override
                    public void run() {
                        send(request, callback);
                    }
                });
            }
        } else {
            LogUtil.e("WebSocketService", "send-推迟到登录后");
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
            LogUtil.e("WebSocketService", "subscribe::" + request.getId() + "::推迟到登录后");
            postOnLogin.add(new Runnable() {
                @Override
                public void run() {
                    Log.e("WebSocketService", "subscribe::已登录执行::" + request.getId());
                    subscribe(request, callback);
                }
            });
        }
    }

    public void unsubscribe(Request request, Callback callback) {
        request.setOp("unsubscribe");
        hiClient.unsubscribe(request, callback);
    }

    ArrayList<Runnable> postOnLogin = new ArrayList<>();
}
