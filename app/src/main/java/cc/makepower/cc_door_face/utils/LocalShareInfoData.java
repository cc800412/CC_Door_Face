package cc.makepower.cc_door_face.utils;

import android.content.Context;

import cc.makepower.cc_door_face.R;

/**
 * author : atex
 * e-mail : xxx@xx
 * time   : 2020/02/15
 * desc   :
 * version: 1.0
 */

public class LocalShareInfoData {
    private static LocalShareInfoData siteInfoData;

    public static LocalShareInfoData getInstance() {
        if (siteInfoData==null){
            siteInfoData=new LocalShareInfoData();
        }
        return siteInfoData;
    }



    public String getBindDeviceId(Context context){
        return (String) SPUtils.get(context,"deviceId", context.getResources().getString(R.string.deviceId));
    }
    public String getBindDeviceName(Context context){
        return (String) SPUtils.get(context,"deviceName", context.getResources().getString(R.string.deviceName));
    }
    public String getdBindeviceBlueToothName(Context context){
        return (String) SPUtils.get(context,"deviceBlueToothName", context.getResources().getString(R.string.deviceBlueToothName));
    }



}
