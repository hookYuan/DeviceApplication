package com.yuan.device.camera.camera1;

public interface Delegate {

	/**
	 * 处理打开相机出错
	 */
	void onOpenCameraError();

	void onFocus(float x, float y);

	void onFocused();
}
