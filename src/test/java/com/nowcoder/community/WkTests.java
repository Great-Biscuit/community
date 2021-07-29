package com.nowcoder.community;

import java.io.IOException;

/**
 * wk工具html转pdf或image
 */
public class WkTests {

    public static void main(String[] args) {
        String str = "D:/wkhtmltopdf/bin/wkhtmltoimage --quality 75 https://blog.csdn.net/qq_43312049 D:/wkhtmltopdf/wk-image/GreatBiscuit.png";
        try {
            Runtime.getRuntime().exec(str);
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
