package com.yuan.device.serialport;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by YuanYe on 2018/5/30.
 * 串口操作工具类
 * 初始化串口对象时，需要传入串口名称，例如：/dev/ttyS4
 */
public class SerialPortHelper {

    private final static String TAG = "SerialPortHelper";

    private ExecutorService serialPortReadPool;//串口读线程
    private ExecutorService serialPortWritePool;//串口写线程
    private int baud = BaudRate.b9600; //波特率
    private SerialPort mSerialPort;
    private OnSerialReadListener readListener;

    /**
     * 串口名称可以通过Finder所有串口日志查看串口名
     *
     * @param portPath
     */
    public SerialPortHelper(String portPath) {
        serialPortReadPool = Executors.newSingleThreadExecutor();
        serialPortWritePool = Executors.newSingleThreadExecutor();
        open(portPath);
        serialPortReadPool.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //每次读取8位数据
                    byte[] buffer = new byte[64];
                    try {
                        if (mSerialPort.getInputStream() != null) {
                            int size = mSerialPort.getInputStream().read(buffer);
                            if (size > 0 && readListener != null) readListener.onRead(buffer, size);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "串口读取数据异常:" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    /**
     * 通过串口写入数据
     *
     * @param bytes
     */
    public void writeBytes(final byte[] bytes) {
        serialPortWritePool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mSerialPort.getOutputStream() != null && bytes != null && bytes.length > 0) {
                        mSerialPort.getOutputStream().write(bytes);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "串口写入数据异常:" + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e(TAG, "串口写入数据异常2:" + e.getMessage());
                }
            }
        });
    }

    /**
     * 通过设置监听，实时接收串口收到的数据
     *
     * @param listener
     */
    public void setOnSerialReadListener(OnSerialReadListener listener) {
        this.readListener = listener;
    }

    //关闭串口
    public void destroy() {
        if (mSerialPort != null) mSerialPort.close();
    }


    /**
     * @param portPath 根据指定地址判断串口是否可用
     * @return
     */
    private boolean isSerialAvailable(@NonNull String portPath) {
        SerialPortFinder mSerialPortFinder = new SerialPortFinder();
        for (int i = 0; i < mSerialPortFinder.getAllDevicesPath().length; i++) {
            if (portPath.equals(mSerialPortFinder.getAllDevicesPath()[i])) return true;
        }
        return false;
    }

    /**
     * 通过串口发送数据
     */
    private void open(String portPath) {
        try {
            if (isSerialAvailable(portPath)) {
                exeShell(portPath);
                mSerialPort = new SerialPort(new File(portPath), baud, 0);
            } else {
                Log.e(TAG, "指定的串口地址不存在");
            }
        } catch (IOException e) {

        }
    }

    /**
     * 发送Linux命令，申请串口文件读写权限
     *
     * @param name
     */
    private void exeShell(String name) {
        //获得root权限（临时获取Root权限，系统重启失效）
//        String command = "chmod -R 777 /dev";
//        try {
//            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
//            process.waitFor();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String cmd = "chmod 777 " + name;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                Log.i("exeShell", line);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
