package cc.makepower.cc_door_face.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * author : atex
 * e-mail : xxx@xx
 * time   : 2018/12/25
 * desc   :
 * version: 1.0
 */

public class BindDeviceResult {

//    @Expose
//    private String lockId;

//    @Expose
//    @SerializedName("lockData")
//    LockInfoBean lockInfoBean;


    @Expose
    private String updateTime;


    @Expose
    @SerializedName("userList")
    private List<UserFacePermission> userFacePermissions;


//    public LockInfoBean getLockInfoBean() {
//        return lockInfoBean;
//    }
//
//    public void setLockInfoBean(LockInfoBean lockInfoBean) {
//        this.lockInfoBean = lockInfoBean;
//    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public List<UserFacePermission> getUserFacePermissions() {
        return userFacePermissions;
    }

    public void setUserFacePermissions(List<UserFacePermission> userFacePermissions) {
        this.userFacePermissions = userFacePermissions;
    }
}
