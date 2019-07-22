package com.huangstudio.androidopus.Callview.callprotocol;

import android.content.Context;
import android.util.Log;

/**
 * 用来打开httpserver服务，具体提供功能的是HttpServer类，
 */

public class JavaHttpHelper {
    private static final String TAG = "JavaHttpHelper";
    private boolean isStarted = false;
    private HttpServer androidWebServer;

    /***
     * 打开http服务
     * @Param port 端口号
     * */
    public boolean startAndroidWebServer(Context context, int port) {
        if (!isStarted) {
            isStarted=true;
            try {
                if (port == 0) {
                    throw new Exception();
                }
                androidWebServer = new HttpServer(context,port);
                androidWebServer.start();
                Log.i(TAG, "startAndroidWebServer: http服务开启");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "startAndroidWebServer: "+"The PORT " + port + " doesn't work, please change it between 1000 and 9999.");
            }
        }
        return false;
    }

    /**
     * 获取服务是否开启
     * */
    public boolean isServerOpen(){
        return isStarted;
    }
    /**
     * 关闭http服务
     * */
    public boolean stopAndroidWebServer() {
        if (isStarted && androidWebServer != null) {
            androidWebServer.stop();
            isStarted=false;
            return true;
        }
        return false;
    }
}
