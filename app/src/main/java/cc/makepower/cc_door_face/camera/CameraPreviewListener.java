package cc.makepower.cc_door_face.camera;

import android.hardware.Camera;

/**
 * author : atex
 * e-mail : xxx@xx
 * time   : 2018/11/13
 * desc   :
 * version: 1.0
 */

public interface CameraPreviewListener {


    /**
     * 打开相机
     * @param camera
     * @param cameraId
     * @param displayOrientation
     */
    void onCameraOpened(Camera camera, int cameraId, int displayOrientation);

    /**
     * 预览数据回调
     * @param datas
     * @param camera
     */
    void onPreview(byte[] datas, Camera camera);


    /**
     * 相机关闭
     */
    void onCameraClosed();

    /**
     * 相机出错
     * @param e
     */
    void onCameraError(Exception e);


    /**
     * 相机属性生改变后
     * @param CaeraId
     * @param disPlayOrientation
     */
    void onCameraConfiguraChanged(int CaeraId, int disPlayOrientation);
}
