package com.yuan.device.camera.camera1;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 * 实时预览帧 setPreviewCallback
 * 相机实时预览操作类
 *
 * @author yuanye
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {
    private static final String TAG = "CameraPreview";
    private Camera mCamera;
    //设置屏幕方向
    private int orientation;
    //是否是在预览状态
    private boolean mPreviewing = true;
    private boolean mSurfaceCreated = false;
    private CameraConfigManager mCameraConfigurationManager;
    private Context context;
    //数据回调
    private CameraCallBack cameraCallBack;
    private Delegate mDelegate;
    //默认预览分辨率的宽高
    private int reqPrevW = 1024, reqPrevH = 1920;
    //摄像头id，根据ID切换摄像头
    private int caremaId;

    public CameraPreview(Context context) {
        super(context);
        this.context = context;
        initCamera();
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initCamera();
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initCamera();

    }

    /**
     * 初始化相机参数
     */
    private void initCamera() {
        caremaId = Camera.CameraInfo.CAMERA_FACING_BACK;
        orientation = context.getResources().getConfiguration().orientation;
    }

    /**
     * 设置相机
     *
     * @param camera
     */
    private void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            mCameraConfigurationManager = new CameraConfigManager(getContext());
            getHolder().addCallback(this);
            if (mPreviewing) {
                requestLayout();
            } else {
                showCameraPreview();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        stopCameraPreview();
        showCameraPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceCreated = false;
        stopCameraPreview();
    }

    //显示预览
    public void showCameraPreview() {
        if (mCamera != null) {
            try {
                mPreviewing = true;
                mCamera.setPreviewDisplay(getHolder());
                mCameraConfigurationManager.setCameraParametersForPreviewCallBack(mCamera, caremaId, reqPrevW,
                        reqPrevH);
                mCamera.startPreview();
                mCamera.setPreviewCallback(CameraPreview.this);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            Camera.Parameters mParameters = mCamera.getParameters();
            List<Size> pictureSizeList = mParameters.getSupportedPreviewSizes();
            for (int i = 0; i < pictureSizeList.size(); i++) {
                Log.i(TAG, "本设备支持分辨率：h--" + pictureSizeList.get(i).height + "-----w" + pictureSizeList.get(i).width);
            }
        }
    }

    //停止预览
    public void stopCameraPreview() {
        if (mCamera != null) {
            try {
                mPreviewing = false;
                mCamera.cancelAutoFocus();
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    /**
     * 相机状态回调
     *
     * @param mDelegate
     */
    public void setDelegate(Delegate mDelegate) {
        this.mDelegate = mDelegate;
    }

    /**
     * 打开摄像头开始预览，但是并未开始识别
     */
    public void cwStartCamera() {
        if (mCamera != null) {
            return;
        }
        try {
            mCamera = Camera.open(caremaId);
        } catch (Exception e) {
            if (mDelegate != null) {
                mDelegate.onOpenCameraError();
            }
        }
        setCamera(mCamera);
    }

    /**
     * 关闭摄像头预览，并且隐藏扫描框
     */
    public void cwStopCamera() {
        if (mCamera != null) {
            stopCameraPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void setCameraCallBack(CameraCallBack cameraCallBack) {
        this.cameraCallBack = cameraCallBack;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //这里的data数据格式为NV21格式
        cameraCallBack.getData(data, caremaId, reqPrevW, reqPrevH);
    }

    /**
     **********************************相机辅助功能***********************************************************
     */

    /**
     * 切换前后摄像头
     */
    public void switchCarema() {
        cwStopCamera();
        if (caremaId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            caremaId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            caremaId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        cwStartCamera();
    }

    /**
     * 获取相机预览尺寸
     *
     * @return
     */
    public Size getPreviewSize() {
        Camera.Parameters parameters = mCamera.getParameters();
        return parameters.getPreviewSize();
    }

    /**
     * 关闭闪光灯
     */
    public void closeFlashlight() {
        if (flashLightAvaliable()) {
            mCameraConfigurationManager.closeFlashlight(mCamera);
        }
    }

    /**
     * 开启闪光灯
     */
    public void openFlashlight() {
        if (flashLightAvaliable()) {
            mCameraConfigurationManager.openFlashlight(mCamera);
        }
    }

    /**
     * 检测闪光灯是否可用
     *
     * @return
     */
    private boolean flashLightAvaliable() {
        return mCamera != null && mPreviewing && mSurfaceCreated
                && getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }
}