package com.whoami.myblog.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author admin
 * @date 2020年3月26日
 * @description
 *
 */
public class FileMd5Util {

    public static String getMD5(FileInputStream fileInputStream) {
        String md5Hex = null;
        try {
            md5Hex = DigestUtils.md5Hex(fileInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return md5Hex;
    }

    public static void main(String[] args) {
        String path = "F:\\毕设\\images\\2021_02_02\\jpg\\806183684204920832.jpg";

        long start = new Date().getTime();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String md5 = getMD5(fileInputStream);
        long end = new Date().getTime();
        System.out.println(md5 + "----" + (end - start) + "ms");

    }
}


