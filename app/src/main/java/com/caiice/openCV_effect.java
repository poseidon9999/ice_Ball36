package com.caiice;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class openCV_effect {

    //图像透视视角转换Perspective warpPerspective
    public static Mat warpPerspective(Mat src,Point pt[],Point retVal,int rot,int usb) {
        //Mat src = currentFrame;
        Random rand;
        rand = new Random();
        try{
            //Mat src = Imgcodecs.imread("tab0.png");
            //Mat src= Utils.loadResource(context, R.id.Viewlogo);
            //读取图像到矩阵中
            if(src.empty()){
                throw new Exception("no file");
            }

            int x0=0;
            int y0=0;
            int x1=src.cols()-1;
            int y1=src.cols()-1;

            List<Point> listSrcs=java.util.Arrays.asList(new Point(x0,y0),new Point(x0,y1),new Point(x1,y1),new Point(x1,y0));
            Mat srcPoints=Converters.vector_Point_to_Mat(listSrcs,CvType.CV_32F);
            //逆时针数据
            //int mx1 = rand.nextInt(100) -50;
            Point [] pos = new Point [4];
            switch (rot) //设置旋转的四个顶点
            {
                case 0: pos[0] = pt[0];pos[1] = pt[1];pos[2] = pt[2];pos[3] = pt[3];break;
                case 1: pos[0] = pt[1];pos[1] = pt[2];pos[2] = pt[3];pos[3] = pt[0];break;
                case 2: pos[0] = pt[2];pos[1] = pt[3];pos[2] = pt[0];pos[3] = pt[1];break;
                case 3: pos[0] = pt[3];pos[1] = pt[0];pos[2] = pt[1];pos[3] = pt[2];break;
                default: pos[0] = pt[0];pos[1] = pt[1];pos[2] = pt[2];pos[3] = pt[3];break;
            }

            List<Point> listDsts=java.util.Arrays.asList(new Point(pos[1].x - retVal.x,pos[1].y - retVal.y),new Point(pos[2].x - retVal.x,pos[2].y - retVal.y),new Point(pos[3].x - retVal.x,pos[3].y - retVal.y),new Point(pos[0].x - retVal.x,pos[0].y - retVal.y));
            Mat dstPoints=Converters.vector_Point_to_Mat(listDsts,CvType.CV_32F);

            Mat perspectiveMmat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);//计算3个二维点对之间的仿射变换矩阵（2行x3列）
            Imgproc.warpPerspective(src, src, perspectiveMmat, src.size(),Imgproc.INTER_LINEAR);//应用仿射变换，可以恢复出原图

            srcPoints.release();//释放
            dstPoints.release();
            perspectiveMmat.release();
            return src;
        }catch(Exception e){
            System.out.println("例外：" + e);
            return src;
        }
    }

    //检测测试
    public static Mat ret_CheckTest(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        Mat input = inputFrame.rgba();
        Mat grayMat = inputFrame.gray();
        Mat thresh = new Mat();
        //Imgproc.GaussianBlur(grayMat, thresh, new Size(3,3), 0);//高斯模糊

        //Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C
        //Imgproc.adaptiveThreshold(blurMat, thresh, 255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,1,11,4);//图像自适应阈值操作

        Imgproc.Canny(grayMat, thresh, 10, 100, 3, false);// Canny边缘检测
        //Imgproc.dilate(thresh, thresh, new Mat(), new Point(-1,-1), 3, 1, new Scalar(1));// 膨胀，连接边缘
        Imgproc.GaussianBlur(thresh, thresh, new Size(5, 3), 2, 2 );//去噪

        List<MatOfPoint> contoursRect = new ArrayList<>();
        Mat hier = new Mat();
        Imgproc.findContours(thresh, contoursRect, hier, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);//轮廓提取
        Imgproc.drawContours(input, contoursRect, -1, new Scalar(0, 0, 255, 255));//查看画出轮廓,函数轮廓绘制

        for (MatOfPoint i : contoursRect) {//循环检测，选取最大的一个四边形
            double area = Math.abs(Imgproc.contourArea(i));//默认计算整个轮廓的面积,返回值可能是负值，需要用fabs()转成正值
            if (area > 100) {
                MatOfPoint2f m = new MatOfPoint2f(i.toArray());
                double peri = Imgproc.arcLength(m, true);//计算图像轮廓的周长
                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(m, approx, 0.02 * peri, true);//用指定的精度逼近多边形曲线,多边形包围轮廓
                if ( approx.total() >4 ) {//边
                    Point[] pt = approx.toArray();
                    int n = pt.length;
                    for(int k=0; k< pt.length; k++){
                        Imgproc.line(input, new Point(pt[k].x, pt[k].y), new Point(pt[(k + 1) % n].x, pt[(k + 1) % n].y), new Scalar(255, 0, 0), 1);
                    }
                }
                if ( approx.total() ==4 ) {//边
                    Point[] pt = approx.toArray();
                    int n = pt.length;
                    for(int k=0; k< pt.length; k++){
                        Imgproc.line(input, new Point(pt[k].x, pt[k].y), new Point(pt[(k + 1) % n].x, pt[(k + 1) % n].y), new Scalar(0, 255, 0), 2);
                    }
                }
            }
        }

        hier.release();
        thresh.release();
        grayMat.release();
        return input;
    }

}
