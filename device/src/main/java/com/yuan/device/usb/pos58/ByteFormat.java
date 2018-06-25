package com.yuan.device.usb.pos58;

import java.io.UnsupportedEncodingException;

/**
 * Created by YuanYe on 2018/6/22.
 * 设置StringBuilder的简单样式
 */
public class ByteFormat {
    // 对齐方式
    public static final int ALIGN_LEFT = 0;     // 靠左
    public static final int ALIGN_CENTER = 1;   // 居中
    public static final int ALIGN_RIGHT = 2;    // 靠右

    //字体大小
    public static final int FONT_NORMAL = 0;    // 正常
    public static final int FONT_MIDDLE = 1;    // 中等
    public static final int FONT_BIG = 2;       // 大

    //加粗模式
    public static final int FONT_BOLD = 0;              // 字体加粗
    public static final int FONT_BOLD_CANCEL = 1;       // 取消加粗


    /**
     * 切纸
     *
     * @return
     */
    public static String getCutPaperCmd() {
        // 走纸并切纸，最后一个参数控制走纸的长度
        byte[] data = {(byte) 0x1d, (byte) 0x56, (byte) 0x42, (byte) 0x15};
        return new String(data);
    }

    /**
     * 对齐方式
     *
     * @param alignMode
     * @return
     */
    public static String getAlignCmd(int alignMode) {
        byte[] data = {(byte) 0x1b, (byte) 0x61, (byte) 0x0};
        if (alignMode == ALIGN_LEFT) {
            data[2] = (byte) 0x00;
        } else if (alignMode == ALIGN_CENTER) {
            data[2] = (byte) 0x01;
        } else if (alignMode == ALIGN_RIGHT) {
            data[2] = (byte) 0x02;
        }
        return new String(data);
    }

    /**
     * 字体大小
     *
     * @param fontSize
     * @return
     */
    public static String getFontSizeCmd(int fontSize) {
        byte[] data = {(byte) 0x1d, (byte) 0x21, (byte) 0x0};
        if (fontSize == FONT_NORMAL) {
            data[2] = (byte) 0x00;
        } else if (fontSize == FONT_MIDDLE) {
            data[2] = (byte) 0x01;
        } else if (fontSize == FONT_BIG) {
            data[2] = (byte) 0x11;
        }

        return new String(data);
    }

    /**
     * 加粗模式
     *
     * @param fontBold
     * @return
     */
    public static String getFontBoldCmd(int fontBold) {
        byte[] data = {(byte) 0x1b, (byte) 0x45, (byte) 0x0};

        if (fontBold == FONT_BOLD) {
            data[2] = (byte) 0x01;
        } else if (fontBold == FONT_BOLD_CANCEL) {
            data[2] = (byte) 0x00;
        }
        return new String(data);
    }

    /**
     * 打开钱箱
     *
     * @return
     */
    public static String getOpenDrawerCmd() {
        byte[] data = new byte[4];
        data[0] = 0x10;
        data[1] = 0x14;
        data[2] = 0x00;
        data[3] = 0x00;
        return new String(data);
    }

    /**
     * 字符串转字节数组
     *
     * @param str
     * @return
     */
    public static byte[] stringToBytes(String str) {
        byte[] data = null;
        try {
            byte[] strBytes = str.getBytes("utf-8");

            data = (new String(strBytes, "utf-8")).getBytes("gbk");
        } catch (UnsupportedEncodingException exception) {
            exception.printStackTrace();
        }

        return data;
    }

    /**
     * 字节数组合并
     *
     * @param bytesA
     * @param bytesB
     * @return
     */
    public static byte[] byteMerger(byte[] bytesA, byte[] bytesB) {
        byte[] bytes = new byte[bytesA.length + bytesB.length];
        System.arraycopy(bytesA, 0, bytes, 0, bytesA.length);
        System.arraycopy(bytesB, 0, bytes, bytesA.length, bytesB.length);
        return bytes;
    }
}