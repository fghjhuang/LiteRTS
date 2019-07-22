package com.huangstudio.audiolibrary.audiohelper;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.audiofx.AcousticEchoCanceler;
import android.util.Log;

import com.huangstudio.audiolibrary.OpusHelper;
import com.huangstudio.audiolibrary.udphelper.UdpSocket;
import com.huangstudio.audiolibrary.udphelper.UdpsocketListener;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by davidHuang on 2017/3/19.
 */

public class VoiceRecorder {
    private final static String TAG = "VoiceRecorder";
    private static VoiceRecorder instance;
    private AudioRecord mAudioRecord;
    private AudioTrack audioTrack;
    private OpusHelper opus;                    // Speex音频编解码器
    private AcousticEchoCanceler canceler;//硬件回声消除对象
    private boolean DEBUG=true;
    private int audioShortArrayLength = 320;
    private int recordBufferSize;
    private int playerBufferSize;   // 缓冲区大小
    private short[] decodedShorts = new short[audioShortArrayLength];   // 解码后的音频数据
    private short[] recordData = new short[audioShortArrayLength];          // 读取音频数据存放的数组 320 Short
    private byte[] encodedbytes = new byte[AudioConfig.SPEEX_DATA_SIZE];  // 编码之后的音频数据     40 Byte


    public synchronized static VoiceRecorder getInstance() {
        if (instance == null) {
            instance = new VoiceRecorder();
        }
        return instance;
    }

    /**
     * 开始打电话
     * @param host  目的的ip地址
     * @param sentoutport 发送udp端口
     * @param listenport 监听udp端口
     * */
    public void startCall(String host, int sentoutport, int listenport,Context context) {
        UdpSocket.getInstance().init();//udp对象初始化
        startRecord(host, sentoutport);
        startListen(listenport);
        setAudioMode(context, AudioManager.MODE_NORMAL);
    }

    /**
     * 初始化参数并开始录音
     * */
    public void startRecord(String host, int port) {
        //1.为了方便，这里只录制单声道
        //如果是双声道，得到的数据是一左一右，注意数据的保存和处理
        recordBufferSize = AudioRecord.getMinBufferSize(AudioConfig.sampleRateInHz,
                AudioConfig.channelConfigIn, AudioConfig.audioFormat);
        //2. 创建AudioRecord实例
        mAudioRecord = new AudioRecord(AudioConfig.audioSource, AudioConfig.sampleRateInHz,
                AudioConfig.channelConfigIn, AudioConfig.audioFormat, recordBufferSize );
        //3. 创建Speex编解码实例
        opus = new OpusHelper();
        opus.open(4);
        //4. 如果支持硬件消除回声就打开
        if(AcousticEchoCanceler.isAvailable()){
            initAEC(mAudioRecord.getAudioSessionId());
            setAECEnabled(true);
        }
        //5.开始录音
        mAudioRecord.startRecording();
        //6.创建播放音频实例AudioTrack
        playerBufferSize = AudioTrack.getMinBufferSize(AudioConfig.sampleRateInHz,
                AudioConfig.channelConfigOut, AudioConfig.audioFormat);
        audioTrack = new AudioTrack(AudioConfig.streamType, AudioConfig.sampleRateInHz, AudioConfig.channelConfigOut,
                AudioConfig.audioFormat, playerBufferSize , AudioConfig.mode);

        //7.打开录音线程
        new Thread(new AudioRecordThread(host, port)).start();
    }


    /**
     * 获取录音数据线程
     * */
    private class AudioRecordThread implements Runnable {
        private String host;//对方
        private int port;//对方

        public AudioRecordThread(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            //定义缓冲
            int readSize;
            while (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                readSize = mAudioRecord.read(recordData, 0, recordData.length);
                //去噪音算法
                calc1(recordData,0,recordData.length);
                if(DEBUG){
                    Log.i(TAG, "run: 读取录音数据，recordData size:"+recordData.length+" ,encodebytes size:"+encodedbytes.length+", readsize:"+readSize);
                }
                //压缩
                if(readSize==audioShortArrayLength)
                opus.encode(recordData,0,encodedbytes,readSize);

                if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                    System.out.println(Utilv2.bytesToHexString(encodedbytes));
                    try {
                        InetAddress serverAddress = InetAddress.getByName(host);
                        byte[] data = encodedbytes;
                        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, port);
                        UdpSocket.getInstance().sentDataPackage(packet);
                        //close();
                        //socket.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            release();//在这里release
        }
    }


    /**
     * 接收udp网络数据线程
     * */
    public void startListen(final int port) {
        UdpSocket.getInstance().setUdpListener(new UdpsocketListener() {
            @Override
            public void onDataIn(byte[] data) {
                if(DEBUG) {
                    Log.i(TAG, "onDataIn: 接收到数据了");
                }
                int decode = opus.decode(data, decodedShorts, data.length);
                int write = audioTrack.write(decodedShorts, 0, decode);
                audioTrack.play(); //开始播放
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
        UdpSocket.getInstance().startUdpListen(port);
    }

    /**
     * 关闭打电话
     * */
    public void stopCall(){
        stopRecording();//先关闭录音线程
        UdpSocket.getInstance().stopRevUdp();//再关闭接收线程
    }

    //在录音线程那里结束的时候执行，用来关闭录音和播放的对象
    public void release() {
        if (mAudioRecord != null)
            mAudioRecord.release();
        mAudioRecord = null;
        if (audioTrack != null)
            audioTrack.release();
        audioTrack = null;
    }

    //在这里stop的时候先不要release
    public void stopRecording() {
        if (mAudioRecord != null)
            mAudioRecord.stop();
        if (audioTrack != null)
            audioTrack.stop();

    }

    /**
     * 设置语音播放的模式
     * @param ctx 上下文对象
     * @param mode 默认扬声器播放还是听筒播放
     */
    public void setAudioMode(Context ctx, int mode) {
        if (mode != AudioManager.MODE_NORMAL && mode != AudioManager.MODE_IN_COMMUNICATION) {
            return;
        }
        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (mode == AudioManager.MODE_NORMAL) {
            audioManager.setSpeakerphoneOn(true);//打开扬声器
        } else if (mode == AudioManager.MODE_IN_COMMUNICATION) {
            audioManager.setSpeakerphoneOn(false);//关闭扬声器
        }
        audioManager.setMode(mode);
    }

    //初始化回声消除对象
    public boolean initAEC(int audioSession)
    {
        if (canceler != null)
        {
            return false;
        }
        canceler = AcousticEchoCanceler.create(audioSession);
        canceler.setEnabled(true);
        return canceler.getEnabled();
    }
    //使能回声消除
    public boolean setAECEnabled(boolean enable)
    {
        if (null == canceler)
        {
            return false;
        }
        canceler.setEnabled(enable);
        return canceler.getEnabled();
    }

    //去噪音算法
    private void calc1(short[] lin,int off,int len) {
        int i,j;

        for (i = 0; i < len; i++) {
            j = lin[i+off];
            lin[i+off] = (short)(j>>2);
        }
    }


}
