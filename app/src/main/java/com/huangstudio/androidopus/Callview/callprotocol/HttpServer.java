package com.huangstudio.androidopus.Callview.callprotocol;

import android.content.Context;
import android.content.Intent;

import com.huangstudio.androidopus.Callview.CallActivity;
import com.huangstudio.androidopus.Callview.CallState;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by Administrator on 2018/8/8.
 */

public class HttpServer extends NanoHTTPD {
    String unknown="unknown";
    Context rootcontext;


    public HttpServer(Context context, int port) {
        super(port);
        rootcontext=context;
    }

    public HttpServer(String hostname, int port) {
        super(hostname, port);
    }
    @Override
    public Response serve(IHTTPSession session) {
        String msg = "";
        Map<String, String> parms = session.getParms();
        if (parms.get("callinip") == null) {
            if (parms.get("callcmd") != null){
                switch (parms.get("callcmd")){
                    case "cancel":
                        msg += "{\"status\":\"canceled\"}";
                        break;
                    case "refuse":
                        msg += "{\"status\":\"refused\"}";
                        break;
                    case "accept":
                        msg += "{\"status\":\"accepted\"}";
                        break;
                    case "disconnect":
                        msg += "{\"status\":\"disconnected\"}";
                        break;
                }
                sendBroadcast(parms.get("callcmd"));
            }else{
                msg += "unknown";
            }
        } else {
            if((!CallState.INSTANCE.getCallinstate().equals(unknown))||
                    (!CallState.INSTANCE.getCalloutstate().equals(unknown))||
                    (!CallState.INSTANCE.getCallstate().equals(unknown))){
                msg += "{\"status\":\"connect_fail\"}";
            }else{
                msg += "{\"status\":\"connected\"}";
                Intent intent=new Intent(rootcontext,CallActivity.class);
                intent.putExtra("type","callin");
                intent.putExtra("ip",parms.get("callinip"));
                intent.putExtra("port",Integer.parseInt(parms.get("port")));
                rootcontext.startActivity(intent);
            }
        }
        return newFixedLengthResponse( msg  );
    }
    private void sendBroadcast(String str){
        Intent intent =new Intent(CallState.INSTANCE.getDataFromServer());
        intent.putExtra("data",str);
        rootcontext.sendBroadcast(intent);
    }
}
