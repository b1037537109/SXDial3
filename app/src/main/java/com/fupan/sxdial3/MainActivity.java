package com.fupan.sxdial3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//2017年12月25日 闪讯路由拨号器
//由于版本2源码丢失 现重写该项目

public class MainActivity extends AppCompatActivity {


    final String ConfigFile="info.cfg";
    EditText edit_account;
    EditText edit_pwd;
    Button btn_dial;

    EditText edit_host;
    EditText edit_routerpsw;
    Button btn_save;


    Thread dialThread,loadConfigThread;
    String acc="";
    String pwd="";
    String Host="";
    String router_psw="";

    private  boolean ExitingAnimation=false;
    LinearLayout l2;

    private ViewPager viewPager;

    private View aboutView, mainView, settingView;
    private List<View> views;// Tab页面列表

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(MainActivity.this,msg.what,Toast.LENGTH_SHORT).show();
        }
    };

    //ViewPager Transform 动画
    public class ZoomOutPageTransformer implements ViewPager.PageTransformer
    {
        private static final float MIN_SCALE = 0.75f;
        private static final float MIN_ALPHA = 0.5f;


        public void transformPage(View view, float position)
        {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();



            if (position < -1)
            { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) //a页滑动至b页 ； a页从 0.0 -1 ；b页从1 ~ 0.0
            { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0)
                {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else
                {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)
                        / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else
            { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }




    public class MyViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;
        private View mCurView;
        public MyViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)   {
            container.removeView(mListViews.get(position));
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position), 0);
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return  mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0==arg1;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mCurView=(View)object;
        }

        public View getPrimaryItem(){
            return mCurView;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //初始化viewpager
        viewPager=(ViewPager) findViewById(R.id.vPager);
        views=new ArrayList<View>();
        LayoutInflater inflater=getLayoutInflater();
        aboutView =inflater.inflate(R.layout.about_view, null);
        mainView =inflater.inflate(R.layout.main_view, null);
        settingView =inflater.inflate(R.layout.setting_view, null);

        //拨号窗口

         l2=mainView.findViewById(R.id.main_linearlayout);


        views.add(aboutView);
        views.add(mainView);
        views.add(settingView);

        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(1);

        viewPager.setPageTransformer(true,new ZoomOutPageTransformer());

        //快速退出操作
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){

                    //以拨号窗口大小为基准
                    if(motionEvent.getY()<l2.getTop()||motionEvent.getY()>l2.getBottom()){
                        //开始动画并退出
                        if(ExitingAnimation==false){
                            ObjectAnimator animator=ObjectAnimator.ofFloat(viewPager,"scaleY",1,0);
                            animator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    finish();
                                }
                            });
                            animator.setDuration(200);
                            animator.start();
                            ExitingAnimation=true;
                        }



                        Log.v("mylog",""+motionEvent.getY());
                     //   finish();
                    }
                    else{
                        //关闭输入法
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        if(imm.isActive()){
                            imm.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                        }
                    }
                }
                return false;
            }
        });






        //子控件绑定
        edit_account=(EditText) mainView.findViewById(R.id.account);
        edit_pwd=(EditText)mainView.findViewById(R.id.pwd);
        btn_dial=(Button)mainView.findViewById(R.id.dial);

        edit_host=(EditText)settingView.findViewById(R.id.host);
        edit_routerpsw=(EditText)settingView.findViewById(R.id.router_psw);
        btn_save=(Button)settingView.findViewById(R.id.save);

        //子线程加载配置
        loadConfigThread=new Thread(new Runnable() {
            @Override
            public void run() {

                SharedPreferences sharedPreferences=getSharedPreferences(ConfigFile,MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();

                Host=sharedPreferences.getString("Host","192.168.1.1");
                router_psw=sharedPreferences.getString("RouterPsw","");
                acc=sharedPreferences.getString("Acc","");
                pwd=sharedPreferences.getString("Pwd","");

                edit_account.setText(acc);
                edit_pwd.setText(pwd);
                edit_host.setText(Host);
                edit_routerpsw.setText(router_psw);

            }
        });

        loadConfigThread.start();

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edit_host.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this,"目标host为空！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(edit_routerpsw.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this,"路由器管理密码为空！",Toast.LENGTH_SHORT).show();
                    return;
                }
                Host=edit_host.getText().toString();
                router_psw=edit_routerpsw.getText().toString();
                SaveConfig();
                Toast.makeText(MainActivity.this,"修改已保存！",Toast.LENGTH_SHORT).show();
            }
        });

        btn_dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                acc=edit_account.getText().toString();
                pwd=edit_pwd.getText().toString();
                // Toast.makeText(MainActivity.this,acc+"  "+pwd,Toast.LENGTH_SHORT).show();
                if(acc.isEmpty()||pwd.isEmpty())
                {
                    Toast.makeText(MainActivity.this,"输入为空！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Host.isEmpty()||router_psw.isEmpty())
                {
                    Toast.makeText(MainActivity.this,"配置信息不完整！",Toast.LENGTH_SHORT).show();
                    return;
                }
                //关闭输入法
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm.isActive()){
                    imm.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }

                //检测网络状态

                ConnectivityManager connManager=(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo nInfo=connManager.getActiveNetworkInfo();
                if(nInfo!=null) {
                    if(nInfo.getType()!=connManager.TYPE_WIFI){
                        Toast.makeText(MainActivity.this,"WIFI好像没有连接哦！",Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                else
                {
                    Toast.makeText(MainActivity.this,"WIFI好像没有连接哦！",Toast.LENGTH_SHORT).show();
                    return;
                }


                //保存用户数据
                SaveConfig();

                dialThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Router.Dial(Host, router_psw, Account.getAccount(acc), pwd);

                        Looper.prepare();
                        Toast.makeText(MainActivity.this,"数据包已发送，请稍后查看网络连接情况！",Toast.LENGTH_SHORT).show();
                        Looper.loop();

                    }
                });
                dialThread.start();

            }


        });


    }


    public void SaveConfig(){

        SharedPreferences sharedPreferences=getSharedPreferences(ConfigFile,MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日 "+"hh:mm:ss");

        editor.putString("author","傅攀");
        editor.putString("first-version","闪讯路由拨号器（For Android）V1.0 2017年5月18日");
        editor.putString("version","闪讯拨号 V3.0 2017年12月25日");
        editor.putString("cfg-time",sdf.format(new Date()));
        editor.putString("Host",Host);
        editor.putString("RouterPsw",router_psw);
        editor.putString("Acc",edit_account.getText().toString());
        editor.putString("Pwd",edit_pwd.getText().toString());
        editor.commit();


    }

}
