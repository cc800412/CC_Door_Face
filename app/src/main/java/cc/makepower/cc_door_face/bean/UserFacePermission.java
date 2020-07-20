package cc.makepower.cc_door_face.bean;

import com.google.gson.annotations.Expose;

/**
 * author : atex
 * e-mail : xxx@xx
 * time   : 2018/12/25
 * desc   :
 * version: 1.0
 */

public class UserFacePermission {
    @Expose
    private String imageUrl;
    @Expose
    private String featureUrl;
    @Expose
    private String userId;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFeatureUrl() {
        return featureUrl;
    }

    public void setFeatureUrl(String featureUrl) {
        this.featureUrl = featureUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
