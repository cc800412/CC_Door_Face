package cc.makepower.cc_door_face.bean;

import com.google.gson.annotations.Expose;

/**
 * author : atex
 * e-mail : xxx@xx
 * time   : 2018/12/25
 * desc   :
 * version: 1.0
 */

public class PostResultBean<T> {
    @Expose
    private String message;

    @Expose
    private int status;


    @Expose
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
