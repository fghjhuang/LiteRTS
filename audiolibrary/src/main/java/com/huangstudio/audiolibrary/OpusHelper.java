package com.huangstudio.audiolibrary;

/**
 * Created by Administrator on 2018/8/2.
 */

public class OpusHelper {
    static {
        try {
            System.loadLibrary("opussdk");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public OpusHelper() {
    }
    public native String test();
    public native int open(int compression);
    public native int getFrameSize();
    public native int decode(byte encoded[], short lin[], int size);
    public native int encode(short lin[], int offset, byte encoded[], int size);
    public native int close();
}
