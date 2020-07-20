package cc.makepower.cc_door_face.bean;

import com.google.gson.annotations.Expose;

/**
 * author : atex
 * e-mail : xxx@xx
 * time   : 2018/11/19
 * desc   :
 * version: 1.0
 */

public class LockBean {


    /**
     * Name : 北京试点3
     * doorStatus : 1
     * deviceID : 34095
     * deviceStatus : 0
     * Code : 110101010005
     */

    @Expose
    private String Name;
    @Expose
    private int doorStatus;
    @Expose
    private String deviceID;
    @Expose
    private int deviceStatus;
    @Expose
    private String Code;

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public int getDoorStatus() {
        return doorStatus;
    }

    public void setDoorStatus(int doorStatus) {
        this.doorStatus = doorStatus;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public int getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(int deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String Code) {
        this.Code = Code;
    }
}
