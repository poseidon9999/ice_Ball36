package com.caiice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.util.Arrays;

import static android.graphics.ImageFormat.YUV_420_888;

//import my.com.toru.firstcamera2opencv.R;
//import my.com.toru.firstcamera2opencv.util.JNIUtil;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = CameraActivity.class.getSimpleName();

    //region textureView
    private TextureView textureView;
    private CFPSMaker fpsMaker;//JAVA游戏开发计算显示FPS

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
    };
    // endregion

    //region CameraDevice
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    //endregion

    //region Capture
    private CameraCaptureSession captureSession;
    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };

    private CameraCaptureSession.StateCallback captureStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            if(cameraDevice == null) return;
            captureSession = session;
            updateCameraPreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
    };

    private CaptureRequest captureRequest;
    private CaptureRequest.Builder captureRequestBuilder;
    //endregion

    //region Image and Size
    private Size imageSize = new Size(640, 480); // 固定640*480演示
    private ImageReader imageReader;
    private ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            if (image == null) {
                return;
            }

            //Image.Plane yPlane = image.getPlanes()[0];
            //Image.Plane uPlane = image.getPlanes()[1];
            //Image.Plane vPlane = image.getPlanes()[2];

            //Log.d(TAG, "Y plane length: " + yPlane.getBuffer().remaining());
            //Log.d(TAG, "U plane length: " + uPlane.getBuffer().remaining());
            //Log.d(TAG, "V plane length: " + vPlane.getBuffer().remaining());

            //Mat mYuv = new Mat(image.getHeight() + image.getHeight() / 2, image.getWidth(), CvType.CV_8UC1);
            //mYuv.put(0, 0, yPlane.getBuffer().array());
            Log.i(TAG, "info 屏幕预览" + image.getWidth() + " " + image.getHeight());
            image.close();
            String fps = Double.toString(fpsMaker.getNowFPS());//double转化为百分数
            Log.i(TAG, "显示FPS:" + fps);
        }
    };
    //endregion

    //region in charge of Background Task
    private Handler backgroundHander;
    private HandlerThread backgroundThread;
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        fpsMaker = new CFPSMaker();//JAVA游戏开发计算显示FPS
        fpsMaker.setNowFPS(System.nanoTime());
        initCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        }
        else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //region Camera Related Work
    private void initCamera() {
        textureView = findViewById(R.id.camera_texture);
        textureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    private void openCamera() {
        if (cameraManager == null) {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        }

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            //imageSize = configurationMap.getOutputSizes(SurfaceTexture.class)[0];

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, cameraStateCallback, null);
            }
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera(){
        if(cameraDevice != null){
            cameraDevice.close();
            cameraDevice = null;
        }
    }
    //endregion

    private Surface surface;

    //region CameraPreview
    private void createCameraPreview(){
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(imageSize.getWidth(), imageSize.getHeight());
        surface  = new Surface(surfaceTexture);

        try {
            //imageReader = ImageReader.newInstance(imageSize.getWidth(), imageSize.getHeight(), YUV_420_888, 1);
            imageReader = ImageReader.newInstance(imageSize.getWidth(), imageSize.getHeight(), YUV_420_888, 1);
            imageReader.setOnImageAvailableListener(imageAvailableListener, backgroundHander);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            captureRequestBuilder.addTarget(imageReader.getSurface());

            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), captureStateCallback, null);
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updateCameraPreview(){
        if(cameraDevice == null) return;
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHander);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }
    //endregion

    //region Background Work
    private void initializeBackgroundThread(){
        backgroundThread = new HandlerThread("Camera Background Thread");
        backgroundThread.start();
        backgroundHander = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread(){
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHander = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // openCV
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
                    //mOpenCvCameraView.enableView();
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
        //mOpenCvCameraView.enableView();
    }
}