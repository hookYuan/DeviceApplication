package com.yuan.deviceapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera:
                open(Camera1Activity.class);
                break;
            case R.id.btn_camera2:
                open(Camera2Activity.class);
                break;
            case R.id.btn_serialPort:

                break;
            case R.id.btn_usb:

                break;
        }
    }

    private void open(Class clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }
}
