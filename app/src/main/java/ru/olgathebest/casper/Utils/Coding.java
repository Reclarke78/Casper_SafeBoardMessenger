package ru.olgathebest.casper.utils;

/**
 * Created by Ольга on 01.12.2016.
 */

import java.nio.charset.Charset;

public class Coding {
    public static byte[] encode(String text) {
        return text.getBytes(UTF_8_CHARSET);
    }

    public static String decode(byte[] bytes) {
        return new String(bytes, UTF_8_CHARSET);
    }

    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

    public static String bytesToHex(byte[] data) {
        if (data == null) {
            return null;
        }

        int len = data.length;
        String str = "";
        for (int i = 0; i < len; i++) {
            if ((data[i] & 0xFF) < 16)
                str = str + "0" + java.lang.Integer.toHexString(data[i] & 0xFF);
            else
                str = str + java.lang.Integer.toHexString(data[i] & 0xFF);
        }
        return str;
    }

    public static byte[] hexToBytes(String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }
    }
}
