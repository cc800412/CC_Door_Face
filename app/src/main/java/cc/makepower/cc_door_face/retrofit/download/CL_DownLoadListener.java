package cc.makepower.cc_door_face.retrofit.download;

import java.io.File;

/**
 * author : atex
 * e-mail : xxx@xx
 * time   : 2018/04/13
 * desc   :
 * version: 1.0
 */

public interface CL_DownLoadListener {
    void onStart();
    void onChange(int ran);
    void onFail();
    void onFinish(File file);
}
