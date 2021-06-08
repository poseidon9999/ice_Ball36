package com.caiice;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SdcardService {
    private Context context;
    //SharedPrference
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public SdcardService(Context context) {
        // TODO Auto-generated constructor stub
        this.context = context;
    }

    public SdcardService() {
        // TODO Auto-generated constructor stub
    }

    // 读取文件
    @SuppressWarnings("resource")
    public String getInputstream(String filename) {// 从文件名中获得inputstream
        // ///////////////////////////////////////
        // 注意FileInputStream是从相应连路径的文件中读数据 而不是写 注意注意
        FileInputStream fileInputStream = null;// 获得一个文件输入流对象
        // /////////////////////////////////////////////////////////
        // ByteArrayOutputStream是缓存流 与磁盘无关 不需要关闭
        ByteArrayOutputStream OutputStream = new ByteArrayOutputStream();
        // 获得sdcard文件跟目录路径
        File file = new File(Environment.getExternalStorageDirectory(),
                filename);// 创建一个新的文件
        // ////////////////////////////////////////////////////
        // 是否sdcard卡开启
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            try {
                fileInputStream = new FileInputStream(file);// 找到相应文件的路径的输入流
                int len = 0;
                byte[] data = new byte[1024];
                // ///////////////////////////////////////////////
                // 读取数据到OutputStream中
                while ((len = fileInputStream.read(data)) != -1) {
                    OutputStream.write(data, 0, len);
                }

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    fileInputStream = null;
                }
            }

        }

        return new String(OutputStream.toByteArray());
    }

    /**
     * 我这个函数是写入文件到sdcard中
     *
     * @param Filename
     *            文件名
     * @param content
     *            文件内容
     * @return
     */
    public boolean saveContentTosdCard(String Filename, String content) {
        boolean flag = false;
        // /////////////////////////////////////////////////////////////
        // 注意FileOutputStream是打开特定的文件 在其中<<写>>东西 人不是 顾名思义第去读 也就是参照是软件的内存
        FileOutputStream outputStream = null;// 获得一个文件输出流
        // 获得sdcard的根目录
        // 注意这个file不是文件 而是 一个路径
        File file = new File(Environment.getExternalStorageDirectory(),
                Filename);
        // 判断sdcard是否可用
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            try {
                // ///////////////////////////////////////////////////////
                //
                outputStream = new FileOutputStream(file);//
                // ////////////////////////////////////
                // 把指定的内容写入相应的文件中
                outputStream.write(content.getBytes());// 从edittext中获得我输入的字符
                // 并且放入outputStream中
                flag = true;
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                // ////////////////////////////////
                // 注意要关闭
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

        return flag;
    }
    //判断文件是否存在
    public boolean fileIsExists(String strFile)
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

    //判断存储卡是否可用，建立文件夹
    public String geWritablePath(Context mContext,String dirPath){
        String directoryPath ="";
        String cachePath = "";
        boolean useExternalStorage = false;//判断SD卡是否可用
        if (mContext.getExternalCacheDir() != null) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                if (Environment.getExternalStorageDirectory().getFreeSpace() > 0) {
                    useExternalStorage = true;
                    //Log.e(Cocos2dxHelper.TAG, "dir Path SD卡可用");
                }
            }
        }
        if (useExternalStorage) {
            //Log.e(Cocos2dxHelper.TAG, "dir Path 原来存储路径:" + mContext.getCacheDir().getAbsolutePath());
            directoryPath =  mContext.getExternalCacheDir().getAbsolutePath();
        } else {
            directoryPath =  mContext.getCacheDir().getAbsolutePath();
        }
        cachePath = Environment.getExternalStorageDirectory().toString() + "/" + dirPath;
        File file = new File(cachePath);
        // 文件夹不存在
        if (!file.exists()) {
            // 创建文件夹
            Log.d("this", "info 创建文件夹:" + cachePath);
            file.mkdirs();
        }
        return cachePath;
    }
    /*-------------------------------- SharedPreferences ---------------------------------------------------*/
    //从 SharedPreferences在中读出数据
    public int ReadFromint(Context context,String strkey) {
        preferences = context.getSharedPreferences("MY_PRE_NAME", Context.MODE_PRIVATE);
        // 读取字符串数据
        //String time = preferences.getString("time", null);
        // 读取int类型的数据
        int randNum = preferences.getInt(strkey, 0);
        //String result = time == null ? "您暂时还未写入数据" : "写入时间为："+ time + "\n上次生成的随机数为：" + randNum;
        return randNum;
    }

    //往SharedPreferences中写入数据
    public void WriteToint(Context context,String strkey,int randNum) {
        preferences = context.getSharedPreferences("MY_PRE_NAME", Context.MODE_PRIVATE);
        editor = preferences.edit();
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 " + "hh:mm:ss");
        // 存入当前时间
        //editor.putString("time", sdf.format(new Date()));        // 存入一个随机数
        //editor.putInt(strkey, (int) (Math.random() * 100));
        editor.putInt(strkey, randNum);
        // 提交所有存入的数据
        editor.apply();
    }
    public Boolean ReadFromBoolean(Context context,String strkey) {
        preferences = context.getSharedPreferences("MY_PRE_NAME", Context.MODE_PRIVATE);
        // 读取字符串数据
        //String time = preferences.getString("time", null);
        // 读取int类型的数据
        Boolean randNum = preferences.getBoolean(strkey, false);
        //String result = time == null ? "您暂时还未写入数据" : "写入时间为："+ time + "\n上次生成的随机数为：" + randNum;
        return randNum;
    }

    //往SharedPreferences中写入数据
    public void WriteToBoolean(Context context,String strkey,Boolean randNum) {
        preferences = context.getSharedPreferences("MY_PRE_NAME", Context.MODE_PRIVATE);
        editor = preferences.edit();
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 " + "hh:mm:ss");
        // 存入当前时间
        //editor.putString("time", sdf.format(new Date()));        // 存入一个随机数
        //editor.putInt(strkey, (int) (Math.random() * 100));
        editor.putBoolean(strkey, randNum);
        // 提交所有存入的数据
        editor.apply();
    }
}