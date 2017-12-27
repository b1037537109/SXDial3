package com.fupan.sxdial3;



import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by fupan on 2016/9/8.
 */
public class HttpUtil {




    public static String httpGet(String url_str, String[] property){
        if(url_str.isEmpty())return null;
        URL url=null;
        HttpURLConnection conn=null;
        String requestHeader=null;
        byte[] requestBody=null;
        String responseHeader=null;
        byte[] responseBody=null;


        try{
            url=new URL(url_str);
            conn=(HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");


            for (int i=0;i<property.length;i++){
               int index=property[i].indexOf(":");
                String a=property[i].substring(0,index);
                String b=property[i].substring(index+1);
                if(a.isEmpty()||b.isEmpty())continue;
                conn.setRequestProperty(a,b);
            }


            conn.setUseCaches(false);
            conn.connect();


            InputStream is=conn.getInputStream();


            responseBody=getBytesByInputStream(is);

            String str=getStringByBytes(responseBody);
            is.close();


            return str;


        }catch (MalformedURLException e) {
            Log.e("myerror",e.getMessage());
        }catch (IOException e){
            Log.e("myerror",e.getMessage());
        }catch (Exception e){
            Log.e("myerror",e.getMessage());
        }

        return null;
    }


    private static byte[] getBytesByInputStream(InputStream is) {
        byte[] bytes = null;
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        byte[] buffer = new byte[1024 * 8];
        int length = 0;
        try {
            while ((length = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, length);
            }
            bos.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bytes;
    }



    //根据字节数组构建UTF-8字符串
    private static String getStringByBytes(byte[] bytes) {
        String str = "";
        try {
            str = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }



    public static JSONObject getJsonObj(String jsonStr){
        try {
            JSONObject jo=new JSONObject(jsonStr);
            return jo;
        } catch (JSONException e) {

            return null;
        }catch (NullPointerException e){
            return null;
        }

    }




    public static String httpPost(String url_str, String[] property, JSONObject data){
        return "";
    }

    public static String httpPost(String url_str, JSONObject data){

        if(url_str.isEmpty())return null;
        URL url=null;
        HttpURLConnection conn=null;
        String requestHeader=null;
        byte[] requestBody=null;
        String responseHeader=null;
        byte[] responseBody=null;
        try{


            url = new URL(url_str);
            conn = (HttpURLConnection) url.openConnection();
            //通过setRequestMethod将conn设置成POST方法
            conn.setRequestMethod("POST");
            //调用conn.setDoOutput()方法以显式开启请求体
            conn.setDoOutput(true);
            //用setRequestProperty方法设置一个自定义的请求头:action，由于后端判断

            conn.setRequestProperty("Content-Type","application/json;charset=UTF-8");
            //获取请求头
            requestHeader = getReqeustHeader(conn);
            //获取conn的输出流
            OutputStream os = conn.getOutputStream();

            //读取assets目录下的person.xml文件，将其字节数组作为请求体
            //requestBody = getBytesFromAssets("person.xml");


            requestBody=data.toString().getBytes("UTF-8");

            os.write(requestBody);
            //记得调用输出流的flush方法
            os.flush();
            //关闭输出流
            os.close();
            //当调用getInputStream方法时才真正将请求体数据上传至服务器
            InputStream is = conn.getInputStream();
            //获得响应体的字节数组
            responseBody = getBytesByInputStream(is);


            //获得响应头
            responseHeader = getResponseHeader(conn);

            return  getStringByBytes(responseBody);

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }


    private static String getReqeustHeader(HttpURLConnection conn) {
        //https://github.com/square/okhttp/blob/master/okhttp-urlconnection/src/main/java/okhttp3/internal/huc/HttpURLConnectionImpl.java#L236
        Map<String, List<String>> requestHeaderMap = conn.getRequestProperties();
        Iterator<String> requestHeaderIterator = requestHeaderMap.keySet().iterator();
        StringBuilder sbRequestHeader = new StringBuilder();
        while (requestHeaderIterator.hasNext()) {
            String requestHeaderKey = requestHeaderIterator.next();
            String requestHeaderValue = conn.getRequestProperty(requestHeaderKey);
            sbRequestHeader.append(requestHeaderKey);
            sbRequestHeader.append(":");
            sbRequestHeader.append(requestHeaderValue);
            sbRequestHeader.append("\n");
        }
        return sbRequestHeader.toString();
    }

    private static String getResponseHeader(HttpURLConnection conn) {
        Map<String, List<String>> responseHeaderMap = conn.getHeaderFields();
        int size = responseHeaderMap.size();
        StringBuilder sbResponseHeader = new StringBuilder();
        for(int i = 0; i < size; i++){
            String responseHeaderKey = conn.getHeaderFieldKey(i);
            String responseHeaderValue = conn.getHeaderField(i);
            sbResponseHeader.append(responseHeaderKey);
            sbResponseHeader.append(":");
            sbResponseHeader.append(responseHeaderValue);
            sbResponseHeader.append("\n");
        }
        return sbResponseHeader.toString();
    }


    public static Boolean netAvailable(Context context){


            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnectedOrConnecting();



    }



}
