package com.caiice;

import org.opencv.core.Rect;

//保存识别后的结果坐标数据
public class YoloChild {
    // Has two properties one is text and another is image resource id
    private String text;
    private int conf;
    private Rect Box;
    //constructor for creating object with resources
    public YoloChild( String text,int conf,Rect box){
        this.text = text;
        this.conf = conf;
        this.Box = box;
    }
    //to get the text, call it
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    //to get the conf id, call it
    public int getConf() {
        return conf;
    }

    public void setConf(int conf) {
        this.conf = conf;
    }
    //to get the conf id, call it
    public Rect getRect() {
        return Box;
    }

    public void setRect(Rect box) {
        this.Box = box;
    }
}