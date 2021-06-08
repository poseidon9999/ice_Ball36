package com.caiice;
import java.text.DecimalFormat;
/**
 *JAVA游戏开发计算显示FPS
 * https://blog.csdn.net/taki_dsm/article/details/20369557
 * 简单的使用方法：
 * 首先，初始化一个FPSMaker实例
 * CFPSMaker fpsMaker;
 * fpsMaker = new CFPSMaker();
 *
 * 然后，在需要计算FPS时初始化以下当前时间：
 * fpsMaker.setNowFPS(System.nanoTime());
 *
 * 最后，在游戏循环中计算和显示当前游戏帧速：
 * fpsMaker.makeFPS();
 * GameManager.drawFPS(fpsMaker.getFPS() + " FPS");
 */
public class CFPSMaker
{
    /**
     * 设定动画运行多少帧后统计一次帧数
     */
    public static final int FPS = 8;

    /**
     * 换算为运行周期
     * 单位: ns(纳秒)
     */
    public static final long PERIOD = (long) (1.0 / FPS * 1000000000);
    /**
     * FPS最大间隔时间，换算为1s = 10^9ns
     * 单位: ns
     */
    public static long FPS_MAX_INTERVAL = 1000000000L;

    /**
     * 实际的FPS数值
     */
    private double nowFPS = 0.0;

    /**
     * FPS累计用间距时间
     * in ns
     */
    private long interval = 0L;
    private long time;
    /**
     * 运行桢累计
     */
    private long frameCount = 0;

    /**
     * 格式化小数位数
     */
    private DecimalFormat df = new DecimalFormat("0.0");

    /**
     * 制造FPS数据
     *
     */
    public void makeFPS()
    {
        frameCount++;
        interval += PERIOD;
        //当实际间隔符合时间时。
        if (interval >= FPS_MAX_INTERVAL)
        {
            //nanoTime()返回最准确的可用系统计时器的当前值，以毫微秒为单位
            long timeNow = System.nanoTime();
            // 获得到目前为止的时间距离
            long realTime = timeNow - time; // 单位: ns
            //换算为实际的fps数值
            nowFPS = ((double) frameCount / realTime) * FPS_MAX_INTERVAL;

            //变更数值
            frameCount = 0L;
            interval = 0L;
            time = timeNow;
        }
    }

    public long getFrameCount()
    {
        return frameCount;
    }

    public void setFrameCount(long frameCount)
    {
        this.frameCount = frameCount;
    }

    public long getInterval()
    {
        return interval;
    }

    public void setInterval(long interval)
    {
        this.interval = interval;
    }

    public double getNowFPS()
    {
        return nowFPS;
    }

    public void setNowFPS(double nowFPS)
    {
        this.nowFPS = nowFPS;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time = time;
    }

    public String getFPS()
    {
        return df.format(nowFPS);
    }
}
