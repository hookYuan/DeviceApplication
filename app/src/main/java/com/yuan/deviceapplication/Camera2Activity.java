package com.yuan.deviceapplication;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.yuan.device.camera.camera2.Camera2Fragment;
import com.yuan.device.camera.camera2.CameraCallBack;

public class Camera2Activity extends AppCompatActivity {
    private static final String TAG = "Camera2Activity";

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        Camera2Fragment fragment = Camera2Fragment.newInstance();
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content, fragment)
                    .commit();
        }
        fragment.setCameraCallBack(new CameraCallBack() {
            @Override
            public void getData(byte[] data, int angle, int videoWidth, int videoHeight) {
                Log.i(TAG, "数据长度" + data.length);
            }
        });
    }
}
