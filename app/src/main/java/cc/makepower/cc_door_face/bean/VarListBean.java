package cc.makepower.cc_door_face.bean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * author : atex
 * e-mail : xxx@xx
 * time   : 2018/11/19
 * desc   :
 * version: 1.0
 */

public class VarListBean {
    @Expose
    @SerializedName("varList")
    private List<LockBean> lockBeanList;

    public List<LockBean> getLockBeanList() {
        return lockBeanList;
    }

    public void setLockBeanList(List<LockBean> lockBeanList) {
        this.lockBeanList = lockBeanList;
    }
}
