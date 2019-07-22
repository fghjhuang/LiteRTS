package com.huangstudio.audiolibrary.audiohelper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.huangstudio.audiolibrary.R;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilv2 {

    /**
     * Toast string
     * */
    public static void ToastString(Context context, String str){
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    /**
     * byte转string ascii编码
     */
    public static String ByteToStringInUnicode(byte[] name){
        String s="";
        try {
            s= new String(name,"Unicode");// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    /**
     * byte转string ascii编码
     */
    public static String ByteToStringInAscii(byte[] name){
        String s="";
        try {
            s= new String(name,"ascii");// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }
    /**
     * string 转byte数组 ascii解码
     */
    public static byte[] StringToByteInAscii(String str){
        byte[] result=null;
        try{
            result=str.getBytes("ascii");
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    /**
     * string 转byte数组 unicode解码
     */
    public static byte[] StringToByteInUnicode(String str){
        byte[] result=null;
        try{
            result=str.getBytes("UTF-16BE");
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    /**
     * @param num 2个字节的byte
     */
    public static int TwoByteToOneInteger(byte[] num){
        int result=(num[0]<<8)+(num[1]&0xff);
        return result;
    }
    /**
    * 1个int数据转成2个byte
    * */
    public static byte[] OneIntegerToTwoByte(int num){
        byte[] result=new byte[2];
        result[0]=(byte) (((num) &0xff00)>>8);
        result[1]=(byte) (((num) ) - ((num)&0xff00));
        return result;
    }

    /**
     *把byte数组转16进制的字符串
     */
    public static String byte2hex(byte [] buffer){
        String h = "";

        for(int i = 0; i < buffer.length; i++){
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if(temp.length() == 1){
                temp = "0" + temp;
            }
            h = h + " "+ temp;
        }
        return h;
    }

    /**
     * @功能 短整型与字节的转换
     * @param number 短整型
     * @return 两位的字节数组
     */
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    /**
     * @功能 字节的转换与短整型
     * @param b 两位的字节数组
     * @return 短整型
     */
    public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    /**
     * @说明 主要是为解析静态数据包，将一个字节数组转换为short数组
     * @param b
     */
    public static short[] byteArray2ShortArray(byte[] b) {
        int len = b.length / 2;
        int index = 0;
        short[] re = new short[len];
        byte[] buf = new byte[2];
        for (int i = 0; i < b.length;) {
            buf[0] = b[i];
            buf[1] = b[i + 1];
            short st = byteToShort(buf);
            re[index] = st;
            index++;
            i += 2;
        }
        return re;
    }

    /**
     * @说明 主要是为解析静态数据包，将一个short数组反转为字节数组
     * @param b
     */
    public static byte[] shortArray2ByteArray(short[] b) {
        byte[] rebt = new byte[b.length * 2];
        int index = 0;
        for (int i = 0; i < b.length; i++) {
            short st = b[i];
            byte[] bt = shortToByte(st);
            rebt[index] = bt[0];
            rebt[index + 1] = bt[1];
            index += 2;
        }
        return rebt;
    }

//    /**
//    * 通过字符串获取图像资源的id，用于设置imageview的图像
//    * @param ResName 资源名称
//    *@param type 1代表mimap资源，2代表drawable资源
//    * */
//    public static int getResourdIdByResourdName(String ResName, int type){
//        int resourceId = 0;
//        try {
//            Field field=null;
//            switch (type){
//                case 1:
//                    field = R.mipmap.class.getField(ResName);//can change to R.drawable.class
//                    break;
//                case 2:
//                    field = R.drawable.class.getField(ResName);//can change to R.drawable.class
//                    break;
//            }
//            field.setAccessible(true);
//            try {
//                resourceId = field.getInt(null);
//            } catch (IllegalArgumentException e) {
//                // log.showLogDebug("IllegalArgumentException:" + e.toString());
//            } catch (IllegalAccessException e) {
//                // log.showLogDebug("IllegalAccessException:" + e.toString());
//            }
//        } catch (NoSuchFieldException e) {
//            //log.showLogDebug("NoSuchFieldException:" + e.toString());
//        }
//        return resourceId;
//    }

    /**
     * 动态修改drawable
     * @param v 要修改drawable的view
     * @param drawable 要替换的drawable
    * */
    public static void setdrawable(View v, Drawable drawable, String presscolor, String releaseColor){
        Drawable backgroundDrawable = drawable;
        StateListDrawable sld = (StateListDrawable) backgroundDrawable;// 通过向下转型，转回原型，selector对应的Java类为：StateListDrawable

        Drawable.ConstantState cs = sld.getConstantState();

        try {
            Method method = cs.getClass().getMethod("getChildren", new Class[ 0 ]);// 通过反射调用getChildren方法获取xml文件中写的drawable数组
            method.setAccessible(true);
            Object obj = method.invoke(cs, new Object[ 0 ]);
            Drawable[] drawables = (Drawable[]) obj;

            for (int i = 0; i < drawables.length; i++) {
                // 接下来我们要通过遍历的方式对每个drawable对象进行修改颜色值
                GradientDrawable gd = (GradientDrawable) drawables[i];
                if (gd == null) {
                    break;
                }
                if (i == 0) {
                    // 我们对按下的状态做浅色处理
                    gd.setColor(ToColor(presscolor));
                } else {
                    // 对默认状态做深色处理
                    gd.setColor(ToColor(releaseColor));
                }
            }
            // 最后总结一下，为了实现这个效果，刚开始并没有看到setColor的方法，而是通过反射获取GradientDrawable对象的属性GradientState，
            // 再通过反射调用GradientState对象的setSolidColor方法去实现，效果不太理想。
            // 最后在仔仔细细一一看GradientDrawable对象的属性，发现属性Paint
            // mFillPaint，从名字就可以看出这个对象是用来绘制drawable的背景的，
            // 于是顺着往下找，发现setColor方法，于是bingo，这个过程也是挺曲折的。
            v.setBackground(backgroundDrawable);
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
    * 16进制字符串转int类型的颜色数据，例如FF000000表示白色
    * */
    public static int ToColor(String data){
        int color=0;
        int rin,gin,bin,ain;
        ain= Integer.parseInt(data.substring(0,2),16);
        rin= Integer.parseInt(data.substring(2,4),16);
        gin= Integer.parseInt(data.substring(4,6),16);
        bin= Integer.parseInt(data.substring(6,8),16);
        color= Color.argb(ain, rin, gin, bin);
        return color;
    }
    /**
     * 16进制byte[]转int类型的颜色数据，例如FF000000表示白色
     * */
    public static int ToColor(byte[] data){
        int color=0;
        int rin,gin,bin;
        rin=(255*(data[0]&0xff))/100;
        gin=(255*(data[1]&0xff))/100;
        bin=(255*(data[2]&0xff))/100;
        String r= Integer.toHexString(rin);
        switch (r.length()){
            case 0:r="00";break;
            case 1:r="0"+r;break;
        }
        String g= Integer.toHexString(gin);
        switch (g.length()){
            case 0:g="00";break;
            case 1:g="0"+g;break;
        }
        String b= Integer.toHexString(bin);
        switch (b.length()){
            case 0:b="00";break;
            case 1:b="0"+b;break;
        }
        color= Color.argb(255, Integer.parseInt(r,16), Integer.parseInt(g,16), Integer.parseInt(b,16));
        return color;
    }
    /**
     *@param beaconbyte 16字节的ibeacon的uuid
     *
     * */
    public static String ByteToBeaconString(byte[] beaconbyte){
        //4-2-2-2-6
        String result="";
        byte[] s1={beaconbyte[0],beaconbyte[1],beaconbyte[2],beaconbyte[3]};
        byte[] s2={beaconbyte[4],beaconbyte[5]};
        byte[] s3={beaconbyte[6],beaconbyte[7]};
        byte[] s4={beaconbyte[8],beaconbyte[9]};
        byte[] s5={beaconbyte[10],beaconbyte[11],beaconbyte[12],beaconbyte[13],beaconbyte[14],beaconbyte[15]};
        String str1="",str2="",str3="",str4="",str5="";
        str1=bytesToHexString(s1);
        str2=bytesToHexString(s2);
        str3=bytesToHexString(s3);
        str4=bytesToHexString(s4);
        str5=bytesToHexString(s5);
        result=str1+"-"+str2+"-"+str3+"-"+str4+"-"+str5;
        return  result;
    }
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * @param beaconstr ibeacon的字符串转byte[] 数组
     *
     * */
    public static byte[] StringToBeaconByte(String beaconstr){
        //4-2-2-2-6
        byte[] result=new byte[16];
        String[] str=beaconstr.split("-");
        result[0]=(byte) Integer.parseInt(str[0].substring(0,2),16);
        result[1]=(byte) Integer.parseInt(str[0].substring(2,4),16);
        result[2]=(byte) Integer.parseInt(str[0].substring(4,6),16);
        result[3]=(byte) Integer.parseInt(str[0].substring(6,8),16);
        result[4]=(byte) Integer.parseInt(str[1].substring(0,2),16);
        result[5]=(byte) Integer.parseInt(str[1].substring(2,4),16);
        result[6]=(byte) Integer.parseInt(str[2].substring(0,2),16);
        result[7]=(byte) Integer.parseInt(str[2].substring(2,4),16);
        result[8]=(byte) Integer.parseInt(str[3].substring(0,2),16);
        result[9]=(byte) Integer.parseInt(str[3].substring(2,4),16);
        result[10]=(byte) Integer.parseInt(str[4].substring(0,2),16);
        result[11]=(byte) Integer.parseInt(str[4].substring(2,4),16);
        result[12]=(byte) Integer.parseInt(str[4].substring(4,6),16);
        result[13]=(byte) Integer.parseInt(str[4].substring(6,8),16);
        result[14]=(byte) Integer.parseInt(str[4].substring(8,10),16);
        result[15]=(byte) Integer.parseInt(str[4].substring(10,12),16);
        return result;
    }

    /*
    * EditText 添加该listener的时候，只要点击输入就会把hint隐藏掉
    * */
    public static View.OnFocusChangeListener onFocusAutoClearHintListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            EditText textView = (EditText) v;
            String hint;
            if (hasFocus) {
                hint = textView.getHint().toString();
                textView.setTag(hint);
                textView.setHint("");
            } else {
                hint = textView.getTag().toString();
                textView.setHint(hint);
            }
        }
    };

    /*
    * 切换软键盘
    * */
    public static void hideKeyBoard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        // 得到InputMethodManager的实例
        if (imm.isActive()) {
            // 如果开启
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                    InputMethodManager.HIDE_NOT_ALWAYS);
            // 关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的
        }
    }

//    /*
//    * 使用反射设置edittext光标颜色
//    * */
//    public static void setCursorColor(EditText ed, String drawable_name){
//        try {//修改光标的颜色（反射）
//            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
//            f.setAccessible(true);
//            f.set(ed, getResourdIdByResourdName(drawable_name,2));
//        } catch (Exception ignored) {
//            // TODO: handle exception
//        }
//    }
    /*
    * 修改EditText的hint字体变小，输入字体不变
    * */
    public static void setHintSmall(EditText text, String hint){
        SpannableString ss = new SpannableString(hint);//定义hint的值
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(12,true);//设置字体大小 true表示单位是sp
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setHint(new SpannedString(ss));
    }
    /*
    * 验证数字范围
    * */
    public static boolean isinRange(int start,int end,int num){
        if(num>=start&&num<=end){
            return true;
        }else{
            return false;
        }
    }
    /*
    * 检查字符串是否为IP地址
    * */
    public static boolean isIP(String str) {
        String num = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
        String regex = "^" + num + "\\." + num + "\\." + num + "\\." + num + "$";
        return match(regex, str);
    }
    /**
     * 验证网址Url http://xxxxxxxx
     *
     * @param str 待验证的字符串
     * @return 如果是符合格式的字符串,返回 <b>true </b>,否则为 <b>false </b>
     */
    public static boolean IsUrl(String str) {
        String regex = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";
        return match(regex, str);
    }

    /**
     * @param regex
     * 正则表达式字符串
     * @param str
     * 要匹配的字符串
     * @return 如果str 符合 regex的正则表达式格式,返回true, 否则返回 false;
     */
    private static boolean match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /*
    * xml字符串转list map对象，只适用于3层结构，即根节点下面的节点还有节点,依赖dom4j库
    * */
//    public static List<HashMap<String,String>> readStringXmlOut(String xml) {
//        List<HashMap<String,String>> result=new ArrayList<>();
//        Document doc = null;
//        try {
//            doc = DocumentHelper.parseText(xml); // 将字符串转为XML
//            Element rootElt = doc.getRootElement(); // 获取根节点
//            List<Element> list = rootElt.elements();//获取根节点下所有节点
//            for (Element element : list) {  //遍历节点
//                HashMap<String,String> map = new HashMap<String,String>();
//                List<Element> img=element.elements();
//                for (Element elementx : img) {
//                    map.put(elementx.getName(), elementx.getStringValue()); //节点的name为map的key，text为map的value
//                }
//                result.add(map);
//            }
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }

    /*
    * xml字符串转list map对象，只适用于2层结构，即根节点下面的节点,依赖dom4j库
    * */
//    public static List<HashMap<String,String>> readStringXmlOut2layer(String xml) {
//        List<HashMap<String,String>> result=new ArrayList<>();
//        Document doc = null;
//        try {
//            doc = DocumentHelper.parseText(xml); // 将字符串转为XML
//            Element rootElt = doc.getRootElement(); // 获取根节点
//            List<Element> list = rootElt.elements();//获取根节点下所有节点
//            for (Element element : list) {  //遍历节点
//                HashMap<String,String> map = new HashMap<String,String>();
//                map.put(element.getName(), element.getStringValue()); //节点的name为map的key，text为map的value
//                result.add(map);
//            }
//        } catch (DocumentException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
//
//    /*
//    * json string to Map，依赖gson库
//    * */
//    public static Map<String,Object> JsonStringToMap(String s){
//        return new Gson().fromJson(s, new TypeToken<HashMap<String,Object>>(){}.getType());
//    }

}

