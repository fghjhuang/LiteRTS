package com.huangstudio.audiolibrary.udphelper;

/**
 * Created by Administrator on 2018/8/7.
 */

public interface UdpsocketListener {
    void onDataIn(byte[] data);
    void onError(Exception e);
}
