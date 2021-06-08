package com.caiice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Activitytest extends Activity implements View.OnClickListener {
    public static Activity sContext = null;
    public static final String TAG = "src";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sContext = this;

        ImageView iv;
        iv = (ImageView)findViewById(R.id.Viewlogo);
        iv.setOnClickListener(this);

        TextView txtExit = findViewById(R.id.txtExit);
        txtExit.setOnClickListener(this);

        Button button1=(Button)findViewById(R.id.button1);
        button1.setOnClickListener(this);

        Button button2=(Button)findViewById(R.id.button2);
        button2.setOnClickListener(this);

        Button button3=(Button)findViewById(R.id.button3);
        button3.setOnClickListener(this);

        Button button4=(Button)findViewById(R.id.button4);
        button4.setOnClickListener(this);

        Button button5=(Button)findViewById(R.id.button5);
        button5.setOnClickListener(this);

        Button button6=(Button)findViewById(R.id.button6);
        button6.setOnClickListener(this);

        Button button7=(Button)findViewById(R.id.button7);
        button7.setOnClickListener(this);

        Button button8=(Button)findViewById(R.id.button8);
        button8.setOnClickListener(this);

        Button button9=(Button)findViewById(R.id.button9);
        button9.setOnClickListener(this);
        // b =============================================
        Button buttonB1=(Button)findViewById(R.id.buttonB1);
        buttonB1.setOnClickListener(this);
        //cameraView.setMaxFrameSize(1280, 720);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.txtExit:
            {
                finish();//关闭当前活动
            }
            break;
            case R.id.Viewlogo:
            {

            }
            break;
            case R.id.button1:
            {
                //Intent it=new Intent(getApplicationContext(),com.caiice.puzzle15.Puzzle15Activity.class);//启动MainActivity
                //startActivity(it);
            }
            break;
            default:
                break;
        }
    }
}