package com.caiice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d("xxx", "intent.getAction() ______________ " + intent.getAction());
        System.out.println("______________ BootBroadcastReceiver");
            if (intent.getAction().equals(ACTION)) {
        	
        	//1.启动一个Activity
            Intent mainActivityIntent = new Intent(context , MainActivity.class);// 要启动的Activity
            Log.d("xxx","开机自启动一个Activity");
            String action = "android.intent.action.MAIN";
            String category = "android.intent.category.LAUNCHER";
            mainActivityIntent.setAction(action);
            mainActivityIntent.addCategory(category);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);
            
            //2.启动一个Service
            //Intent service = new Intent(context,ServiceCrack.class);// 要启动的Service
            //context.startService(service);
            //Log.d("xxx","开机自启动一个Service");
            
            //3.启动一个app
            //Intent app = context.getPackageManager().getLaunchIntentForPackage("com.google.www");//包名
            //context.startActivity(app);
            
        }
    }
}