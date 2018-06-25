package com.yuan.device.gpio;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by YuanYe on 2018/6/13.
 * GPIO口控制控制读取，这里采用的方式是与linux共用文件的方式达到gpio的数据读写
 * <p>
 * “/sys/class/yusong/powerswitch/gpio1”需要宇松设备支持
 * 在宇松设备YS3288上，gpio1口控制继电器开关，发送继电器脉冲
 * 可以连接闸机等支持继电器设备
 */
public class GPIO1Helper {

    private static GPIO1Helper helper;
    private ExecutorService gpioPool;

    private GPIO1Helper() {
        gpioPool = Executors.newSingleThreadExecutor();
    }

    public static GPIO1Helper getInstance() {
        if (helper == null) helper = new GPIO1Helper();
        if (helper == null) throw new NullPointerException("GPIO1Helper初始化失败");
        return helper;
    }

    /**
     * 向发送gpio1命令
     *
     * @param gpioFilePath /sys/class/yusong/powerswitch/gpio1
     * @Param cmd 需要发送的指令
     */
    private void write(String gpioFilePath, String cmd) {
        try {
            //读取当前io状态
//            BufferedReader reader = new BufferedReader(new FileReader("/sys/class/yusong/powerswitch/gpio1"));
//            String str = reader.readLine();
//            reader.close();
            BufferedWriter writer = new BufferedWriter(new FileWriter(gpioFilePath));
            writer.write(cmd);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
