package com.chaitai.socket;

import android.util.Log;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/26
 */
class LogUtil {
    protected static boolean isDebug;

    public static void e(String message) {
        e("Socket", message);
    }

    public static void e(String tag, String message) {
        if(isDebug){
            Log.e(tag, message);
        }

    }
}
