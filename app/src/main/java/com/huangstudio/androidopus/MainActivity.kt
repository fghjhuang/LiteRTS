package com.huangstudio.androidopus

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import android.widget.Toast
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.support.v4.media.MediaDescriptionCompatApi21.Builder.setTitle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View.inflate
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import kotlinx.android.synthetic.main.dialog_adddevices.*
import org.json.JSONArray
import org.json.JSONObject
import android.support.v4.widget.PopupWindowCompat.showAsDropDown
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.PopupMenu
import android.widget.PopupWindow
import android.view.View.inflate
import com.huangstudio.androidopus.R.menu.popumenu
import android.view.View.inflate
import android.view.MenuInflater
import com.huangstudio.androidopus.Callview.CallActivity
import com.huangstudio.androidopus.Callview.callprotocol.HttpServer
import com.huangstudio.androidopus.Callview.callprotocol.JavaHttpHelper


/**
 * 目录结构：
 * 1.Callview：打电话和接电话的界面，可以简单看看代码然后画自己的界面
 * 2.Device和Mainactivity，主要是可以添加对方ip和port，使用的是自带json，因为我尽可能不想依赖其他库，不然本sdk不容易看懂
 * 3.audiolibrary，核心库，封装了采集音频，udp发送音频数据，压缩用的opus库，直接在其他项目使用该库就可以了
 *
 * */
class MainActivity : AppCompatActivity() {
    private var TAG:String="MainActivity"
    internal var sentstart = false
    internal var datalist:MutableList<Device> = ArrayList()
    internal var strnamelist:MutableList<String> = ArrayList()
    internal var madapter:ArrayAdapter<String>?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(R.string.app_name)
        setSupportActionBar(maintoolbar)
        tv_ip_info.text="${getIP(this@MainActivity)}"
        initParam()
        initView()
        initEvent()
//        bt_connect.setOnClickListener {
//            if (!sentstart) {
//                sentstart = true
//                bt_connect.text="正在通话中"
//                val remoteHost = et_ip.text.toString()
//                savesetting(remoteHost)
//                VoiceRecorder.getInstance().startCall(remoteHost, Integer.parseInt(sentoutport.text.toString()), Integer.parseInt(revport.text.toString()),this@MainActivity)
//            }else{
//                bt_connect.text="开始连接"
//                sentstart = false
//                VoiceRecorder.getInstance().stopCall()
//            }
//        }
    }

    fun initParam(): Unit {
        val str=readsettring()
        if(str!=""){
            //创建JSONArray把json传入
            val jsonArray= JSONArray(str)

            for(i in 0..(jsonArray.length()-1)){
                val jsonObject=jsonArray.getJSONObject(i)
                val devices=Device(jsonObject.getString("ip"),jsonObject.getInt("port"))
                datalist.add(devices)
                strnamelist.add(jsonObject.getString("ip")+":"+jsonObject.getInt("port"))
            }
        }
        /**
         * 开启httpserver服务，用于接收打过来的电话,默认打开9999端口
         * */
        JavaHttpHelper().startAndroidWebServer(this@MainActivity,9999)
    }

    fun initView(): Unit {
        madapter= ArrayAdapter(this@MainActivity,android.R.layout.simple_list_item_1,strnamelist)
        deviceslist.adapter=madapter
    }

    fun initEvent(){
        //长按弹出菜单选择删除数据还是打电话
        deviceslist.setOnItemLongClickListener { adapterView, view, position, l ->
            //创建弹出式菜单对象（最低版本11）
            val popup = PopupMenu(this, view)//第二个参数是绑定的那个view
            //获取菜单填充器
            val inflater = popup.getMenuInflater()
            //填充菜单
            inflater.inflate(R.menu.popumenu, popup.getMenu())
            //绑定菜单项的点击事件
            popup.setOnMenuItemClickListener { item->
                when(item.itemId){
                    R.id.call->{
                        val intent= Intent(this@MainActivity,CallActivity::class.java)
                        intent.putExtra("type","callout")
                        intent.putExtra("ip",datalist.get(position).ip)
                        intent.putExtra("port",datalist.get(position).port)
                        startActivity(intent)
                    }
                    R.id.delete->{
                        datalist.removeAt(position)
                        strnamelist.removeAt(position)
                        savenewData()
                        madapter?.notifyDataSetChanged()
                    }
                }
                return@setOnMenuItemClickListener true
            }
            //显示(这一行代码不要忘记了)
            popup.show()
            return@setOnItemLongClickListener true
        }
    }

    /**
     * 保存设备数据列表 使用json的格式,本地json库
     * */
    fun savesetting(iplist:String){
        val sharedPreferences = getSharedPreferences("ipconfig", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("ipaddr", iplist)
        editor.commit()
    }

    /**
     * 读取数据列表
     * */
    fun readsettring():String{
        val sharedPreferences = getSharedPreferences("ipconfig", Context.MODE_PRIVATE)
        return sharedPreferences.getString("ipaddr", "")
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.mainmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.addphone ->{
                val builder = AlertDialog.Builder(this)
                val inflater = this.getLayoutInflater()
                val view = inflater.inflate(R.layout.dialog_adddevices, null)
                val inputport:EditText=view.findViewById(R.id.edport) as EditText
                val inputip:EditText=view.findViewById(R.id.edipaddr) as EditText
                builder.setTitle("添加设备")
                        .setView(view)
                        .setPositiveButton("确定", DialogInterface.OnClickListener {
                            dialog, id ->
                            val port:Int=Integer.parseInt("${inputport.text.toString().trim()}")
                            val devices = Device( "${inputip.text.toString().trim()}",port)
                            datalist.add(devices)
                            strnamelist.add("${inputip.text.toString().trim()}"+":"+port)
                            savenewData()
                        })
                        .setNegativeButton("取消", DialogInterface.OnClickListener { dialog, id -> })
                 builder.create().show()
            }
            else -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 保存数据
     * */
    fun savenewData(): Unit {
        val jsonArray=JSONArray()
        for(i in 0..(datalist.size-1)){
            var jo=JSONObject()
            jo.put("ip",datalist.get(i).ip)
            jo.put("port",datalist.get(i).port)
            jsonArray.put(jo)
        }
        savesetting(jsonArray.toString())
        Log.i(TAG, "保存数据: "+jsonArray.toString())
    }
}
