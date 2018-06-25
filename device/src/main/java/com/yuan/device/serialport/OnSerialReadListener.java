package com.yuan.device.serialport;

/**
 * Created by YuanYe on 2018/5/30.
 */

public interface OnSerialReadListener {
    /**
     * 读取到的字节
     *
     * @param buffer
     * @param length 本次读取的长度
     */
    void onRead(byte[] buffer, int length);
}
