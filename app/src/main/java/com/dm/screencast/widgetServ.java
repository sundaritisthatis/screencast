package com.dm.screencast;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class widgetServ extends Service {
    int LAYOUT_FLAG;
    View flWin;
    WindowManager windowManager;
    ImageView imageView;
    float hight,width;
    TextView mainBt, chromeBt, closeBt, sendCmBt, SettingBt;
    wifiMan wifiM;
    Context cn,mCn;
    MainActivity.MyThread2 myThread;
    ConstraintLayout cL;
    boolean isConnection;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        cn = getApplicationContext();
        isConnection = false;
        flWin = LayoutInflater.from(this).inflate(R.layout.layout_widget3, null);
        cL = flWin.findViewById(R.id.floating);
        mainBt = (TextView) flWin.findViewById(R.id.Main);
        chromeBt = (TextView) flWin.findViewById(R.id.chromeBox);
        closeBt = (TextView) flWin.findViewById(R.id.Close);
        sendCmBt = (TextView) flWin.findViewById(R.id.upBox);
        SettingBt = (TextView) flWin.findViewById(R.id.settings);
        wifiM = new wifiMan(getApplicationContext());
        mCn = SocketHandler.getContext();
        myThread = SocketHandler.getMyThread2();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, LAYOUT_FLAG, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
//        layoutParams.gravity = Gravity.TOP|Gravity.RIGHT;
        layoutParams.x = 500;
        layoutParams.y = -200;
        WindowManager.LayoutParams imageParams = new WindowManager.LayoutParams(140,140, LAYOUT_FLAG, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
        imageParams.gravity = Gravity.BOTTOM|Gravity.CENTER;
        imageParams.y = 100;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(flWin,layoutParams);
        hight = windowManager.getDefaultDisplay().getHeight();
        width = windowManager.getDefaultDisplay().getWidth();

        closeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
            }
        });
        sendCmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myThread.connected && !Objects.equals(myThread.sshCommandDef, "")) {
                        myThread.sshCommand = myThread.sshCommandDef;
                        myThread.runSSHCommand = true;
                } else {
                    String out ="Error (Not Connected || Command Empty)";
                    Log.i("SSH",out );
                    Toast.makeText(getApplicationContext(), out, Toast.LENGTH_SHORT).show();
                }

            }
        });
        SettingBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myThread.updateCommand = !myThread.updateCommand;
                if(myThread.updateCommand){
                    SettingBt.setBackground(ContextCompat.getDrawable(cn,R.drawable.ic_baseline_settings_red_24));//726493
                }
                else{
                    SettingBt.setBackground(ContextCompat.getDrawable(cn,R.drawable.ic_baseline_settings_applications_24));//
                }

            }
        });

        chromeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isConnection){
                    if(wifiM.isWifi()){
                        Intent intent = new Intent(cn, ipAddressAct.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Log.i("Chrome","IP :"+wifiM.IP );
//                    Toast.makeText(getApplicationContext(), "IP :"+wifiM.IP, Toast.LENGTH_SHORT).show();


                    }
                    else{
                        Log.i("Chrome","Wifi Not Connected" );

                    }
                }

            }
        });

        mainBt.setOnTouchListener(new View.OnTouchListener() {
            int intialX,intialY;
            float intialTouchX,intialTouchY;
            boolean onFl= true;


            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        intialX = layoutParams.x;
                        intialY = layoutParams.y;
                        intialTouchX=motionEvent.getRawX();
                        intialTouchY=motionEvent.getRawY();
                        Log.i("Motion","Down");
                        return true;
                    case MotionEvent.ACTION_UP:
                        Log.i("Motion","Up");
                        int xMov = (int) (motionEvent.getRawX()-intialTouchX);
                        int yMov = (int) (motionEvent.getRawY()-intialTouchY);
                        if (xMov==0 && yMov==0){
                            if(onFl) {
                                onFl = false;
                                openFloating();
                            }
                            else {
                                onFl = true;
                                closeFloating();
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        Log.i("Motion","Move");
                        layoutParams.x=intialX+ (int) (motionEvent.getRawX()-intialTouchX);
                        layoutParams.y = intialY+(int) (motionEvent.getRawY()-intialTouchY);
                        windowManager.updateViewLayout(flWin,layoutParams);
                        Log.i("Motion","Move:"+String.valueOf((motionEvent.getRawX()-intialTouchX))+", "+ String.valueOf(motionEvent.getRawY()-intialTouchY));
                        return true;
                }
                return false;
            }
        });

        return START_STICKY;
    }

    private void openFloating() {
        cL.setBackground(ContextCompat.getDrawable(cn,R.drawable.long_ovel));
        chromeBt.setVisibility(View.VISIBLE);
        closeBt.setVisibility(View.VISIBLE);
        SettingBt.setVisibility(View.VISIBLE);
        sendCmBt.setVisibility(View.VISIBLE);
        mainBt.setBackground(ContextCompat.getDrawable(cn,R.drawable.floting_off));

    }
    private void closeFloating(){
        cL.setBackground(ContextCompat.getDrawable(cn,R.drawable.long_ovel_st));
        chromeBt.setVisibility(View.GONE);
        closeBt.setVisibility(View.GONE);
        SettingBt.setVisibility(View.GONE);
        sendCmBt.setVisibility(View.GONE);
        mainBt.setBackground(ContextCompat.getDrawable(cn,R.drawable.floting_on));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(flWin!=null){
            windowManager.removeView(flWin);
        }
        System.exit(0);
    }




}
