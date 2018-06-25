package com.yuan.device.camera.camera2;

/**
 * Created by YuanYe on 2018/5/25.
 */

public interface CameraCallBack {

    void getData(byte[] data, int angle, int videoWidth, int videoHeight);

}
