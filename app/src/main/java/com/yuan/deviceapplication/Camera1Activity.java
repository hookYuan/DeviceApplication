package com.yuan.deviceapplication;

import android.Manifest;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.yuan.device.camera.camera1.CameraCallBack;
import com.yuan.device.camera.camera1.CameraPreview;

public class Camera1Activity extends AppCompatActivity {

    private static final String TAG = "Camera1Activity";
    private CameraPreview cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1001);
        }

        cameraPreview = (CameraPreview) findViewById(R.id.cameraPreview);
        cameraPreview.cwStartCamera();
        //实时数据回调，可用于二维识别，人脸识别等
        cameraPreview.setCameraCallBack(new CameraCallBack() {
            @Override
            public void getData(byte[] data, int cameraId, int videoWidth, int videoHeight) {
                Log.i(TAG, "数据长度" + data.length);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            Log.i(TAG, "退出重试");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraPreview.cwStopCamera();
    }
}
