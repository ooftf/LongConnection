package com.chaitai.socket;

/**
 * @author ooftf
 * @email 994749769@qq.com
 * @date 2019/11/7
 */
public interface Callback {
    void success(String message);

    void fail(String message);
}
