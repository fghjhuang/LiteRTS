package com.huangstudio.androidopus.Callview.callprotocol;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.huangstudio.androidopus.Callview.CallState;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 请求post到局域网内对方的手机，请求实时语音通信
 */

public class PostHelper {
    Context rootcontext;
    public PostHelper(Context context){
        rootcontext=context;
    }
    /**
     * 开始post
     * */
    public void StartCallPost(String ip,String localip,int port,int connect_port){
        String url="http://"+ip+":"+port+"/?callinip="+localip+"&port="+connect_port;
        try{
            new PostThread(url).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 取消打电话
     * */
    public void CancelCallPost(String ip,int port){
        String url="http://"+ip+":"+port+"/?callcmd=cancel";
        try{
            new PostThread(url).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 拒绝电话
     * */
    public void RefuseCallPost(String ip,int port){
        String url="http://"+ip+":"+port+"/?callcmd=refuse";
        try{
            new PostThread(url).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 接听电话
     * */
    public void AcceptCallPost(String ip,int port){
        String url="http://"+ip+":"+port+"/?callcmd=accept";
        try{
            new PostThread(url).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 主动断开电话
     * */
    public void DisconnectCallPost(String ip,int port){
        String url="http://"+ip+":"+port+"/?callcmd=disconnect";
        try{
            new PostThread(url).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //子线程：使用POST方法向服务器发送用户名、密码等数据
    class PostThread extends Thread {
        String url;
        public PostThread(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            HttpClient httpClient = new DefaultHttpClient();
            //第二步：生成使用POST方法的请求对象
            HttpPost httpPost = new HttpPost(url);
            //将准备好的键值对对象放置在一个List当中
            ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
            try {
                //创建代表请求体的对象（注意，是请求体）
                HttpEntity requestEntity = new UrlEncodedFormEntity(pairs);
                //将请求体放置在请求对象当中
                httpPost.setEntity(requestEntity);
                //执行请求对象
                try {
                    //第三步：执行请求对象，获取服务器发还的相应对象
                    HttpResponse response = httpClient.execute(httpPost);
                    //第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
                    if (response.getStatusLine().getStatusCode() == 200) {
                        //第五步：从相应对象当中取出数据，放到entity当中
                        HttpEntity entity = response.getEntity();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(entity.getContent()));
                        String result = reader.readLine();
                        sendBroadcast(result);
                        Log.d("HTTP", "POST:" + result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendBroadcast(String str){
        Intent intent =new Intent(CallState.INSTANCE.getDataFromPost());
        intent.putExtra("data",str);
        rootcontext.sendBroadcast(intent);
    }
}
