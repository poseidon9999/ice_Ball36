package com.caiice;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener {
    //需要的权限
    private static final String[] VIDEO_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int VIDEO_PERMISSIONS_CODE = 1;
    private static final int WRITE_EXTERNAL_STORAGE = 2;

    public static Activity sContext = null;
    public static final String TAG = "src";
    Timer timer;
    int num = 0;
    private final static int COUNT = 1;
    private Random rand = new Random(12345);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //getSupportActionBar().hide();//隐藏标题栏

        rand = new Random();//随机数

        /*背景色设置结束*/
        setContentView(R.layout.activity_main);
        sContext = this;
        boolean b1 = requestPermission();//申请权限

        TextView txtExit = findViewById(R.id.txtExit);
        txtExit.setOnClickListener(this);

        int bk =  rand.nextInt(3);
        ImageView iv = (ImageView) findViewById(R.id.imglogo);
        if (bk ==0){
            iv.setImageDrawable(getResources().getDrawable(R.drawable.logo));//更换图片
        }else{
            if (bk ==1){
                iv.setImageDrawable(getResources().getDrawable(R.drawable.logo1));
            }else{
                iv.setImageDrawable(getResources().getDrawable(R.drawable.logo2));
            }
        }

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                timer.cancel();//终止线程
                Intent it = new Intent(getApplicationContext(), Activitytest.class);//启动MainActivity
                startActivity(it);
                finish();//关闭当前活动
            }
        });

        requestPermissionSDcard(); //sdcard 卡权限
        //检查外部存储卡,检查存放模型文件夹
        SdcardService sdcard = new SdcardService();
        String dirPath = sdcard.geWritablePath(sContext, "Yolo36");
        Log.d("this", "info 可用路径:" + dirPath);

        timer = new Timer();
        if (b1) {//获取权限
            initView();
        }
    }

    public void initView() {
        //countDown =  (TextView) findViewById(R.id.textViewTime24);
        //timer = new Timer();
        /*** 每一秒发送一次消息给handler更新UI
         * schedule(TimerTask task, long delay, long period)*/
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(COUNT);
            }
        }, 0, 100);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtExit: {
                Intent it = new Intent(getApplicationContext(), ActivityCamera.class);
                startActivity(it);
                finish();//关闭当前活动
            }
            break;
            default:
                break;
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case COUNT:
                    num++;
                    if (num > 20) {
                        timer.cancel();//0秒结束
                        try {
                            //Intent it=new Intent(getApplicationContext(),CameraColorActivity.class);//启动MainActivity
                            Intent it = new Intent(getApplicationContext(), ActivityCamera.class);
                            startActivity(it);
                            finish();//关闭当前活动
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //Log.i(TAG, "myinfo:秒-> " + String.valueOf(num));
                    break;
                default:
                    break;
            }
        }

        ;
    };

    //申请权限 ↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘↘
    //动态申请权限（一次申请多个动态权限）
    private boolean requestPermission() {
        // 当API大于 23 时，才动态申请权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(MainActivity.this, VIDEO_PERMISSIONS, VIDEO_PERMISSIONS_CODE);
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //先判断有没有权限 ，没有就在这里进行权限的申请
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA}, 1);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private void requestPermissionSDcard() //sdcard 卡权限
    {
        //检查权限
        if (ActivityCompat
                .checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //调用这个方法只会在API>=23的时候才会起作用，否则一律返回false
            // 第一次请求权限时，用户拒绝了，调用后返回true
            // 第二次请求权限时，用户拒绝且选择了“不在提醒”，调用后返回false。
            // 设备的策略禁止当前应用获取这个权限的授权时调用后返回false 。
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) { //此时我们都弹出提示
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE);
            } else {
                //这里是用户各种拒绝后我们也弹出提示
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE);
            }
        }
    }
    //权限回调判断
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case VIDEO_PERMISSIONS_CODE:
                //权限请求失败
                if (grantResults.length == VIDEO_PERMISSIONS.length) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            //弹出对话框引导用户去设置
                            showDialog();
                            Toast.makeText(MainActivity.this, "请求权限被拒绝", Toast.LENGTH_LONG).show();
                            break;
                        }
                    }
                }else{
                    Toast.makeText(MainActivity.this, "已授权", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    //弹出提示框
    private void showDialog(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("录像需要相机、录音和读写权限，是否去设置？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        goToAppSetting();
                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    // 跳转到当前应用的设置界面
    private void goToAppSetting(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
    //申请权限 ↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖↖
}