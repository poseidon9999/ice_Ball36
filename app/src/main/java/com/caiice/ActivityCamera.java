package com.caiice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ActivityCamera extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private ActivityCamera sContext = null;
    private static final int REQUEST_PERMISSION = 100;
    //private CameraBridgeViewBase mOpenCvCameraView;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat currentFrame;
    private Yolo yolo;
    //定义屏幕变量
    static  int outerwidth,outerHeight,densityDpi;//密度
    static  float density;
    private int mWidth,mHeight;//读取摄像头画面大小
    private float scaleX,scaleY;//摄像头画面与屏幕实际比例
    private int OffsetX,OffsetY;///摄像头画面与屏幕偏移量
    //定义画布
    private ImageView iv_canvas;
    protected Bitmap mBitmap;
    private Canvas mCanvas;//画布
    private Paint mPaint;
    int fontsize;//显示字体大小

    //其他
    private boolean isBoard = false;//检测到底板标志
    private Point[] xBoard;//检测到的底板四边形数据
    private Point[] xlastBd;//检测到的底板四边形数据,如果没有位移，则累计识别结果
    private int xSubgrid =0;//每个格子的估算边长
    private Point offsetVal; //检测到的底板四边形数据--偏移量
    private Random rand = new Random(12345);
    private SdcardService sdcard;
    private String datPath ="";//使用文件路径
    private CFPSMaker fpsMaker;//JAVA游戏开发计算显示FPS
    NumberFormat nf;//数字格规范化

    private Boolean check1 =false;
    private Boolean check2 =false;
    private Boolean check3 =false;
    private Boolean check4 =false;
    boolean x_Refresh;//收到数据,刷新画面
    private RadioGroup radioGroup;
    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private int usb_camera;//0-USB摄像头 ,1-手机摄像头

    int [] intGrid = new int[36];//台面网格 36 数据，0-不知道，1-圆，2-三角形，3-矩形
    //四个方向的格子数据,用于对比方向
    int [][]iGrid = {{3,1,2,3,1,2,2,3,1,2,3,1,1,2,3,1,2,3,3,1,2,3,1,2,2,3,1,2,3,1,1,2,3,1,2,3},
            {2,1,3,2,1,3,1,3,2,1,3,2,3,2,1,3,2,1,2,1,3,2,1,3,1,3,2,1,3,2,3,2,1,3,2,1},
            {3,2,1,3,2,1,1,3,2,1,3,2,2,1,3,2,1,3,3,2,1,3,2,1,1,3,2,1,3,2,2,1,3,2,1,3},
            {1,2,3,1,2,3,2,3,1,2,3,1,3,1,2,3,1,2,1,2,3,1,2,3,2,3,1,2,3,1,3,1,2,3,1,2}};
    //四个方向的格子数据,用于对比颜色  0-红 1-绿 2-黑 3-黄
    int [][]iColor = {{0,1,0,1,0,1,2,3,2,3,2,3,0,1,0,1,0,1,2,3,2,3,2,3,0,1,0,1,0,1,2,3,2,3,2,3},
            {1,3,1,3,1,3,0,2,0,2,0,2,1,3,1,3,1,3,0,2,0,2,0,2,1,3,1,3,1,3,0,2,0,2,0,2},
            {3,2,3,2,3,2,1,0,1,0,1,0,3,2,3,2,3,2,1,0,1,0,1,0,3,2,3,2,3,2,1,0,1,0,1,0},
            {2,0,2,0,2,0,3,1,3,1,3,1,2,0,2,0,2,0,3,1,3,1,3,1,2,0,2,0,2,0,3,1,3,1,3,1}};
    Point[] xGrid = new Point[36];//网格对应的坐标
    int x_angle =0;//对应上面的 0-3 编号  角度
    int []x_correspond ={0,0,0,0};//识别数量统计
    Mat img1;

    //识别结果
    int winCount = 0;//识别次数
    int winPlace = 0;//识别位置
    int winShape = 0;//形状  1-圆，2-三角形，3-矩形
    int winColor = -1;//颜色  0-红 1-绿 2-黑 3-黄
    int winConf = 0;//置信度
    int winErr = 0;//未识别统计

    //音频播放
    private MediaPlayer mediaPlayer;

    private ArrayList<YoloChild> sListBox  = new ArrayList<>();//识别球的位置数据

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    try {
                        initializeOpenCVDependencies();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private void initializeOpenCVDependencies() throws IOException {
        mOpenCvCameraView.enableView();
    }


    public ActivityCamera() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
        sContext = this;
        getScreenmetric(sContext);//获得屏幕大小尺寸
        fontsize = (int)(outerHeight /25);//设置显示字体的大小参考数据
        Toast.makeText(sContext, String.format("%d x %d  = %f",outerwidth,outerHeight,density) , Toast.LENGTH_LONG).show();
        rand = new Random();//随机数
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        }

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setMaxFrameSize(640,480);
        mOpenCvCameraView.setCvCameraViewListener(this);

        fpsMaker = new CFPSMaker();//JAVA游戏开发计算显示FPS
        fpsMaker.setNowFPS(System.nanoTime());

        //画布
        BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inScaled = false;////设置这个属性防止因为不同的dpi文件夹导致缩放
        options2.inPreferredConfig = Bitmap.Config.ARGB_8888;

        mBitmap = Bitmap.createBitmap(outerwidth, outerHeight, Bitmap.Config.ARGB_8888);
        iv_canvas = (ImageView) findViewById(R.id.mImageView);
        mCanvas = new Canvas(mBitmap);//自定义一个画布，画布材料是Bitmap对象
        iv_canvas.setImageBitmap(mBitmap);
        //=============================================================
        //在Layout中修改checkbox的属性，注意android:button="@null"是自定义的关键。
        CheckBox checkBox1 = (CheckBox) findViewById(R.id.checkBox1);//取得CheckBox对象
        checkBox1.setX((int)(outerwidth/6));
        checkBox1.setY(5);
        checkBox1.setTextSize(fontsize /density);
        checkBox1.setChecked(true);
        checkBox1.setOnCheckedChangeListener(new ck1_OnCheckedChangeListener());// 绑定事件

        CheckBox checkBox2 = (CheckBox) findViewById(R.id.checkBox2);//取得CheckBox对象
        checkBox2.setX((int)(outerwidth/3));
        checkBox2.setY(5);
        checkBox2.setTextSize(fontsize /density);
        checkBox2.setChecked(true);
        checkBox2.setOnCheckedChangeListener(new ck2_OnCheckedChangeListener());// 绑定事件

        CheckBox checkBox3 = (CheckBox) findViewById(R.id.checkBox3);//取得CheckBox对象
        checkBox3.setX((int)(outerwidth/2));
        checkBox3.setY(5);
        checkBox3.setTextSize(fontsize /density);
        checkBox3.setChecked(true);
        checkBox3.setOnCheckedChangeListener(new ck3_OnCheckedChangeListener());// 绑定事件

        CheckBox checkBox4 = (CheckBox) findViewById(R.id.checkBox4);//取得CheckBox对象
        checkBox4.setX((int)(outerwidth/3*2));
        checkBox4.setY(5);
        checkBox4.setTextSize(fontsize /density);
        checkBox4.setChecked(true);
        checkBox4.setOnCheckedChangeListener(new ck4_OnCheckedChangeListener());// 绑定事件

        //判断文件是否存在
        //检查外部存储卡,检查存放模型文件夹
        sdcard =  new SdcardService();
        //datPath = sdcard.geWritablePath(sContext,"Yolo36");
        datPath = getCocos2dxWritablePath(sContext);

        check1 = sdcard.ReadFromBoolean(sContext,"check1");
        check2 = sdcard.ReadFromBoolean(sContext,"check2");
        check3 = sdcard.ReadFromBoolean(sContext,"check3");
        check4 = sdcard.ReadFromBoolean(sContext,"check4");
        checkBox1.setChecked(check1);
        checkBox2.setChecked(check2);
        checkBox3.setChecked(check3);
        checkBox4.setChecked(check4);
        //=============================================================
        radioGroup = (RadioGroup) this.findViewById(R.id.RadioGroup1);
        radioButton1 = (RadioButton) findViewById(R.id.Radio1);
        radioButton2 = (RadioButton) findViewById(R.id.Radio2);
        radioButton1.setX((float) outerwidth/9*4);
        radioButton1.setTextSize(fontsize /density);
        radioButton2.setX((float) outerwidth/9*5);
        radioButton2.setTextSize(fontsize /density);

        usb_camera = sdcard.ReadFromint(sContext,"camera");//0-USB摄像头 ,1-手机摄像头
        if (usb_camera ==0){
            radioButton1.setChecked(true);
        }else{
            radioButton2.setChecked(true);
        }
        initListener();
        //=============================================================
        // 初始化一个画笔，笔触宽度为5，颜色为红色
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);    //空心矩形框
        //mPaint.setStyle(Paint.Style.FILL);      //实心矩形框
        mPaint.setStrokeWidth(2);
        mPaint.setTextSize(fontsize);
        mPaint.setColor(Color.BLUE);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);//抗锯齿标志
        //XOR：交叠和被交叠部分均不显示;DST_OVER：自身交叠部分不显示；SRC_OVER交叠部分只显示自己
        PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);//设置绘画时重叠部分的处理模式
        mPaint.setXfermode(mode);

        //=============================================================
        initializeOpenCVimg();
        //检测到的底板四边形数据
        xBoard = new Point[4];
        int xx = sdcard.ReadFromint(sContext,"X0");
        int yy = sdcard.ReadFromint(sContext,"Y0");
        xBoard[0] = new Point(xx,yy);
        xx = sdcard.ReadFromint(sContext,"X1");
        yy = sdcard.ReadFromint(sContext,"Y1");
        xBoard[1] = new Point(xx,yy);
        xx = sdcard.ReadFromint(sContext,"X2");
        yy = sdcard.ReadFromint(sContext,"Y2");
        xBoard[2] = new Point(xx,yy);
        xx = sdcard.ReadFromint(sContext,"X3");
        yy = sdcard.ReadFromint(sContext,"Y3");
        xBoard[3] = new Point(xx,yy);

        x_angle = sdcard.ReadFromint(sContext,"x_angle");//角度
        x_correspond[0] = sdcard.ReadFromint(sContext,"x_correspond0");//置信度
        x_correspond[1] = sdcard.ReadFromint(sContext,"x_correspond1");
        x_correspond[2] = sdcard.ReadFromint(sContext,"x_correspond2");
        x_correspond[3] = sdcard.ReadFromint(sContext,"x_correspond3");

        if (xx==0 || yy ==0){//默认值
            xBoard[0] = new Point(80,40);
            xBoard[1] = new Point(80,440);
            xBoard[2] = new Point(560,440);
            xBoard[3] = new Point(560,40);
        }

        xlastBd = new Point[4];
        xlastBd[0] = new Point(xBoard[0].x,xBoard[0].y);
        xlastBd[1] = new Point(xBoard[1].x,xBoard[1].y);
        xlastBd[2] = new Point(xBoard[2].x,xBoard[2].y);
        xlastBd[3] = new Point(xBoard[3].x,xBoard[3].y);

        offsetVal = new Point(80, 0);//检测到的底板四边形数据--偏移量

        //  36个格子的位置
        for(int i=0; i<36; i++){
            xGrid[i] = new Point(0, 0 );
            intGrid[i] =0;
        }
        reckon_rect36();//由边框计算 36个格子的位置
        //四个方向的格子数据,用于对比方向，0-不知道，1-圆，2-三角形，3-矩形

        nf = NumberFormat.getInstance();//数字格规范化
        nf.setGroupingUsed(false);//不使用分组方式显示数据
        nf.setMaximumFractionDigits(2);//设置数值的小数部分允许的最大位数。
        nf.setMaximumIntegerDigits(2);//设置数值的整数部分允许的最大位数
        nf.setMinimumIntegerDigits(2);//设置数值的整数部分允许的最小位数.
        //=============================================================
        //启动定时器
        mHandler.sendEmptyMessageDelayed(0, 1);
    }

    /*** 复选框选中与不选中的事件*/
    private class ck1_OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton button, boolean isChecked){
            check1 = isChecked;
            sdcard.WriteToBoolean(sContext,"check1",check1);
        }
    }
    /*** 复选框选中与不选中的事件*/
    private class ck2_OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton button,boolean isChecked){
            check2 = isChecked;
            sdcard.WriteToBoolean(sContext,"check2",check2);
        }
    }
    /*** 复选框选中与不选中的事件*/
    private class ck3_OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton button,boolean isChecked){
            check3 = isChecked;
            sdcard.WriteToBoolean(sContext,"check3",check3);
        }
    }
    /*** 复选框选中与不选中的事件*/
    private class ck4_OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton button,boolean isChecked){
            check4 = isChecked;
            sdcard.WriteToBoolean(sContext,"check4",check4);
        }
    }
    private void initListener() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.Radio1:
                        usb_camera = 0;//0-USB摄像头 ,1-手机摄像头
                        sdcard.WriteToint(sContext,"camera",usb_camera);
                        break;
                    case R.id.Radio2:
                        usb_camera = 1;//0-USB摄像头 ,1-手机摄像头
                        sdcard.WriteToint(sContext,"camera",usb_camera);
                        break;
                }
            }
        });
        radioButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "单选1");
            }
        });
        radioButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "单选2");
            }
        });
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        mHandler.sendEmptyMessageDelayed(1, 0);//销毁定时器
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mWidth = width;
        mHeight = height;
        scaleY = (float)outerHeight/mHeight;//摄像头画面与屏幕实际比例
        OffsetX = (int)(outerwidth -mWidth *scaleY)/2;//摄像头画面与屏幕偏移量
        OffsetY = 0;
        scaleX = (float)(outerwidth -OffsetX*2)/mWidth;

        try {
            //RFB-320-quant-ADMM-32
            copyBigDataToSD(sContext,"coco2.names");
            copyBigDataToSD(sContext,"yolov3-tiny2.cfg");
            copyBigDataToSD(sContext,"yolov3-tiny2.weights");

        } catch (IOException e) {
            e.printStackTrace();
        }

        yolo = new Yolo(this,
                416, 416,
                datPath,
                "coco2.names",
                "yolov3-tiny2.cfg",
                "yolov3-tiny2.weights",
                0.3f,
                0.2f);
    }

    public void onCameraViewStopped(){
    }
    //获得屏幕大小尺寸
    public void getScreenmetric(Context context){
        //隐藏虚拟按键，并且全屏
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        //获取手机分辨率
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int API_LEVEL = android.os.Build.VERSION.SDK_INT;
        DisplayMetrics metric = new DisplayMetrics();//API 17之后使用，获取的像素宽高包含虚拟键所占空间，在API 17之前通过反射获取
        if (API_LEVEL >= 17) {
            display.getRealMetrics(metric);
        } else {
            display.getMetrics(metric);
        }
        outerwidth = metric.widthPixels;// 宽度（像素）屏幕实际分辨率
        outerHeight = metric.heightPixels; // 高度（像素）
        //outerwidth = sContext.getResources().getDisplayMetrics().widthPixels;//取屏幕宽
        //outerHeight = sContext.getResources().getDisplayMetrics().heightPixels;//- (int) (25 * getResources().getDisplayMetrics().density);//去标题栏
        density = metric.density; // dp缩放因子
        densityDpi = metric.densityDpi;  // 广义密度
        float xdpi = metric.xdpi;//x轴方向的真实密度
        float ydpi = metric.ydpi;//y轴方向的真实密度
    }

    private void initializeOpenCVimg()//读取图片
    {
        img1 = new Mat();
        AssetManager assetManager = this.getAssets();
        try {
            InputStream istr = assetManager.open("table.png");
            Bitmap bitmap = BitmapFactory.decodeStream(istr);
            Utils.bitmapToMat(bitmap, img1);
            //Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGB2GRAY);
            Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2BGRA);//COLOR_BGR2BGRA   COLOR_RGBA2RGB
            //img1.convertTo(img1,0); //converting the image to match with the type of the cameras image
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if ( !check1 && !check3 && !check4){
            currentFrame = onCameraTest(inputFrame);
        }else{
            if (check3){
                currentFrame = Check_Board(inputFrame);//检测球盘,边框
                currentFrame = Check_circle(inputFrame,currentFrame);//检测圆形
            }
            if (check4){
                currentFrame = onCameraYolo(inputFrame);
            }
            if (check1 ){//显示网格
                //xBoard[0] = new Point(80 + rand.nextInt(100) -50,40 + rand.nextInt(100) -50);
                //xBoard[1] = new Point(80 + rand.nextInt(100) -50,440 + rand.nextInt(100) -50);
                //xBoard[2] = new Point(560 + rand.nextInt(100) -50,440 + rand.nextInt(100) -50);
                //xBoard[3] = new Point(500 + rand.nextInt(100) -50,40 + rand.nextInt(100) -50);
                currentFrame = onCameraGrid(inputFrame,xBoard,offsetVal); //网格
            }
            if (check2 ){//显示编号
                currentFrame = showNumGrid(currentFrame); //显示球盘位置编号
            }
        }
        fpsMaker.makeFPS();//计算和显示当前游戏帧速    fpsMaker.getFPS()
        x_Refresh = true;//收到数据,刷新画面
        return currentFrame;//inputFrame.rgba();
    }
    public static Mat combineTwoImages(Mat source, Mat imgOverlay, Rect rectangle) {//没用代码
        Mat srcImg = Objects.requireNonNull(source, "Mat must not be null 不能为空");//判断一个对象是否为空,空的时候报空指针异常的时候就可以使用这个方法。
        Mat src2Img = Objects.requireNonNull(imgOverlay, "bar must not be null 不能为空");
        Mat dstImg = new Mat();
        Core.addWeighted(srcImg.submat(rectangle), 0.1, src2Img, 1.0, 0, dstImg.submat(rectangle));//aInputFrame.submat(rectangle)
        return source;
    }
    public Mat onCameraTest(CameraBridgeViewBase.CvCameraViewFrame inputFrame) //其他
    {
        //currentFrame = openCV_effect.ret_CheckTest(inputFrame);
        currentFrame = inputFrame.rgba();
        return currentFrame;
    }
    public Mat onCameraYolo(CameraBridgeViewBase.CvCameraViewFrame inputFrame) //球
    {
        currentFrame = inputFrame.rgba();
        Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_RGBA2RGB);
        yolo.detect(currentFrame);
        //sListBox.clear();//清除返回数据
        sListBox = yolo.getBoundingBox();
        return currentFrame;
    }
    private Mat onCameraGrid(CameraBridgeViewBase.CvCameraViewFrame inputFrame,Point pt[],Point retVal) //网格
    {
        currentFrame = inputFrame.rgba();
        Imgproc.cvtColor(currentFrame, currentFrame, Imgproc.COLOR_BGR2BGRA);
        Mat distortedFrame =  new Mat(img1.size(), img1.type());//Mat distortedFrame = img1.clone();//会另外开辟一个内存存储被复制的数据区域
        Imgproc.cvtColor(img1, distortedFrame, Imgproc.COLOR_BGR2BGRA);

        Mat renderedFrame = openCV_effect.warpPerspective(distortedFrame,pt, offsetVal,x_angle,usb_camera);//图像透视视角转换
        org.opencv.core.Rect rectangle = new org.opencv.core.Rect(80,0,  renderedFrame.cols(),renderedFrame.rows());
        Core.addWeighted(currentFrame.submat(rectangle), 1.0, renderedFrame, 0.5, 0, currentFrame.submat(rectangle));//acurrentFrame.submat(rectangle)

        Imgproc.line(currentFrame, new Point(xBoard[0].x, xBoard[0].y), new Point(xBoard[1].x, xBoard[1].y), new Scalar(0, 255, 0), 2);
        Imgproc.line(currentFrame, new Point(xBoard[1].x, xBoard[1].y), new Point(xBoard[2].x, xBoard[2].y), new Scalar(0, 255, 0), 2);
        Imgproc.line(currentFrame, new Point(xBoard[2].x, xBoard[2].y), new Point(xBoard[3].x, xBoard[3].y), new Scalar(0, 255, 0), 2);
        Imgproc.line(currentFrame, new Point(xBoard[3].x, xBoard[3].y), new Point(xBoard[0].x, xBoard[0].y), new Scalar(0, 255, 0), 2);

        renderedFrame.release();
        distortedFrame.release();
        return currentFrame;
    }
    void rotation_angle()//旋转网格角度计算
    {
        int arr[] = {0,0,0,0};
        for(int i=0; i<36; i++){
            if (iGrid[0][i] == intGrid[i]){ arr[0]++;}
            if (iGrid[1][i] == intGrid[i]){ arr[1]++;}
            if (iGrid[2][i] == intGrid[i]){ arr[2]++;}
            if (iGrid[3][i] == intGrid[i]){ arr[3]++;}
        }
        x_correspond[0] = arr[0];//置信度
        x_correspond[1] = arr[1];
        x_correspond[2] = arr[2];
        x_correspond[3] = arr[3];
        //求最大值
        int sum = arr[0];//假设第一个元素是最大值
        x_angle =0;//对应上面的 0-3 编号
        //for循环遍历数组中元素，每次循环跟数组索引为0的元素比较大小
        for (int i = 0; i < arr.length; i++){
            if (sum < arr[i]){//数组中的元素跟sum比较，比sum大就把它赋值给sum作为新的比较值
                sum = arr[i];
                x_angle = i;
            }
        }
        //Log.i(TAG, "网格角度:" + String.valueOf(arr[0]) + "," + String.valueOf(arr[1]) + "," +String.valueOf(arr[2]) + "," + String.valueOf(arr[3]) + "-" + String.valueOf(x_angle));
    }
    private void saveBoardGrid() //保存球盘位置数据
    {
        sdcard.WriteToint(sContext,"X0",(int)xBoard[0].x);
        sdcard.WriteToint(sContext,"Y0",(int)xBoard[0].y);
        sdcard.WriteToint(sContext,"X1",(int)xBoard[1].x);
        sdcard.WriteToint(sContext,"Y1",(int)xBoard[1].y);
        sdcard.WriteToint(sContext,"X2",(int)xBoard[2].x);
        sdcard.WriteToint(sContext,"Y2",(int)xBoard[2].y);
        sdcard.WriteToint(sContext,"X3",(int)xBoard[3].x);
        sdcard.WriteToint(sContext,"Y3",(int)xBoard[3].y);

        sdcard.WriteToint(sContext,"x_angle",x_angle);
        sdcard.WriteToint(sContext,"x_correspond0",x_correspond[0]);
        sdcard.WriteToint(sContext,"x_correspond1",x_correspond[1]);
        sdcard.WriteToint(sContext,"x_correspond2",x_correspond[2]);
        sdcard.WriteToint(sContext,"x_correspond3",x_correspond[3]);
    }
    private Mat showNumGrid(Mat board) //显示球盘位置编号
    {
        //查看显示 36 个位置坐标
        for(int xx=0; xx<6; xx++){
            for(int yy =0; yy<6; yy++){
                int s = xx *6 +yy;
                //Imgproc.circle(board, new Point(xGrid[s].x, xGrid[s].y), 4, new Scalar(0, 255, 255), 1);
                Scalar mycolor = new Scalar(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));

                //将文本框居中绘制
                //Imgproc.putText(board, Integer.toString(s), new Point(xGrid[s].x - text_size.width / 2, xGrid[s].y + text_size.height / 2), Core.FONT_HERSHEY_SIMPLEX ,1.0, mycolor,2);
                //Imgproc.putText(board, Integer.toString(s), new Point(xGrid[s].x + text_size.width / 3, xGrid[s].y + text_size.height / 3), Core.FONT_HERSHEY_SIMPLEX ,0.75, mycolor,2);

                //绘制识别结果
                //intGrid[s]  这个变量是已识别的部分
                switch (iGrid[x_angle][s])//这个是预测值，台面网格 36 数据，0-不知道，1-圆，2-三角形，3-矩形
                {
                    case 0://中心点
                        Imgproc.line(board, new Point(xGrid[s].x -8, xGrid[s].y), new Point(xGrid[s].x +8, xGrid[s].y), mycolor, 2);//圆心点
                        Imgproc.line(board, new Point(xGrid[s].x, xGrid[s].y -8), new Point(xGrid[s].x, xGrid[s].y +8), mycolor, 2);
                        break;
                    case 1://圆
                        Imgproc.circle(board, new Point(xGrid[s].x, xGrid[s].y), (int) 10, mycolor, 2);//圆
                        break;
                    case 2://绘制三角形
                        List<MatOfPoint> list = new ArrayList();
                        list.add(
                                new MatOfPoint (
                                        new Point(xGrid[s].x, xGrid[s].y -8),
                                        new Point(xGrid[s].x -10, xGrid[s].y +10),
                                        new Point(xGrid[s].x +10, xGrid[s].y +10)
                                )
                        );
                        // 第三个参数指的是是否封口,这里注意第二个参数外面必须再加一层中括号
                        // Drawing polylines
                        Imgproc.polylines (
                                board,                    // Matrix obj of the image
                                list,                      // java.util.List<MatOfPoint> pts
                                true,                     // isClosed
                                 mycolor,     // Scalar object for color    new Scalar(0, 0, 255)
                                2                          // Thickness of the line
                        );
                        break;
                    case 3://矩形
                        Imgproc.rectangle(board, new Point(xGrid[s].x - 9, xGrid[s].y - 9), new Point(xGrid[s].x + 9, xGrid[s].y + 9), mycolor, 2);//矩形
                        break;
                    default:
                        break;
                }
                //显示颜色提示文字
                String txt = "颜色"; //Integer.toString(s);
                mycolor = new Scalar(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
                switch (iColor[x_angle][s])//显示颜色  0-红 1-绿 2-黑 3-黄
                {
                    case 0: txt = "R";break;
                    case 1: txt = "G";break;
                    case 2: txt = "B";break;
                    case 3: txt = "Y";break;
                    default: break;
                }
                if (s ==0 || s==5 || s==30 || s==35){
                    txt = Integer.toString(s);//显示编号
                }
                int[] baseLine = new int[1];
                int font_face = Core.FONT_HERSHEY_COMPLEX;
                Size text_size = Imgproc.getTextSize(txt, font_face, 0.5, 2,baseLine);//获取文本框的长宽
                Imgproc.putText(board,txt, new Point(xGrid[s].x - text_size.width / 2, xGrid[s].y + text_size.height / 2), Core.FONT_HERSHEY_SIMPLEX ,0.5, mycolor,2);
            }
        }
        return board;
    }
    //检测圆形
    public Mat Check_circle(CameraBridgeViewBase.CvCameraViewFrame inputFrame,Mat board)
    {
        //Mat input = inputFrame.rgba();
        Mat midImage = inputFrame.gray();//单通道图像

        Mat blurMat = new Mat();
        Mat circles = new Mat();

        //检测其他图形
        Mat thresh = new Mat();
        Imgproc.GaussianBlur(midImage, thresh, new Size(3,3), 0);//高斯模糊
        Imgproc.Canny(thresh, thresh, 10, 100, 3, false);// Canny边缘检测
        //Imgproc.dilate(thresh, thresh, new Mat(), new Point(-1,-1), 3, 1, new Scalar(1));// 膨胀，连接边缘
        Imgproc.GaussianBlur(thresh, thresh, new Size(5, 3), 2, 2 );//去噪

        List<MatOfPoint> contoursRect = new ArrayList<>();
        Mat hier = new Mat();
        Imgproc.findContours(thresh, contoursRect, hier, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);//轮廓提取
        Imgproc.drawContours(board, contoursRect, -1, new Scalar(0,0,255,255));//查看画出轮廓,函数轮廓绘制

        // 代码 minEnclosingCircle   最小包围圆的算法
        MatOfPoint2f[] contoursPoly  = new MatOfPoint2f[contoursRect.size()];//由轮廓确定正外接矩形及最小外接矩形
        Rect[] boundRect = new Rect[contoursRect.size()];//正外接矩形
        Rect[] RotatedRect= new Rect[contoursRect.size()];//最小外接矩形
        Point[] centers = new Point[contoursRect.size()];//最小包围圆的中心点
        //float[][] radius = new float[contoursRect.size()][1];//最小外接圆的半径
        float[][] radius = new float[contoursRect.size()][1];//最小外接圆的半径

        for (int i = 0; i < contoursRect.size(); i++) {
            contoursPoly[i] = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contoursRect.get(i).toArray()), contoursPoly[i], 3, true);//多边拟合函数
            boundRect[i] = Imgproc.boundingRect(new MatOfPoint(contoursPoly[i].toArray()));//包围轮廓的矩形框,垂直边界最小矩形，矩形是与图像上下边界平行的
            centers[i] = new Point();
            Imgproc.minEnclosingCircle(contoursPoly[i], centers[i], radius[i]);//最小包围圆的算法
        }
        //Mat drawing = Mat.zeros(grayMat.size(), CvType.CV_8UC3);
        List<MatOfPoint> contoursPolyList = new ArrayList<>(contoursPoly.length);
        for (MatOfPoint2f poly : contoursPoly) {
            contoursPolyList.add(new MatOfPoint(poly.toArray()));
        }
        int xCount1 =0;//统计筛选后的有效图块数量
        int xCount2 = contoursRect.size();//统计筛选后的有效图块数量
        for (int i = 0; i < contoursRect.size(); i++) {
            Scalar mycolor = new Scalar(255,0,0);//new Scalar(rng.nextInt(128)+128, rng.nextInt(128)+128, rng.nextInt(128)+128);
            final MatOfPoint contour = contoursRect.get(i);
            final Rect bb = Imgproc.boundingRect(contour);
            double area = Math.abs(Imgproc.contourArea(contour));//默认计算整个轮廓的面积,返回值可能是负值，需要用fabs()转成正值
            //Imgproc.drawContours(grayMat, contoursPolyList, i, mycolor);//显示过滤前的结果
            if (area > 100 && area < 5000) {//面积过滤
                boolean b1 = false;
                int ww = boundRect[i].width;//矩形获取
                int hh = boundRect[i].height;
                //Log.i("myinfo","过滤:"+ Double.toString(xx) +","+ Double.toString(yy) +","+ Double.toString(hh) +","+ Double.toString(ww));
                //显示矩形边长
                //Imgproc.putText(input, Integer.toString((int)boundRect[i].width) + "," + Integer.toString((int)boundRect[i].height), new Point(boundRect[i].x, boundRect[i].y), Core.FONT_HERSHEY_PLAIN, 1.0, new Scalar(255,0,0));
                if (hh > 25 && hh < 100 && ww > 25 && ww < 100) {//过滤矩形边长大小
                    if (hh > ww/2 && ww > hh/2) {//过滤矩形边长比例,接近正方形
                        //显示圆半径
                        //Imgproc.putText(input, Integer.toString((int)radius[i][0]), new Point(centers[i].x, centers[i].y), Core.FONT_HERSHEY_PLAIN, 1.0, new Scalar(255,0,0));
                        if (radius[i][0] > 20 && radius[i][0] < 50) {//过滤大小,最小包围圆的半径
                            int cx1 = boundRect[i].x + ww / 2;//矩形中心点
                            int cy1 = boundRect[i].y + hh / 2;
                            int cx2 = (int) centers[i].x;//最小包围圆的中心点
                            int cy2 = (int) centers[i].y;
                            int cr = (int) radius[i][0];////最小包围圆的半径
                            if (cr>20 && cr<100){//限制圆的大小
                                double l1 = getSpacePointToPoint(cx1, cy1, cx2, cy2);//点到点的距离,两个中心点不能相差太多
                                if (l1 < 30) {
                                    //Imgproc.rectangle(input, new Point(cx1 - 3, cy1 - 3), new Point(cx1 + 3, cy1 + 3), mycolor, 1);//标记出矩形中心点位置
                                    b1 = true;
                                }
                            }
                        }
                    }
                }

                //质心获取
                Moments mom = Imgproc.moments(contour);//质心获取
                int x1 = (int)(mom.get_m10()/mom.get_m00());
                int y1 = (int)(mom.get_m01()/mom.get_m00());
                Point p = new Point(x1,y1);

                if (b1 ){
                    MatOfPoint2f m = new MatOfPoint2f(contour.toArray());
                    double peri = Imgproc.arcLength(m, true);
                    MatOfPoint2f approx = new MatOfPoint2f();
                    Imgproc.approxPolyDP(m, approx, 0.02 * peri, true);//用指定的精度逼近多边形曲线,多边形包围轮廓
                    //Imgproc.putText(grayMat, Integer.toString((int)approx.total()), new Point(centers[i].x, centers[i].y), Core.FONT_HERSHEY_PLAIN, 1.0, new Scalar(255,0,0));

                    //Imgproc.circle(grayMat, p, 8, mycolor, 1);//画出质心圆
                    //当measureDist设置为true时，返回实际距离值。若返回值为正，表示点在多边形内部，返回值为负，表示在多边形外部，返回值为0，表示在多边形上。
                    //当measureDist设置为false时，返回 -1、0、1三个固定值。若返回值为+1，表示点在多边形内部，返回值为-1，表示在多边形外部，返回值为0，表示在多边形上
                    double distance = Imgproc.pointPolygonTest(m, p, true);//检测质心点是否在轮廓内,判断一个点是否在一个contour的内部还是外部。
                    //显示质心点到轮廓的最小距离
                    //Imgproc.putText(input, Integer.toString((int)distance), new Point(centers[i].x, centers[i].y), Core.FONT_HERSHEY_PLAIN, 1.0, new Scalar(255,0,0));
                    if (distance < 0){//在轮廓内
                        //if (approx.total() > 3) {//四边
                        //    Point[] pos = approx.toArray();
                        //    int cl = (int)radius[i][0];//最小圆半径
                        //    int sum = (int)approx.total();
                        //    for(int k=1; k< pos.length-1; k++){
                                //double ll1 = getSpacePointToPoint(pos[k].x, pos[k].y, pos[(k+1)%sum].x, pos[(k+1)%sum].y);//相邻两点的距离
                                //double ll2 = getSpacePointToPoint(pos[(k+1)%sum].x, pos[(k+1)%sum].y, centers[i].x, centers[i].y);
                                //角度验证
                         //       double cosine = getIn_angle(pos[k].x,pos[k].y,pos[(k+1)%sum].x,pos[(k+1)%sum].y,pos[k-1].x,pos[k-1].y);
                         //       if(cosine <45){
                         //           b1 = false;
                         //           break;
                         //       }
                         //   }
                        //}
                        b1 = false;
                    }else{
                        if (approx.total() == 3) //三角形
                        {
                            Imgproc.drawContours(board, contoursPolyList, i, new Scalar(255,255,255),2);//显示过滤后的结果
                            int ls = ret_nearby(x1,y1);//返回最近的点位置序号,质心
                            if (ls>=0 && ls<36){
                                intGrid[ls] = 2;//0-不知道，1-圆，2-三角形，3-矩形
                            }
                        }
                    }
                }
                if (b1){
                    Imgproc.drawContours(board, contoursPolyList, i, new Scalar(0,255,0),1);//显示过滤后的结果
                    //Imgproc.rectangle(input, boundRect[i].tl(), boundRect[i].br(), mycolor, 1);//显示过滤后的矩形结果
                    //Imgproc.circle(input, centers[i], (int) radius[i][0], new Scalar(0,255,0), 2);//最小包围圆
                }
            }
        }

        //检测圆形
        //Imgproc.cvtColor(input ,midImage, Imgproc.COLOR_BGR2GRAY);//彩色转灰度的函数,转化边缘检测后的图为灰度图//彩色转灰度的函数,转化边缘检测后的图为灰度图
        Imgproc.GaussianBlur(midImage, blurMat, new Size(3,3), 0);//高斯模糊
        //String s= "myinfo:图像大小-> " + String.valueOf(mWidth) + ", " + String.valueOf(bmpH) + ", " + String.valueOf(input.channels()) + ", " + String.valueOf(input.depth());
        //Imgproc.Canny(blurMat, blurMat, 10, 90);
        Imgproc.HoughCircles(blurMat, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 100, 100, 100, 0, 50);
        //Log.i(TAG, String.valueOf("size-> " + circles.cols()) + ", " + String.valueOf(circles.rows()));
        if (circles.cols() > 0) {
            //Log.i(TAG, "myinfo:圆-> " + String.valueOf(circles.cols()));
            for (int x=0; x < Math.min(circles.cols(), 12); x++ ) {
                double circleVec[] = circles.get(0, x);
                if (circleVec == null) {
                    break;
                }
                Point center = new Point((int) circleVec[0], (int) circleVec[1]);
                int clradius = (int) circleVec[2];
                //Imgproc.circle(input, center, 3, new Scalar(255, 255, 255), 5);
                //Imgproc.circle(input, center, radius, new Scalar(255, 255, 255), 2);
                Imgproc.circle(board, center, clradius+4, new Scalar(0, 255, 255), 2);
                int ls = ret_nearby( (int)center.x, (int)center.y);//返回最近的点位置序号,质心
                if (ls>=0 && ls<36){
                    intGrid[ls] = 1;//0-不知道，1-圆，2-三角形，3-矩形
                }
                //Imgproc.putText(input, Integer.toString(radius), new Point(center.x -8, center.y +8), Core.FONT_HERSHEY_PLAIN, 1.0, new Scalar(255,0,0));
            }
        }
        //isBoard
        Imgproc.line(board, new Point(xBoard[0].x, xBoard[0].y), new Point(xBoard[1].x, xBoard[1].y), new Scalar(0, 255, 0), 2);
        Imgproc.line(board, new Point(xBoard[1].x, xBoard[1].y), new Point(xBoard[2].x, xBoard[2].y), new Scalar(0, 255, 0), 2);
        Imgproc.line(board, new Point(xBoard[2].x, xBoard[2].y), new Point(xBoard[3].x, xBoard[3].y), new Scalar(0, 255, 0), 2);
        Imgproc.line(board, new Point(xBoard[3].x, xBoard[3].y), new Point(xBoard[0].x, xBoard[0].y), new Scalar(0, 255, 0), 2);

        hier.release();
        thresh.release();
        circles.release();
        midImage.release();
        blurMat.release();
        return board;
        //return inputFrame.rgba();
    }
    //检测球盘,边框
    public Mat Check_Board(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        Mat input = inputFrame.rgba();
        Mat grayMat = inputFrame.gray();
        Mat blurMat = new Mat();
        Mat thresh = new Mat();
        Imgproc.GaussianBlur(input, blurMat, new Size(3,3), 0);//高斯模糊
        Imgproc.cvtColor(blurMat ,blurMat, Imgproc.COLOR_BGR2GRAY);//彩色转灰度的函数,转化边缘检测后的图为灰度图
        Imgproc.medianBlur(blurMat, blurMat, 3);//平均模糊

        // Binary:自适应Canny阈值算法
        //int maskRoiX = 10;
        //int maskRoiY = 10;
        //int maskRoiW = blurMat.cols()-10;
        //int maskRoiH = blurMat.rows()-10;
        //Rect maskRoi = new Rect(maskRoiX, maskRoiY, maskRoiW, maskRoiH);
        //Mat maskSrc = new Mat(blurMat, maskRoi);
        //CannyUtils utils = new CannyUtils();
        //utils.FindAdaptiveThreshold(maskSrc, 3, 0.80);
        //double thCannyLow = utils.getM_cannyLowTh();
        //double thCannyHigh = utils.getM_cannyHighTh();
        //int imgRows = blurMat.rows();
        //int imgCols = blurMat.cols();
        //Mat thresh = Mat.zeros(imgRows, imgCols, CvType.CV_8UC1);
        //Imgproc.Canny(blurMat, thresh, thCannyLow, thCannyHigh, 3, false);//使用自适应Canny阈值算法

        //Mat thresh = new Mat();
        //Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C
        Imgproc.adaptiveThreshold(blurMat, thresh, 255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,1,11,4);//图像自适应阈值操作
        Imgproc.Canny(thresh, thresh, 10, 100, 3, false);// Canny边缘检测
        //Imgproc.dilate(thresh, thresh, new Mat(), new Point(-1,-1), 3, 1, new Scalar(1));// 膨胀，连接边缘
        Imgproc.GaussianBlur(thresh, thresh, new Size(5, 3), 2, 2 );//去噪

        List<MatOfPoint> contoursRect = new ArrayList<>();
        Mat hier = new Mat();
        Imgproc.findContours(thresh, contoursRect, hier, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);//轮廓提取
        Imgproc.drawContours(input, contoursRect, -1, new Scalar(0, 0, 255, 255));//查看画出轮廓,函数轮廓绘制

        //找到最大四边形
        double max_area = 0;//最大mianji
        MatOfPoint2f biggest = new MatOfPoint2f();//从列表 转换为 MatOfPoint2f  保存最大面积
        for (MatOfPoint i : contoursRect) {//循环检测，选取最大的一个四边形
            double area = Math.abs(Imgproc.contourArea(i));//默认计算整个轮廓的面积,返回值可能是负值，需要用fabs()转成正值
            if (area > 40000) //200x200
            {
                MatOfPoint2f m = new MatOfPoint2f(i.toArray());
                double peri = Imgproc.arcLength(m, true);//计算图像轮廓的周长
                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(m, approx, 0.02 * peri, true);//用指定的精度逼近多边形曲线,多边形包围轮廓
                if (area > max_area && approx.total() == 4) {//四边
                    Point[] pos = approx.toArray();
                    boolean b1 = isConvex(pos);//四边形正则检查
                    if (b1){
                        biggest = approx;
                        max_area = area;
                    }
                }
            }
        }
        Point[] points = biggest.toArray();
        if (points.length == 4) {
            isBoard = true;
            xBoard = biggest.toArray();//检测到的底板四边形数据
            Imgproc.line(input, new Point(xBoard[0].x, xBoard[0].y), new Point(xBoard[1].x, xBoard[1].y), new Scalar(0, 255, 0), 2);
            Imgproc.line(input, new Point(xBoard[1].x, xBoard[1].y), new Point(xBoard[2].x, xBoard[2].y), new Scalar(0, 255, 0), 2);
            Imgproc.line(input, new Point(xBoard[2].x, xBoard[2].y), new Point(xBoard[3].x, xBoard[3].y), new Scalar(0, 255, 0), 2);
            Imgproc.line(input, new Point(xBoard[3].x, xBoard[3].y), new Point(xBoard[0].x, xBoard[0].y), new Scalar(0, 255, 0), 2);

            //判断是否镜像翻转
            if (xBoard[1].x > xBoard[0].x){
                Point tpos = xBoard[0];
                xBoard[0] = xBoard[1];
                xBoard[1] = tpos;
                Point tpos1 = xBoard[2];
                xBoard[2] = xBoard[3];
                xBoard[3] = tpos1;
                Log.e(TAG, "镜像翻转!");
            }
            reckon_rect36();//由边框计算 36个格子的位置
            rotation_angle();//旋转网格角度计算
        }
        hier.release();
        thresh.release();
        blurMat.release();
        grayMat.release();
        return input;
    }
    void reckon_rect36()//由边框计算 36个格子的位置
    {
        int l1 = (int)getSpacePointToPoint(xBoard[0].x, xBoard[0].y, xBoard[1].x, xBoard[1].y);//点到点的距离
        int l2 = (int)getSpacePointToPoint(xBoard[1].x, xBoard[1].y, xBoard[2].x, xBoard[2].y);
        int l3 = (int)getSpacePointToPoint(xBoard[2].x, xBoard[2].y, xBoard[3].x, xBoard[3].y);
        int l4 = (int)getSpacePointToPoint(xBoard[3].x, xBoard[3].y, xBoard[0].x, xBoard[0].y);

        xSubgrid = (l1+l2+l3+l4)/24;//每个格子的估算边长
        //Log.e(TAG,  "检测边框" + Integer.toString(l1) + "," + Integer.toString(l2) +  "," + Integer.toString(l3) +  "," + Integer.toString(l4));
        //生成6 *6 格子坐标中心点数据
        double offm = 1.00;//去除边框的距离常量
        double ww1 = (xBoard[1].x -xBoard[0].x);//竖边
        double ww2 = (xBoard[2].x -xBoard[3].x);
        double hh1 = (xBoard[1].y -xBoard[0].y);
        double hh2 = (xBoard[2].y -xBoard[3].y);

        double pw1 = (xBoard[3].x -xBoard[0].x);
        double pw2 = (xBoard[2].x -xBoard[1].x);
        double ph1 = (xBoard[3].y -xBoard[0].y);
        double ph2 = (xBoard[2].y -xBoard[1].y);

        for(int xx=0; xx<6; xx++){
            double tx1 = xBoard[0].x + ww1 * offm/6*xx + ww1/12 + (1-offm)* ww1/2;
            double ty1 = xBoard[0].y + hh1 * offm/6*xx + hh1/12;
            double tx2 = xBoard[3].x + ww2 * offm/6*xx + ww2/12 + (1-offm)* ww2/2;
            double ty2 = xBoard[3].y + hh2 * offm/6*xx + hh2/12;
            //Imgproc.line(input, new Point(tx1, ty1), new Point(tx2, ty2), new Scalar(255, 0, 0), 1);
            //Imgproc.circle(input, new Point(tx1, ty1), 4, new Scalar(255, 0, 0), 2);
            //Imgproc.circle(input, new Point(tx2, ty2), 4, new Scalar(0, 0, 255), 2);
            //Imgproc.putText(input, Integer.toString(xx), new Point(tx1, ty1), Core.FONT_HERSHEY_PLAIN, 1.0, new Scalar(255,0,0));
            for(int i =0; i<6; i++){// x 坐标
                int s = (5-xx) + i *6;
                xGrid[s].x = tx1 + (tx2 - tx1)/6 *i + (tx2 - tx1)/12 ;
                //Log.i(TAG, "坐标-> " + String.valueOf(xx) + ", " + String.valueOf(i) + ", " + String.valueOf(s));
            }
            double px1 = xBoard[0].x + pw1 * offm/6*xx + pw1/12;
            double py1 = xBoard[0].y + ph1 * offm/6*xx + ph1/12 + (1-offm)* ph1/2;
            double px2 = xBoard[1].x + pw2 * offm/6*xx + pw2/12;
            double py2 = xBoard[1].y + ph2 * offm/6*xx + ph2/12 + (1-offm)* ph2/2;
            //Imgproc.line(input, new Point(px1, py1), new Point(px2, py2), new Scalar(255, 0, 0), 1);
            //Imgproc.putText(input, Integer.toString(xx), new Point(px1, py1), Core.FONT_HERSHEY_PLAIN, 1.0, new Scalar(255,0,0));
            for(int i =0; i<6; i++){// y 坐标
                int s = xx *6 +(5-i) ;
                xGrid[s].y = py1 + (py2 -py1 )/6 *i + (py2 -py1 )/12;
            }
        }
    }
    int ret_nearby(int x,int y)//返回最近的点位置序号
    {
        int sl =  1000;
        int index = -1;
        for(int i =0; i<36; i++){
            int l1 = (int)getSpacePointToPoint(xGrid[i].x, xGrid[i].y, x, y);//点到点的距离
            if (l1 < xSubgrid/2){//格子大小,圆半径
                if ( l1<sl){
                    sl =l1;
                    index =i;
                }
            }
        }
        return index;
    }

    //判断四边形是否为凸四边形,寻找凸包
    boolean isConvex(Point[] pt) {
        int n = 4;
        double xa, ya, xb, yb, xc, yc;
        int prevsign = 0;
        for(int i=0; i<n; i++){
            xa = pt[i].x;
            ya = pt[i].y;
            xb = pt[(i + 1) % n].x;
            yb = pt[(i + 1) % n].y;
            xc = pt[(i + 2) % n].x;
            yc = pt[(i + 2) % n].y;
            int cur =(int) ((xc-xb) * (yb-ya) - (yc-yb) * (xb-xa));
            if(prevsign == 0){
                prevsign = cur;
            } else {
                if(prevsign < 0 && cur > 0) return false;
                if(prevsign > 0 && cur < 0) return false;
            }
        }
        //四边形角度验证
        double maxCosine = 0;
        for (int j = 2; j < 5; j++) {
            double cosine = Math.abs(getAngle(pt[j%4].x,pt[j%4].y,pt[j-2].x,pt[j-2].y,pt[j-1].x,pt[j-1].y));
            maxCosine = Math.max(maxCosine, cosine);
        }
        //Log.i("myinfo","角度:"+ Double.toString(maxCosine));//将double转换为字符串
        if (maxCosine > 0.3) {// 角度大概72度
            //Log.i("myinfo","角度:"+ Double.toString(maxCosine));//将double转换为字符串
            return false;
        }
        //判断边长
        for(int i=0; i<n; i++){
            xa = pt[i].x;
            ya = pt[i].y;
            xb = pt[(i + 1) % n].x;
            yb = pt[(i + 1) % n].y;
            xc = pt[(i + 2) % n].x;
            yc = pt[(i + 2) % n].y;
            double cur1 = getSpacePointToPoint(xa, ya, xb, yb);
            double cur2 = getSpacePointToPoint(xb, yb, xc, yc);
            double ck = Math.abs(cur1-cur2);//两边差距
            if(ck > cur1/3 || ck > cur2/3){
                //Log.i("myinfo","边长:"+ Double.toString(ck));//将double转换为字符串
                return false;
            }
        }
        return true;
    }
    // 点到点的距离
    private static double getSpacePointToPoint(double p1x, double p1y,double p2x, double p2y) {
        double a = p1x - p2x;
        double b = p1y - p2y;
        return Math.sqrt(a * a + b * b);
    }
    // 两直线的交点
    private static Point computeIntersect(double[] a, double[] b) {
        if (a.length != 4 || b.length != 4)
            throw new ClassFormatError();
        double x1 = a[0], y1 = a[1], x2 = a[2], y2 = a[3], x3 = b[0], y3 = b[1], x4 = b[2], y4 = b[3];
        double d = ((x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4));
        if (d != 0) {
            Point pt = new Point();
            pt.x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
            pt.y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
            return pt;
        }
        else
            return new Point(-1, -1);
    }
    // 根据三个点计算中间那个点的夹角
    private  static double getIn_angle(double x1, double x2, double y1, double y2, double z1, double z2) {
        //向量的点乘
        double t =(y1-x1)*(z1-x1)+(y2-x2)*(z2-x2);

        //为了精确直接使用而不使用中间变量
        //包含了步骤：A=向量的点乘/向量的模相乘
        //          B=arccos(A)，用反余弦求出弧度
        //          result=180*B/π 弧度转角度制
        double result =(int)(180*Math.acos(
                t/Math.sqrt
                        ((Math.abs((y1-x1)*(y1-x1))+Math.abs((y2-x2)*(y2-x2)))
                                *(Math.abs((z1-x1)*(z1-x1))+Math.abs((z2-x2)*(z2-x2)))
                        ))
                /Math.PI);
        //      pi   = 180
        //      x    =  ？
        //====> ?=180*x/pi
        return result;
    }
    // 根据三个点计算中间那个点的夹角   pt1 pt0 pt2
    private static double getAngle(double pt1x, double pt1y,double pt2x, double pt2y, double pt0x, double pt0y)
    {
        double dx1 = pt1x - pt0x;
        double dy1 = pt1y - pt0y;
        double dx2 = pt2x - pt0x;
        double dy2 = pt2y - pt0y;
        return (dx1*dx2 + dy1*dy2)/Math.sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
    }
    //如何判断一个点在任意四边形内
    public static boolean pInQuadrangle(Point a, Point b, Point c, Point d, Point p) {
        double dTriangle = triangleArea(a, b, p) + triangleArea(b, c, p)
                + triangleArea(c, d, p) + triangleArea(d, a, p);
        double dQuadrangle = triangleArea(a, b, c) + triangleArea(c, d, a);
        return dTriangle == dQuadrangle;
    }
    // 返回三个点组成三角形的面积
    private static double triangleArea(Point a, Point b, Point c) {
        double result = Math.abs((a.x * b.y + b.x * c.y + c.x * a.y - b.x * a.y
                - c.x * b.y - a.x * c.y) / 2.0D);
        return result;
    }

    //定时器,只要在启动定时器的时候，Handler.sendEmptyMessage(0)，定时器就启动了
    //private Handler mHandler;//全局变量
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    // 移除所有的msg.what为0等消息，保证只有一个循环消息队列再跑
                    mHandler.removeMessages(0);
                    //String s= "myinfo:图像:" + Integer.toString(mWidth) + "x" + Integer.toString(bmpH) + " FPS:" + Integer.toString(fps);
                    //s = s +  " " + Integer.toString(mWidth) + "x" + Integer.toString(mHeight);
                    //Log.e(TAG, s);
                    if (x_Refresh){
                        x_Refresh = false;//收到数据,刷新画面
                        updatepaint();
                    }
                    // 再次发出msg，循环更新
                    mHandler.sendEmptyMessageDelayed(0, 100);
                    break;
                case 1:
                    // 直接移除，定时器停止
                    mHandler.removeMessages(0);
                    break;
                default:
                    break;
            }
        };
    };
    //清空画布
    public void updateclear()
    {
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCanvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }
    //绘制图像
    private void updatepaint()
    {
        updateclear();//清空画布
        mPaint.setStrokeWidth(1);
        mPaint.setColor(Color.YELLOW);
        mPaint.setStyle(Paint.Style.STROKE);//空心

        //摄像画面位置
        mCanvas.drawRect( OffsetX, OffsetY,OffsetX + mWidth *scaleX, OffsetY + mHeight *scaleY -1, mPaint);
        mPaint.setTextAlign(Paint.Align.LEFT);//对齐方式
        mPaint.setStyle(Paint.Style.FILL);//实心
        String xtext="图像:" + Integer.toString(mWidth) + "x" + Integer.toString(mHeight);
        mCanvas.drawText(xtext, 10, fontsize, mPaint);
        xtext="格宽:" + Integer.toString(xSubgrid);
        mCanvas.drawText(xtext, 10, (fontsize+4) *2, mPaint);
        xtext="格角度:" + Integer.toString(x_angle *90);
        mCanvas.drawText(xtext, 10, (fontsize+4) *3, mPaint);
        xtext="位置:" + Integer.toString(winPlace);
        mCanvas.drawText(xtext, 10, (fontsize+4) *4, mPaint);
        xtext="统计:" + Integer.toString(winCount);
        mCanvas.drawText(xtext, 10, (fontsize+4) *5, mPaint);
        xtext="置信度:" + Integer.toString(winConf);
        mCanvas.drawText(xtext, 10, (fontsize+4) *6, mPaint);
        xtext="质疑度:" + Integer.toString(winErr);
        mCanvas.drawText(xtext, 10, (fontsize+4) *7, mPaint);
        //xtext="图形:" + Integer.toString(winShape);//形状 0-不知道 1-圆，2-三角形，3-矩形
        switch(winShape)
        {
            case 1 :
                xtext="图形:圆形";
                break;
            case 2 :
                xtext="图形:三角";
                break;
            case 3 :
                xtext="图形:矩形";
                break;
            default :
                xtext="图形:未知";
                break;
        }
        mCanvas.drawText(xtext, 10, (fontsize+4) *8, mPaint);
        //xtext="颜色:" + Integer.toString(winColor);//颜色  0-红 1-绿 2-黑 3-黄
        switch(winColor)
        {
            case 0 :
                xtext="颜色:红";
                break;
            case 1:
                xtext="颜色:绿";
                break;
            case 2 :
                xtext="颜色:黑";
                break;
            case 3 :
                xtext="颜色:黄";
                break;
            default :
                xtext="颜色:未知";
                break;
        }
        mCanvas.drawText(xtext, 10, (fontsize+4) *9, mPaint);
        String fps = nf.format(fpsMaker.getNowFPS());
        xtext="FPS:" + fps;//Double.toString(fpsMaker.getNowFPS());//double转化为百分数
        mCanvas.drawText(xtext, 10, (fontsize+4) *10, mPaint);
        //检测的底板四边形
        if (isBoard){
            isBoard = false;
            saveBoardGrid(); //保存球盘位置数据
            int l1 = (int)getSpacePointToPoint(xBoard[0].x, xBoard[0].y, xlastBd[0].x, xlastBd[0].y);//点到点的距离
            int l2 = (int)getSpacePointToPoint(xBoard[1].x, xBoard[1].y, xlastBd[1].x, xlastBd[1].y);
            int l3 = (int)getSpacePointToPoint(xBoard[2].x, xBoard[2].y, xlastBd[2].x, xlastBd[2].y);
            int l4 = (int)getSpacePointToPoint(xBoard[3].x, xBoard[3].y, xlastBd[3].x, xlastBd[3].y);

            //Log.i(TAG, "坐标位移:" + String.valueOf(l1) + String.valueOf("," + l2) + String.valueOf("," + l3) + String.valueOf("," + l4));
            //底板位置变动
            if (l1>10 || l2>10 || l3>10 || l4>10){
                for(int i=0; i<4; i++){
                    xlastBd[i] = new Point(xBoard[i].x,xBoard[i].y);
                }
                for(int i=0; i<36; i++){
                    intGrid[i] =0;//新坐标，识别结果清除。
                }
                winCount = 0;//识别次数
                winShape = 0;//形状
                winColor = 0;//颜色
                winPlace = 0;//位置
                winConf = 0;//置信度
                winErr = 0;//未识别统计
                Log.i(TAG, "新坐标");
            }
        }
        if (check1){
            drawBoardlocation(mCanvas, mPaint);//显示板的四个坐标点数字
        }
        if (check4){
            drawBalllocation(mCanvas, mPaint);//显示球位置
        }

        //结束画图
        mCanvas.drawBitmap(mBitmap,0,0,null);//将画好的bitmap画出来,这一步必不可少
        iv_canvas.setImageBitmap(mBitmap);
    }
    private void drawBoardlocation(Canvas canvas, Paint paint)//显示板的四个坐标点数字
    {
        if (xBoard.length >= 4) {
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL);//实心
            paint.setColor(Color.YELLOW);
            float x1 = (float) xBoard[0].x *scaleX +OffsetX;
            float y1 = (float) xBoard[0].y *scaleX;
            float x2 = (float) xBoard[1].x *scaleX +OffsetX;
            float y2 = (float) xBoard[1].y *scaleX;
            float x3 = (float) xBoard[2].x *scaleX +OffsetX;
            float y3 = (float) xBoard[2].y *scaleX;
            float x4 = (float) xBoard[3].x *scaleX +OffsetX;
            float y4 = (float) xBoard[3].y *scaleX;
            canvas.drawCircle(x1,y1,20, paint);
            canvas.drawCircle(x2,y2,20, paint);
            canvas.drawCircle(x3,y3,20, paint);
            canvas.drawCircle(x4,y4,20, paint);
            mPaint.setStyle(Paint.Style.STROKE);//空心
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(2);
            canvas.drawCircle(x1,y1,20, paint);
            canvas.drawCircle(x2,y2,20, paint);
            canvas.drawCircle(x3,y3,20, paint);
            canvas.drawCircle(x4,y4,20, paint);

            paint.setStyle(Paint.Style.FILL);//实心
            paint.setColor(Color.BLACK);
            paint.setTextAlign(Paint.Align.CENTER);//对齐方式
            canvas.drawText("1", x1, y1 + (float) fontsize/3, paint);
            canvas.drawText("2", x2, y2 + (float) fontsize/3, paint);
            canvas.drawText("3", x3, y3 + (float) fontsize/3, paint);
            canvas.drawText("4", x4, y4 + (float) fontsize/3, paint);
        }
    }
    //显示球位置
    private void drawBalllocation(Canvas canvas, Paint paint) {
        //ArrayList<YoloChild> sListBox = yolo.getBoundingBox();
        if (sListBox.size()<1){
            winErr ++;//未识别统计
            winErr = winErr  % 100;//未识别统计
            if (winErr == 8){//未识别统计
                if (winCount >0 && winCount <8){
                    if (winConf >50){
                        //此处清除
                        winCount = 0;//识别次数
                        winPlace = 0;//位置
                        winConf = 0;//置信度
                        winErr = 0;//未识别统计
                        //音频播放
                        try
                        {
                            playAudio();
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }

            return;
        }
        winErr =0;//未识别统计
        paint.setStrokeWidth(3);//线宽
        //遍历
        //System.out.println("置信度个数" + sListBox.size());
        float x1 = 0;//寻找置信度最高的位置
        float y1 = 0;
        int zxd =0;//置信度
        for(int i=0;i<sListBox.size();i++) {
            //Log.i(TAG, "置信度:" + sListBox.get(i).getText() + "," + sListBox.get(i).getConf());
            paint.setStyle(Paint.Style.STROKE);//空心
            Rect Box = sListBox.get(i).getRect();
            int conf = sListBox.get(i).getConf();
            if (conf >80){ paint.setColor(Color.GREEN); }else{
                if (conf >55){ paint.setColor(Color.WHITE); }else{
                    paint.setColor(Color.RED);
                }
            }
            //canvas.drawRect(Box.x, Box.y,Box.x + Box.width, Box.y + Box.height,paint );
            canvas.drawRect(Box.x *scaleX + OffsetX, Box.y *scaleY,Box.x *scaleX + Box.width *scaleX + OffsetX, Box.y *scaleY + Box.height *scaleY,paint );

            if (conf >zxd){
                //球位置，中心点,需要按屏幕比例缩放的位置
                x1 = Box.x *scaleX +  + Box.width *scaleX /2 + OffsetX;
                y1 = Box.y *scaleY + Box.height *scaleY /2;
                zxd = conf;//寻找置信度最高的位置
            }
            paint.setStyle(Paint.Style.FILL);//实心
            String xtext = sListBox.get(i).getText()  + " " + Integer.toString(sListBox.get(i).getConf());
            canvas.drawText(xtext, Box.x *scaleX + OffsetX, Box.y *scaleY -4, paint);
        }
        if (zxd > 30){//球位置
            //  36个格子的位置
            int weizhi =-1;//找出位置
            int weijuli =1000;//找出最近距离
            for(int i=0; i<36; i++){
                int l1 = (int)getSpacePointToPoint(x1, y1, xGrid[i].x *scaleX + OffsetX, xGrid[i].y *scaleY);//点到点的距离
                if (l1 <weijuli){
                    weijuli =l1;
                    weizhi = i;//找出位置
                }
                //canvas.drawCircle((float) xGrid[i].x *scaleX + OffsetX,(float) xGrid[i].y *scaleY,20, paint);//需要按屏幕比例缩放的位置
            }
            if (weizhi >=0){//识别结果
                float x = (int)xGrid[weizhi].x *scaleX + OffsetX;//需要按屏幕比例缩放的位置
                float y = (int)xGrid[weizhi].y *scaleY;
                //canvas.drawCircle(x ,y,20, paint);
                int tmoShape = iGrid[x_angle][weizhi];//形状  1-圆，2-三角形，3-矩形
                int tmpColor = iColor[x_angle][weizhi];//颜色  0-红 1-绿 2-黑 3-黄
                if (tmoShape != winShape || tmpColor != winColor || weizhi != winPlace){//winPlace = 0;//识别位置
                    winCount = 0;//识别次数
                    winShape = tmoShape;//形状
                    winColor = tmpColor;//颜色
                    winPlace = weizhi;//位置
                    winConf = zxd;//置信度
                }
                winCount ++;
                winCount = winCount % 100;
                if (winCount ==8){
                    //音频播放
                    try
                    {
                        playAudio();
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                //String xtext = Integer.toString(winShape) + Integer.toString(winColor);
                //canvas.drawText(xtext, x, y, paint);
            }
        }
    }
    //音频播放
    private void playAudio() throws Exception
    {
        //mediaPlayer=MediaPlayer.create(this,R.raw.b3);
        //mediaPlayer.start();
        switch(winColor)//颜色  0-红 1-绿 2-黑 3-黄
        {
            case 0 :
                switch(winShape)//形状  1-圆，2-三角形，3-矩形
                {
                    case 1 :
                        mediaPlayer=MediaPlayer.create(this,R.raw.rc);
                        mediaPlayer.start();
                        break;
                    case 2 :
                        mediaPlayer=MediaPlayer.create(this,R.raw.r3);
                        mediaPlayer.start();
                        break;
                    case 3 :
                        mediaPlayer=MediaPlayer.create(this,R.raw.r4);
                        mediaPlayer.start();
                        break;
                    default :
                        break;
                }
                break;
            case 1:
                switch(winShape)
                {
                    case 1 :
                        mediaPlayer=MediaPlayer.create(this,R.raw.gc);
                        mediaPlayer.start();
                        break;
                    case 2 :
                        mediaPlayer=MediaPlayer.create(this,R.raw.g3);
                        mediaPlayer.start();
                        break;
                    case 3 :
                        mediaPlayer=MediaPlayer.create(this,R.raw.g4);
                        mediaPlayer.start();
                        break;
                    default :
                        break;
                }
                break;
            case 2 :
                switch(winShape)
                {
                    case 1 :
                        mediaPlayer=MediaPlayer.create(this,R.raw.bc);
                        mediaPlayer.start();
                        break;
                    case 2 :
                        mediaPlayer=MediaPlayer.create(this,R.raw.b3);
                        mediaPlayer.start();
                        break;
                    case 3 :
                        mediaPlayer=MediaPlayer.create(this,R.raw.b4);
                        mediaPlayer.start();
                        break;
                    default :
                        break;
                }
                break;
            case 3 :
                switch(winShape)
                {
                    case 1 :
                        mediaPlayer=MediaPlayer.create(this,R.raw.yc);
                        mediaPlayer.start();
                        break;
                    case 2 :
                        mediaPlayer=MediaPlayer.create(this,R.raw.y3);
                        mediaPlayer.start();
                        break;
                    case 3 :
                        mediaPlayer=MediaPlayer.create(this,R.raw.y4);
                        mediaPlayer.start();
                        break;
                    default :
                        break;
                }
                break;
            default :
                break;
        }
    }
    // ==================================================================================
    //在外置 sdCard 创建文件夹
    public static String getCocos2dxWritablePath(Context context){
        String directoryPath ="";
        String cachePath = "";
        boolean useExternalStorage = false;//判断SD卡是否可用
        if (context.getExternalCacheDir() != null) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                if (Environment.getExternalStorageDirectory().getFreeSpace() > 0) {
                    useExternalStorage = true;
                    //Log.e(TAG, "info tflite SD卡可用");
                }
            }
        }
        if (useExternalStorage) {
            //Log.e(Cocos2dxHelper.TAG, "dir Path 原来存储路径:" + sActivity.getCacheDir().getAbsolutePath());
            directoryPath =  context.getExternalCacheDir().getAbsolutePath();
        } else {
            directoryPath =  context.getCacheDir().getAbsolutePath();
        }
        cachePath = Environment.getExternalStorageDirectory().toString() + "/" + context.getPackageName();
        File file = new File(cachePath);
        // 文件夹不存在
        if (!file.exists()) {
            // 创建文件夹
            boolean isMadirs;
            isMadirs = file.mkdirs();
            //Log.e(TAG, "info tflite 创建文件夹:" + cachePath);
        }
        //Log.e(Cocos2dxHelper.TAG, "dir Path 现在使用路径:" + directoryPath);
        Log.e(TAG, "info tflite 现在使用路径:" + cachePath);
        return cachePath + "/";
    }
    //拷贝文件到sd卡
    private void copyBigDataToSD(Context context,String strOutFileName) throws IOException {
        //get root dir
        String cachePath = Environment.getExternalStorageDirectory().toString() + "/" + context.getPackageName() + "/";
        Log.i(TAG, "info get root dir " + cachePath);
        File file = new File(cachePath);
        if (!file.exists()) {
            file.mkdir();
        }

        String tmpFile = cachePath + strOutFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
            Log.i(TAG, "info file exists " + strOutFileName);
            return;
        }
        InputStream myInput;
        java.io.OutputStream myOutput = new FileOutputStream(cachePath + strOutFileName);
        myInput = this.getAssets().open(strOutFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
        Log.i(TAG, "info end copy file " + strOutFileName);
    }
    // ==================================================================================
}