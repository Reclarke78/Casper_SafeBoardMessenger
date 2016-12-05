package ru.olgathebest.casper;

/**
 * Created by Ольга on 01.12.2016.
 */

import java.nio.charset.Charset;

public class UTF8 {
    public static byte[] encode(String text) {
        return text.getBytes(UTF_8_CHARSET);
    }

    public static String decode(byte[] bytes) {
        return new String(bytes, UTF_8_CHARSET);
    }

    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
}
