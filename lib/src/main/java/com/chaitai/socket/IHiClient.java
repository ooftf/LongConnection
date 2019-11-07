package com.chaitai.socket;

import android.os.Handler;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/7
 */
public interface IHiClient {
    void subscribe(BaseRequest key, Callback callback);

    void unSubscribe(BaseRequest key);

    void send(BaseRequest request, Callback callback);

}
