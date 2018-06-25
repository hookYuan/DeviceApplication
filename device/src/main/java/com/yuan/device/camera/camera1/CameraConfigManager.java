package com.yuan.device.camera.camera1;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

final class CameraConfigManager {
    private static final int TEN_DESIRED_ZOOM = 27;
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    private static final String TAG = "CameraConfigManager";

    private final Context mContext;
    private Point mScreenResolution;
    private Point cameraResolution;

    Size pictureSize;

    public CameraConfigManager(Context context) {
        mContext = context;
    }

    public void initFromCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        mScreenResolution = new Point(display.getWidth(), display.getHeight());// 屏幕宽度高度
        Point screenResolutionForCamera = new Point();
        screenResolutionForCamera.x = mScreenResolution.x;
        screenResolutionForCamera.y = mScreenResolution.y;

        // preview size is always something like 480*320, other 320*480
        if (mScreenResolution.x < mScreenResolution.y) {
            screenResolutionForCamera.x = mScreenResolution.y;
            screenResolutionForCamera.y = mScreenResolution.x;
        }

        cameraResolution = getCameraResolution(parameters, screenResolutionForCamera);
    }

    public void setDesiredCameraParameters(Camera camera, int caremaId) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
        setZoom(parameters);

        camera.setDisplayOrientation(getDisplayOrientation(caremaId));

        // 设置照片尺寸
        if (this.pictureSize == null) {
            WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            List<Size> pictureSizes = parameters.getSupportedPictureSizes();
            this.setPicutreSize(pictureSizes, display.getWidth(), display.getHeight());
        }
        try {
            parameters.setPictureSize(this.pictureSize.width, this.pictureSize.height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        camera.setParameters(parameters);
    }

    /**
     * setCameraParametersForPreviewCallBack:设置参数(用于实时预览数据回掉). <br/>
     * @param camera
     * @param previewW 建议640
     * @param previewH 建议480
     * @author:284891377 Date: 2016年7月11日 下午2:53:05
     */
    public void setCameraParametersForPreviewCallBack(Camera camera, int caremaId, int previewW, int previewH) {
        Camera.Parameters parameters = camera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains("continuous-video")) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        List<int[]> range = parameters.getSupportedPreviewFpsRange();

        for (int j = 0; j < range.size(); j++) {
            int[] r = range.get(j);
            for (int k = 0; k < r.length; k++) {
                Log.i(TAG,TAG + r[k]);
            }
        }
        Size localSize = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), previewW, previewH);
        if (localSize != null) {
            parameters.setPreviewSize(localSize.width, localSize.height);
        } else {
            Log.i(TAG, "设置预览失败");
        }

        parameters.setPreviewFormat(ImageFormat.NV21);

        setZoom(parameters);

        camera.setDisplayOrientation(getDisplayOrientation(caremaId));

        camera.setParameters(parameters);
    }

    /**
     * getOptimalPreviewSize:获取最接近预览分辨率 <br/>
     *
     * @param localList
     * @param w
     * @param h
     * @return
     * @author:284891377 Date: 2016年7月11日 下午2:51:06
     * @since JDK 1.7
     */
    private Size getOptimalPreviewSize(List<Size> localList, int w, int h) {
        Size optimalSize = null;
        try {
            ArrayList<Size> localArrayList = new ArrayList<Size>();
            Iterator<Size> localIterator = localList.iterator();
            while (localIterator.hasNext()) {
                Size localSize = localIterator.next();
                if (localSize.width > localSize.height) {
                    localArrayList.add(localSize);
                }
            }
            Collections.sort(localArrayList, new PreviewComparator(w, h));
            optimalSize = localArrayList.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return optimalSize;
    }

    class PreviewComparator implements Comparator<Size>

    {
        int w, h;

        public PreviewComparator(int w, int h) {
            this.w = w;
            this.h = h;

        }

        @Override
        public int compare(Size paramSize1, Size paramSize2) {
            return Math.abs(paramSize1.width * paramSize1.height - this.w * this.h)
                    - Math.abs(paramSize2.width * paramSize2.height - this.w * this.h);
        }

    }

    public void openFlashlight(Camera camera) {
        doSetTorch(camera, true);
    }

    public void closeFlashlight(Camera camera) {
        doSetTorch(camera, false);
    }

    private void doSetTorch(Camera camera, boolean newSetting) {
        Camera.Parameters parameters = camera.getParameters();
        String flashMode;
        /** 是否支持闪光灯 */
        if (newSetting) {
            flashMode = findSettableValue(parameters.getSupportedFlashModes(), Camera.Parameters.FLASH_MODE_TORCH,
                    Camera.Parameters.FLASH_MODE_ON);
        } else {
            flashMode = findSettableValue(parameters.getSupportedFlashModes(), Camera.Parameters.FLASH_MODE_OFF);
        }
        if (flashMode != null) {
            parameters.setFlashMode(flashMode);
        }
        camera.setParameters(parameters);
    }

    private static String findSettableValue(Collection<String> supportedValues, String... desiredValues) {
        String result = null;
        if (supportedValues != null) {
            for (String desiredValue : desiredValues) {
                if (supportedValues.contains(desiredValue)) {
                    result = desiredValue;
                    break;
                }
            }
        }
        return result;
    }

    public int getDisplayOrientation(int caremaId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(caremaId, info);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private static Point getCameraResolution(Camera.Parameters parameters, Point screenResolution) {
        String previewSizeValueString = parameters.get("preview-size-values");
        if (previewSizeValueString == null) {
            previewSizeValueString = parameters.get("preview-size-value");
        }
        Point cameraResolution = null;
        if (previewSizeValueString != null) {
            cameraResolution = findBestPreviewSizeValue(previewSizeValueString, screenResolution);
        }
        if (cameraResolution == null) {
            cameraResolution = new Point((screenResolution.x >> 3) << 3, (screenResolution.y >> 3) << 3);
        }
        return cameraResolution;
    }

    private static Point findBestPreviewSizeValue(CharSequence previewSizeValueString, Point screenResolution) {
        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;
        for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {

            previewSize = previewSize.trim();
            int dimPosition = previewSize.indexOf('x');
            if (dimPosition < 0) {
                continue;
            }

            int newX;
            int newY;
            try {
                newX = Integer.parseInt(previewSize.substring(0, dimPosition));
                newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
            } catch (NumberFormatException nfe) {
                continue;
            }

            int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);
            if (newDiff == 0) {
                bestX = newX;
                bestY = newY;
                break;
            } else if (newDiff < diff) {
                bestX = newX;
                bestY = newY;
                diff = newDiff;
            }

        }

        if (bestX > 0 && bestY > 0) {
            return new Point(bestX, bestY);
        }
        return null;
    }

    private static int findBestMotZoomValue(CharSequence stringValues, int tenDesiredZoom) {
        int tenBestValue = 0;
        for (String stringValue : COMMA_PATTERN.split(stringValues)) {
            stringValue = stringValue.trim();
            double value;
            try {
                value = Double.parseDouble(stringValue);
            } catch (NumberFormatException nfe) {
                return tenDesiredZoom;
            }
            int tenValue = (int) (10.0 * value);
            if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom - tenBestValue)) {
                tenBestValue = tenValue;
            }
        }
        return tenBestValue;
    }

    private void setZoom(Camera.Parameters parameters) {
        String zoomSupportedString = parameters.get("zoom-supported");
        if (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString)) {
            return;
        }

        int tenDesiredZoom = TEN_DESIRED_ZOOM;

        String maxZoomString = parameters.get("max-zoom");
        if (maxZoomString != null) {
            try {
                int tenMaxZoom = (int) (10.0 * Double.parseDouble(maxZoomString));
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom;
                }
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }

        String takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max");
        if (takingPictureZoomMaxString != null) {
            try {
                int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom;
                }
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }

        String motZoomValuesString = parameters.get("mot-zoom-values");
        if (motZoomValuesString != null) {
            tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom);
        }

        String motZoomStepString = parameters.get("mot-zoom-step");
        if (motZoomStepString != null) {
            try {
                double motZoomStep = Double.parseDouble(motZoomStepString.trim());
                int tenZoomStep = (int) (10.0 * motZoomStep);
                if (tenZoomStep > 1) {
                    tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
                }
            } catch (NumberFormatException nfe) {
                // continue
            }
        }
        if (maxZoomString != null || motZoomValuesString != null) {
            parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
        }
        if (takingPictureZoomMaxString != null) {
            parameters.set("taking-picture-zoom", tenDesiredZoom);
        }
    }

    /**
     * 设置照片尺寸为最接近指定尺寸
     *
     * @param list
     * @return
     */
    private void setPicutreSize(List<Size> list, int width, int height) {
        int approach = Integer.MAX_VALUE;

        for (Size size : list) {
            int temp = Math.abs(size.width - width + size.height - height);
            if (approach > temp) {
                approach = temp;
                pictureSize = size;
            }

        }

    }

    // 拍照
    private ToneGenerator tone;

    public void tackPicture(Camera camera, final OnCaptureCallback callback) throws Exception {
        camera.cancelAutoFocus();
        //
        camera.autoFocus(new AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean flag, Camera camera) {
                camera.takePicture(new ShutterCallback() {
                    @Override
                    public void onShutter() {
                        if (tone == null) {
                            // 发出提示用户的声音
                            tone = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
                        }
                        tone.startTone(ToneGenerator.TONE_PROP_BEEP);
                    }
                }, null, new PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        callback.onCapture(data);
                    }
                });
            }
        });
    }

}