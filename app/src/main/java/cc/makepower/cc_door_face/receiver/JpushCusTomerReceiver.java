package cc.makepower.cc_door_face.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import cn.jpush.android.api.JPushInterface;

/**
 * author : atex
 * e-mail : xxx@xx
 * time   : 2018/12/19
 * desc   :
 * version: 1.0
 */

public class JpushCusTomerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            Log.d("Jpush", "JPush 用户注册成功");
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())
                || JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.d("Jpush", "接受到推送下来的自定义消息");
            EventBus.getDefault().post(bundle);
//            EventB
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d("Jpush", "用户点击打开了通知");
        } else {
            Log.d("Jpush", "Unhandled intent - " + intent.getAction());
        }
    }
}
