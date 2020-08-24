package cc.makepower.cc_door_face;

import java.util.List;

import cc.makepower.cc_door_face.base.BaseView;
import cc.makepower.cc_door_face.bean.BindDeviceResult;

interface MainContract {
    interface View extends BaseView {

        void initFaceSdkCallBack(boolean result);
        void searchFaceFindUserCallBack(boolean result, float similar, String userName);


        void downLoadPermissionCallBack(boolean result, int surplusNum);

        void buildRemotedoorCalBack(boolean result);

        void fetchFaceListCallBack(boolean result, List<String> facePaths);

        void pushOpenLogCallBack(boolean result);
    }
}