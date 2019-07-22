package com.huangstudio.androidopus.Callview

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import com.huangstudio.androidopus.Callview.callprotocol.JavaHttpHelper
import com.huangstudio.androidopus.Callview.callprotocol.PostHelper
import com.huangstudio.androidopus.R
import kotlinx.android.synthetic.main.activity_call.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import android.content.BroadcastReceiver
import android.content.IntentFilter
import com.huangstudio.audiolibrary.audiohelper.VoiceRecorder


/**
 * 我们使用nanohttpd作为打电话通信工具，要打电话过去就post一个数据过去就可以了
 * */
class CallActivity : AppCompatActivity() {
    private var TAG:String="CallActivity"
    internal var type="unknown"
    internal var connectip=""
    internal var localip:String ? =null
    internal var port=0
    internal val broadcast=BroadcastCenter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        initParam()
        initview()
        initevent()
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(CallState.DataFromPost)
        intentFilter.addAction(CallState.DataFromServer)
        registerReceiver(broadcast, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcast)
    }
    override fun onDestroy() {
        super.onDestroy()
        CallState.callinstate="unknown"
        CallState.calloutstate="unknown"
        CallState.callstate="unknown"
    }
    fun initParam(){
        type=intent.getStringExtra("type") //根据type分辨通话事件
        connectip=intent.getStringExtra("ip") //传进来打进来的ip或者打出去的ip
        localip=getIP(this@CallActivity)
        port=intent.getIntExtra("port",0)
        Log.i(TAG, "initParam: type is:$type,connect ip is : $connectip, local ip is :$localip, connect port is $port")
        when(type){
            "callin"->{
                callinlayout.visibility= View.VISIBLE
                calloutlayout.visibility=View.GONE
                accaptlayout.visibility=View.GONE
                callinip.text=connectip
            }
            "callout"->{
                CallState.calloutstate="calling"
                calloutlayout.visibility= View.VISIBLE
                callinlayout.visibility=View.GONE
                accaptlayout.visibility=View.GONE
                calloutip.text=connectip
            }
            else->{
                finish()
            }
        }
    }

    fun initview(){
        when(type){
            "callin"->{

            }
            "callout"->{
                PostHelper(this@CallActivity).StartCallPost(connectip,localip,9999,port)
            }

        }
    }

    fun initevent(){
        /**
         * callin 电话进来按钮事件
         * */
        refusecall.setOnClickListener {
            PostHelper(this@CallActivity).RefuseCallPost(connectip,9999)
            CallState.callinstate="unknown"
            finish()
        }

        acceptcall.setOnClickListener {
            PostHelper(this@CallActivity).AcceptCallPost(connectip,9999)
            CallState.callinstate="accept"
        }

        /**
         * callout 电话出去按钮事件
         * */
        cancelcall.setOnClickListener {
            PostHelper(this@CallActivity).CancelCallPost(connectip,9999)
            CallState.calloutstate="unknown"
            finish()
        }
        /**
         * call 通话中按钮事件
         * */
        disconnectcall.setOnClickListener {
            PostHelper(this@CallActivity).DisconnectCallPost(connectip,9999)
            time_counttext.stop()//关闭定时
            CallState.callinstate="unknown"
            CallState.calloutstate="unknown"
            CallState.callstate="unknown"
            VoiceRecorder.getInstance().stopCall()
            launch(CommonPool){
                delay(1000L)
                finish()
            }
        }
    }

    inner class BroadcastCenter : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val result=intent.getStringExtra("data")
            Log.i(TAG, "接收到广播数据:$result ")
            try{
                if (action == CallState.DataFromPost) {
                    processPostResult(result)
                } else if (action == CallState.DataFromServer) {
                    processServerData(result)
                } else {

                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
    /**
     * 请求服务来的数据
     * */
    fun processServerData(s:String){
        when(type){
            "callin"->{
                when(s){
                    "cancel"->{
                        CallState.callinstate="unknown"
                        CallState.calloutstate="unknown"
                        CallState.callstate="unknown"
                        finish()
                    }
                    "disconnect"->{
                        Log.i(TAG, "processServerData:对方断开连接 ")
                        CallState.callinstate="unknown"
                        CallState.calloutstate="unknown"
                        CallState.callstate="unknown"
                        finish()
                    }
                }
            }
            "callout"->{
                when(s){
                    "accept"->{
                        CallState.callstate="start call"
                        calloutlayout.visibility= View.GONE
                        callinlayout.visibility=View.GONE
                        accaptlayout.visibility=View.VISIBLE
                        time_counttext.setBase(SystemClock.elapsedRealtime())
                        time_counttext.start()//打开定时
                        VoiceRecorder.getInstance().startCall(connectip, port, port,this@CallActivity)
                    }
                    "refuse"->{
                        Log.i(TAG, "processServerData:对方拒绝接听 ")
                        CallState.callinstate="unknown"
                        CallState.calloutstate="unknown"
                        CallState.callstate="unknown"
                        finish()
                    }
                    "disconnect"->{
                        Log.i(TAG, "processServerData:对方断开连接 ")
                        CallState.callinstate="unknown"
                        CallState.calloutstate="unknown"
                        CallState.callstate="unknown"
                        VoiceRecorder.getInstance().stopCall()
                        launch(CommonPool){
                            delay(1000L)
                            finish()
                        }
                    }
                }
            }
        }

    }
    /**
     * 处理主动post结果
     * */
    fun processPostResult(str:String){
        Log.i(TAG, "processPostResult: $str")
        val json=JSONObject(str)
        val status=json.get("status")as String
        when(type){
            "callin"->{
                when(status){
                    "accepted"->{//对方打过来，本身点击接听按钮
                        Log.i(TAG, "processPostResult:本身接听电话 ")
                        calloutlayout.visibility= View.GONE
                        callinlayout.visibility=View.GONE
                        accaptlayout.visibility=View.VISIBLE
                        time_counttext.setBase(SystemClock.elapsedRealtime())
                        time_counttext.start()//打开定时
                        VoiceRecorder.getInstance().startCall(connectip, port, port,this@CallActivity)
                    }
                    "refused"->{
                        Log.i(TAG, "processPostResult: 本身拒绝接听")
                    }
                }
            }
            "callout"->{
                when(status){
                    "connected"->{
                        CallState.calloutstate="connected"
                        calloutstate.text="电话已接通，等待对方接听..."
                        Log.i(TAG, "initParam:电话已接通，等待对方接听 ")
                    }
                    "canceled"->{
                        Log.i(TAG, "processPostResult: 点击取消打电话返回数据")
                    }
                    "connect_fail"->{
                        Log.i(TAG, "processPostResult:对方正在通话中 ")
                        calloutstate.text="抱歉,对方正在通话中..."
                        launch(CommonPool){
                            delay(1000L)
                            finish()
                        }
                    }
                }
            }
        }
    }
    /**
     * 读取本地ip地址
     * */
    fun getIP(context: Context): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.getInetAddresses()
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress() && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress().toString()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }

        return null
    }
}
