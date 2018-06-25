package com.yuan.device.camera.camera1;

/**
 * 调用相机拍照回调
 */
public interface OnCaptureCallback {

    public void onCapture(byte[] jpgdata);
}