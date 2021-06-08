package com.caiice;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Yolo {

    private static final String TAG = Yolo.class.getSimpleName();

    private Context context;

    private Net net;

    private String datPath;//使用文件路径

    private List<String> classNames;

    private Size inputImageSize;

    private float confidenceThreshold;

    private float nonMaxSupressThreshold;

    public static ArrayList<YoloChild> BoundBox  = new ArrayList<>();//初始化List对象,作为interface意味着无法实例化 实际上实例化一个实现类。识别结果

    public Yolo(Context context, int width, int height,String Filepath, String classesFilename, String modelArchitectureFilename, String modelWeightsFilename, float confidenceThreshold, float nonMaxSupressThreshold) {
        this.context = context;

        this.datPath = Filepath;//使用文件路径

        this.inputImageSize = new Size(width, height);

        this.classNames = loadClassesNames(classesFilename);

        loadNet(modelArchitectureFilename, modelWeightsFilename);

        this.confidenceThreshold = confidenceThreshold;

        this.nonMaxSupressThreshold = nonMaxSupressThreshold;
    }

    // Upload file to storage and return a path.
    // 如果 sdcard 卡文件不存在，则读取 assets文件夹
    private String getAssetPath(String file) {
        AssetManager assetManager = context.getAssets();

        BufferedInputStream inputStream = null;
        try {
            // Read data from assets.
            if (TextUtils.isEmpty(datPath)){//判断字符串是否为null或者""
                inputStream = new BufferedInputStream(assetManager.open(file));//读取assets文件夹
                Log.i(TAG, "读取 assets 文件:" + file);
            }else{
                if (fileIsExists(datPath + "/" + file))//判断文件是否存在
                {
                    inputStream = new BufferedInputStream(new FileInputStream(datPath + "/" + file));//读取sdcard文件夹
                    Log.i(TAG, "读取文件:" + datPath + "/" + file);
                }else{
                    inputStream = new BufferedInputStream(assetManager.open(file));//读取assets文件夹
                    Log.i(TAG, "读取 assets 文件:" + file);
                }
            }
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();

            // Create copy file in storage.
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            // Return a path to file which may be read in common way.
            return outFile.getAbsolutePath();
        } catch (IOException ex) {
            Log.i(TAG, "读取文件错误" + file);
            return "";
        }
    }

    private List<String> getOutputNames(Net net) {
        List<String> names = new ArrayList<>();
        List<Integer> outLayers = net.getUnconnectedOutLayers().toList();
        List<String> layersNames = net.getLayerNames();
        //展开并从加载的YOLO模型中创建R-CNN层//
        //outLayers.forEach((item) -> names.add(layersNames.get(item - 1)));//方法在API24之前会崩溃
        //解决办法是将foreach改为for
        for(Integer item : outLayers){
            names.add(layersNames.get(item - 1));
            //Log.d("info outLayers-查看 " , item + "");//输出数值怎么写的
        }
        //for(String item : layersNames){
        //    Log.d("info layersNames 查看 " , item);
        //}
        //for(String item : names){
        //    Log.d("info names 查看 " , item);
        //}
        //for (int i = 0; i < names.size(); i++) {
        //    Log.d("info names 查看:" , names.get(i));
        //}
        return names;
    }

    private List<String> loadClassesNames(String classesFilename) {
        String classesFilenamePath = getAssetPath(classesFilename);

        File file = new File(classesFilenamePath);
        List<String> classes = new ArrayList<>();
        try {
            Scanner sc = new Scanner(file);
            while(sc.hasNextLine()) {
                classes.add(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, classesFilename+" file not found!");
        }

        return classes;
    }

    private void loadNet(String modelArchitectureFilename, String modelWeightsFilename) {
        Log.i(TAG, "Loading YOLO Net...");

        String modelArchitecture = getAssetPath( modelArchitectureFilename);
        String modelWeights = getAssetPath(modelWeightsFilename);

        net = Dnn.readNetFromDarknet(modelArchitecture, modelWeights);
    }

    private void nonMaxSupression(Mat inputImage, List<Mat> netOutputs) {
        List<Integer> clsIds = new ArrayList<>();
        List<Float> confs = new ArrayList<>();
        List<Rect> rects = new ArrayList<>();
        for (int i = 0; i < netOutputs.size(); ++i)
        {
            // each row is a candidate detection, the 1st 4 numbers are
            // [center_x, center_y, width, height], followed by (N-4) class probabilities
            Mat level = netOutputs.get(i);
            for (int j = 0; j < level.rows(); ++j)
            {
                Mat row = level.row(j);
                Mat scores = row.colRange(5, level.cols());
                Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
                float confidence = (float)mm.maxVal;
                Point classIdPoint = mm.maxLoc;
                if (confidence > confidenceThreshold)
                {
                    //Log.i(TAG, "Found one object of class id "+classIdPoint.x+" with a confidence of "+confidence*100+"%.");
                    int centerX = (int)(row.get(0,0)[0] * inputImage.cols());
                    int centerY = (int)(row.get(0,1)[0] * inputImage.rows());
                    int width   = (int)(row.get(0,2)[0] * inputImage.cols());
                    int height  = (int)(row.get(0,3)[0] * inputImage.rows());
                    int left    = centerX - width  / 2;
                    int top     = centerY - height / 2;

                    clsIds.add((int)classIdPoint.x);
                    confs.add((float)confidence);
                    rects.add(new Rect(left, top, width, height));
                }
            }
        }
        BoundBox.clear();//清除返回数据
        if(confs.isEmpty())
            return;

        // Apply non-maximum suppression procedure.
        MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));
        Rect[] boxesArray = rects.toArray(new Rect[0]);
        MatOfRect boxes = new MatOfRect(boxesArray);
        MatOfInt indices = new MatOfInt();
        Dnn.NMSBoxes(boxes, confidences, confidenceThreshold, nonMaxSupressThreshold, indices);

        // Draw bounding boxes   绘制边界框

        int [] ind = indices.toArray();
        for (int i = 0; i < ind.length; ++i)
        {
            int idx = ind[i];
            float conf = confs.get(idx);
            Rect box = boxesArray[idx];
            int classId = clsIds.get(idx);
            //名称，置信度
            //String label = String.format("%s [%.0f%%]", classNames.get(classId), 100 * conf);
            String label = String.format("%s", classNames.get(classId));
            int confidence =(int)(100 * conf);
            //Log.i(TAG, "置信度，名称..." + 100 * conf + classNames.get(classId));
            //drawBoundingBox(inputImage, label,confidence, box, 2);//画边框
            makeBoundingBox(label, confidence, box);//生成结果数据
        }
    }
    //生成结果数据
    private void makeBoundingBox(String labela, int confidence, Rect boundingBox)
    {
        BoundBox.add(new YoloChild(labela,confidence,boundingBox));//名称
    }
    public ArrayList<YoloChild> getBoundingBox()
    {
        return BoundBox;
    }
    //画边框
    public void drawBoundingBox(Mat inputImage, String label, int confidence, Rect boundingBox, int thickness) {
        if(confidence > 80) {
            Scalar color = new Scalar(0, 255, 0);
            Imgproc.rectangle(inputImage, boundingBox.tl(), boundingBox.br(), color, thickness);
            Imgproc.putText(inputImage, label + ":" + confidence, new Point(boundingBox.x, boundingBox.y), Core.FONT_HERSHEY_PLAIN, 2.0, color, thickness);
        }
        else if (confidence > 55 && confidence < 80) {
            Scalar color = new Scalar(255, 255, 0);
            Imgproc.rectangle(inputImage, boundingBox.tl(), boundingBox.br(), color, thickness);
            Imgproc.putText(inputImage,label + ":" + confidence, new Point(boundingBox.x, boundingBox.y), Core.FONT_HERSHEY_PLAIN, 2.0, color, thickness);
        }
        else {
            Scalar color = new Scalar(255, 0, 0);
            Imgproc.rectangle(inputImage, boundingBox.tl(), boundingBox.br(), color, thickness);
            Imgproc.putText(inputImage, label + ":" + confidence, new Point(boundingBox.x, boundingBox.y), Core.FONT_HERSHEY_PLAIN, 2.0, color, thickness);
        }
    }

    public void detect(Mat inputImage) {
        Mat blob = Dnn.blobFromImage(inputImage, 0.00392, inputImageSize, new Scalar(0, 0, 0), false, false);

        net.setInput(blob);

        List<Mat> result = new ArrayList<>();

        List<String> outBlobNames = getOutputNames(net);

        net.forward(result, outBlobNames);

        nonMaxSupression(inputImage, result);
    }
    //判断文件是否存在
    private boolean fileIsExists(String strFile)//判断文件是否存在
    {
        try
        {
            File f=new File(strFile);
            if(!f.exists())
            {
                return false;
            }

        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }
}
