package cc.makepower.cc_door_face;

import android.app.Application;

import cn.jpush.android.api.JPushInterface;

public class FaceManagerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);//极光推送sdk初始化
        JPushInterface.init(getApplicationContext());
    }
}
