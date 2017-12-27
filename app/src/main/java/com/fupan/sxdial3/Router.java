package com.fupan.sxdial3;


import android.util.Base64;
import android.util.Log;


/**
 * Created by fupan on 2017/5/16.
 */

public class Router {



    public static String Authorization(String router_pwd) {

        String auth = new String(Base64.encode(("admin:"+router_pwd).getBytes(), Base64.DEFAULT));
        //多出一个 0A
        auth=auth.substring(0,auth.length()-1);
       return "Authorization="+escape("Basic "+auth);

    }

    public static String FormatUrl(String host, String addr, String[] property)
    {

        String result="";
        for (String s:property
             ) {
            result+=s;
            result+="&";

        }
        result=result.substring(0,result.length()-1);
        return "http://"+host+addr+"?"+ result;

    }

    public static int Dial(String host, String router_pwd, byte[] acc, String psw)
    {
        String fullAddress="";
        String[] property={
                "wan=0",
                "wantype=2",
                "specialDial=0",
                "acc="+escape(new String(acc)),
                "psw="+psw,
                "confirm="+psw,
                "SecType=0",
                "sta_ip=0.0.0.0",
                "sta_mask=0.0.0.0",
                "linktype=4",
                "Connect=%C1%AC+%BD%D3"


        };
        fullAddress=FormatUrl(host,"/userRpm/PPPoECfgRpm.htm",property);
        String Au=Authorization(router_pwd);

        String[] requestHead={

                "Host:"+host
                ,"Connection:keep-alive"
                ,"Upgrade-Insecure-Requests:1"
                ,"User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.104 Safari/537.36 Core/1.53.2669.400 QQBrowser/9.6.10990.400"
                ,"Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                ,"Referer:"+"http://"+host+"/userRpm/WanCfgRpm.htm"
                ,"Accept-Encoding:gzip, deflate, sdch"
                ,"Accept-Language:zh-CN,zh;"
                ,"Cookie:"+Au


        };



        String result=HttpUtil.httpGet(fullAddress,requestHead);


        Log.v("Dial","run over");
        return 1;
    }



    public static String escape(String src) {
        int i;
        char j;
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length() * 6);
        for (i = 0; i < src.length(); i++) {
            j = src.charAt(i);
            if (Character.isDigit(j) || Character.isLowerCase(j)
                    || Character.isUpperCase(j))
                tmp.append(j);
            else if (j < 256) {
                tmp.append("%");
                if (j < 16)
                    tmp.append("0");
                tmp.append(Integer.toString(j, 16).toUpperCase());
            } else {
                tmp.append("%u");
                tmp.append(Integer.toString(j, 16));
            }
        }
        return tmp.toString();
    }

    public static String unescape(String src) {
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (src.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(src
                            .substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(src
                            .substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    tmp.append(src.substring(lastPos));
                    lastPos = src.length();
                } else {
                    tmp.append(src.substring(lastPos, pos));
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }
}
