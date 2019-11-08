package com.chaitai.longconnection;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/8
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ARouter.init(this);
    }
}
