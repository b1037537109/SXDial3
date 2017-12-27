package com.fupan.sxdial3;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Created by fupan on 2017/5/16.
 */

public class Account {

    /*
     * in  account 输入闪讯账号
     * return 加密后的账号字节流
     * 由于存在编码的问题所以该处采用字节流
     */
    public  static byte[] getAccount(String account){

        Date curDate=new Date(System.currentTimeMillis());
        long time= curDate.getTime()/1000;
        long timediv5=time/5;

        byte[] timecode=new byte[4];
        for(int i=0;i!=4;++i){
            timecode[i]=(byte)((timediv5>>((3-i)*8))%256);
        }

        String data="";

        data+=account.substring(0,11);
        data+="singlenet01";

        byte[] datacode=data.getBytes();
        byte[] result=new byte[timecode.length+datacode.length];
        System.arraycopy(timecode, 0, result, 0, timecode.length);
        System.arraycopy(datacode, 0, result, timecode.length, datacode.length);



        String aftermd5=md5(result);
        String sig=aftermd5.substring(0,2);

        byte[] temp=new byte[32];
        //  byte[]	timechar=new byte[4];

        for (int i = 0; i < 32; i++) {
            temp[i] = (byte) (timecode[(31 - i) >> 3] & 1);
            timecode[(31 - i) >> 3] = (byte) (timecode[(31 - i) >> 3] >> 1);
        }

        byte[] timeHash = new byte[4];
        for (int i = 0; i < 4; i++) {
            timeHash[i] = (byte) (temp[i] * 128 + temp[4 + i] * 64 + temp[8 + i]
                    * 32 + temp[12 + i] * 16 + temp[16 + i] * 8 +
                    temp[20 + i]
                            * 4 + temp[24 + i] * 2 + temp[28 + i]);
        }

        temp[1] = (byte) ((timeHash[0] & 3) << 4);
        temp[0] = (byte) ((timeHash[0] >> 2) & 0x3F);
        temp[2] = (byte) ((timeHash[1] & 0xF) << 2);
        temp[1] = (byte) ((timeHash[1] >> 4 & 0xF) + temp[1]);
        temp[3] = (byte) (timeHash[2] & 0x3F);
        temp[2] = (byte) (((timeHash[2] >> 6) & 0x3) + temp[2]);
        temp[5] = (byte) ((timeHash[3] & 3) << 4);
        temp[4] = (byte) ((timeHash[3] >> 2) & 0x3F);

        byte[] sig2=new byte[6];

        for (int i = 0; i < 6; i++) {

            byte tp = (byte) (temp[i] + 0x020);
            if (tp >= 0x40) {
                tp++;
            }
            sig2[i]=tp;

        }

        byte[] rr=new byte[10+account.length()];

        rr[0]='\r';
        rr[1]='\n';
        System.arraycopy(sig2, 0, rr, 2, sig2.length);

        String t=sig+account;
        byte[] tb=t.getBytes();

        System.arraycopy(tb, 0, rr,rr.length-tb.length ,tb.length);


        return rr;

    }

    public static String md5(byte[] bb){
        String MD5Str = "";
        if (bb!=null){


            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(bb);
                byte b[] = md.digest();
                int i;

                StringBuilder builder = new StringBuilder(32);
                for (int offset = 0; offset < b.length; offset++) {
                    i = b[offset];
                    if (i < 0)
                        i += 256;
                    if (i < 16)
                        builder.append("0");
                    builder.append(Integer.toHexString(i));
                }
                MD5Str = builder.toString();

            } catch (NoSuchAlgorithmException e) {

                e.printStackTrace();
            }

        }
        return MD5Str;
    }




}
