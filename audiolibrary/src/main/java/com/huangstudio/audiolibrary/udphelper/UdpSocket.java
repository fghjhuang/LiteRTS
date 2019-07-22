package com.huangstudio.audiolibrary.udphelper;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by David Huang on 2018/8/7.
 */

public class UdpSocket {
    private static final String TAG = "UdpSocket";
    private static UdpSocket instance;//单例
    private DatagramSocket udpsender=null;
    private UdpsocketListener listener=null;
    private boolean listenState=false;//管理接收线程

    /**
     * 设置单例
     * */
    public synchronized static UdpSocket getInstance() {
        if (instance == null) {
            instance = new UdpSocket();
        }
        return instance;
    }
    /**
     * 初始化
     * */
    public void init(){
        try{
            udpsender= new DatagramSocket();
        }catch (SocketException e){
            e.printStackTrace();
            udpsender=null;
            if(listener!=null)listener.onError(e);
        }
    }

    /**
     * 设置监听接收数据接口
     * */
    public void setUdpListener(UdpsocketListener lis){
        this.listener=lis;
    }

    /**
     * 发送udp数据
     * */
    public void sentDataPackage(DatagramPacket packet){
        try{
            udpsender.send(packet);
        }catch (IOException ioe){
            ioe.printStackTrace();
            if(listener!=null)listener.onError(ioe);
        }
    }

    /**
     * 接收udp数据线程
     * */
    public void startUdpListen(final int port){
        listenState=true;//开始接收数据
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                DatagramSocket socket = null;
                Log.i(TAG, "run: 开始接收数据");
                try {
                    socket = new DatagramSocket(port);
                    while (listenState) {
                        byte data[] = new byte[256];
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        socket.receive(packet);//阻塞
                        byte[] buffer = new byte[packet.getLength()];
                        System.arraycopy(data, 0, buffer, 0, buffer.length);
                        if(listener!=null)listener.onDataIn(buffer);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 停止udp接收线程
     * */
    public void stopRevUdp(){
        listenState=false;
    }
}
