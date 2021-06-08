package com.caiice;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.abs;

/**
 * Created by Administrator on 2016/12/7.
 */
public class CannyUtils {

    private static double m_cannyLowTh;  /* !< the lower threshold for Canny. */
    private static double m_cannyHighTh; /* !< the higher threshold for Canyy. */

    public void CannyUtils() {

    }


    public double getM_cannyLowTh () {
        return m_cannyLowTh;
    }


    public double getM_cannyHighTh() {
        return m_cannyHighTh;
    }


    /**
     * Find thresholds for Canny detector.
     * @param src input image.
     * @param aperture_size the window size for Canny detector.
     * @param PercentOfPixelsNotEdges the precision of pixels which not belong to edge.
     */
    public void FindAdaptiveThreshold(Mat src, int aperture_size, double PercentOfPixelsNotEdges)
    {
        Mat dx = new Mat(src.rows(), src.cols(), CvType.CV_16SC1);
        Mat dy = new Mat(src.rows(), src.cols(), CvType.CV_16SC1);
        Imgproc.Sobel(src, dx, CvType.CV_16S, 1, 0, aperture_size, 1, 0, Core.BORDER_DEFAULT);
        Imgproc.Sobel(src, dy, CvType.CV_16S, 0, 1, aperture_size, 1, 0, Core.BORDER_DEFAULT);
        _FindApdaptiveThreshold(dx, dy, PercentOfPixelsNotEdges);
    }


    /**
     *  Find thresholds for Canny detector (core function).
     * @param dx gradient of x orientation.
     * @param dy gradient of y orientation.
     * @param PercentOfPixelsNotEdges the precision of pixels which not belong to edge.
     */
    private static void _FindApdaptiveThreshold(Mat dx, Mat dy, double PercentOfPixelsNotEdges)
    {
        int i, j;
        Size size = dx.size();
        Mat imge = Mat.zeros(size, CvType.CV_32FC1);
        // Compute the strong of edge and store the result in image
        double maxv = 0.0, data;
        for (i = 0; i < size.height; i++) {
            for (j = 0; j < size.width; j++) {
                data = abs(dx.get(i, j)[0]) + abs(dy.get(i, j)[0]);
                imge.put(i, j, data);
                maxv = maxv < data ? data : maxv;
            }
        }
        if (0.0 == maxv) {
            m_cannyLowTh = 0.0;
            m_cannyHighTh = 0.0;
            return;
        }

        // Compute histogram
        int histSize = 256;
        histSize = histSize > (int)maxv ? (int)maxv : histSize;
        MatOfInt hist_size = new MatOfInt(histSize);
        MatOfFloat ranges = new MatOfFloat(0, (float) maxv);
        MatOfInt channels = new MatOfInt(0);
        // Compute hist
        Mat hist = new Mat();
        List<Mat> images = new ArrayList<>();
        images.add(imge);
        Imgproc.calcHist(images.subList(0, 1), channels, new Mat(), hist, hist_size, ranges, false);

        double sum = 0.0;
        int icount = hist.rows();
        double total = size.height * size.width * PercentOfPixelsNotEdges;
        for (i = 0; i < icount; i++) {
            sum += hist.get(i, 0)[0];
            if (sum > total) {
                break;
            }
        }
        // Compute high and low threshold of Canny
        m_cannyLowTh = (i + 1) * maxv / histSize;
        if(0.0 == m_cannyLowTh) {
            m_cannyHighTh = 0.0;
        } else {
            m_cannyHighTh = 2.5 * m_cannyLowTh; // Canny 推荐的 高:低 阈值比在 2:1 到3:1之间。2~3 --> 2.5
            if (m_cannyHighTh > 255.0) {
                m_cannyHighTh = 255.0;
            }
        }
    }
}