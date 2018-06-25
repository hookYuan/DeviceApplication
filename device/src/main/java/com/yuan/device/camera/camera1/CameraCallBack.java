package com.yuan.device.camera.camera1;


/**
 * Created by YuanYe on 2018/5/24.
 */
public interface CameraCallBack {

    void getData(byte[] data, int cameraId, int videoWidth, int videoHeight);
}
