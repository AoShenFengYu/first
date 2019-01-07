package com.qisiemoji.apksticker.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MD5 {
    public static String getMD5(String val) {
        if (val != null && !val.isEmpty()) {
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(val.getBytes());
                byte[] m = md5.digest();
                return md5ToHex(m);
            } catch (NoSuchAlgorithmException e) {

            }
        }
        return "";

    }

    /**
     * 获取字节数组的MD5
     *
     * @param bytes 要获取MD5值的字符串
     * @return byte数组的32位小写MD5字符串, 出现异常则返回空字符串
     */
    public static String getByteArrayMD5(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] result = digest.digest(bytes);
            return md5ToHex(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取文件的MD5
     *
     * @param file 要获取MD5值的文件
     * @return 文件的32位小写MD5字符串, 出现异常则返回空字符串
     */
    public static String getFileMD5(File file) {
        if (file == null) {
            return "";
        }
        FileInputStream fis = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            byte[] result = digest.digest();
            return md5ToHex(result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtils2.closeQuietly(fis);
        }
        return "";
    }

    /**
     * 获取某个文件部分内容的MD5值 具体算法: 如果文件长度小于4096字节, 直接计算整个文件的MD5, 如果文件长度大于4096字节,
     * 取其前1024字节和后1024字节, 计算这部分的MD5
     *
     * @param file 要获取部分内容MD5值的文件
     * @return 文件部分内容的32位小写MD5字符串, 出现异常则返回空字符串
     */
    public static String getFilePartMD5(File file) {
        if (file == null) {
            return "";
        }
        if (file.length() < 4096) {
            return getFileMD5(file);
        } else {
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(file, "r");
                byte[] headAndTail = new byte[2048];
                raf.seek(0);
                raf.read(headAndTail, 0, 1024);
                raf.seek(raf.length() - 1024);
                raf.read(headAndTail, 1024, 1024);
                return getByteArrayMD5(headAndTail);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                FileUtils2.closeQuietly(raf);
            }
            return "";
        }
    }

    private static String md5ToHex(byte[] md5) {
        if (md5 == null || md5.length == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int offset = 0; offset < md5.length; offset++) {
            byte b = md5[offset];
            if ((b & 0xFF) < 0x10) sb.append("0");
            sb.append(Integer.toHexString(b & 0xFF));
        }
        return sb.toString();
    }

    private static final String AES = "AES";

    private static final String CRYPT_KEY = "YUUAtestYUUAtest";

    /**
     * 加密
     *
     * @return
     */
    public static byte[] encrypt(byte[] src, String key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        SecretKeySpec securekey = new SecretKeySpec(key.getBytes(), AES);
        cipher.init(Cipher.ENCRYPT_MODE, securekey);//设置密钥和加密形式
        return cipher.doFinal(src);
    }

    /**
     * 解密
     *
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] src, String key)  throws Exception  {
        Cipher cipher = Cipher.getInstance(AES);
        SecretKeySpec securekey = new SecretKeySpec(key.getBytes(), AES);//设置加密Key
        cipher.init(Cipher.DECRYPT_MODE, securekey);//设置密钥和解密形式
        return cipher.doFinal(src);
    }

    /**
     * 二行制转十六进制字符串
     *
     * @param b
     * @return
     */
    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;
        }
        return hs.toUpperCase();
    }

    public static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0)
            throw new IllegalArgumentException("长度不是偶数");
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }

    /**
     * 解密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public final static String decrypt(String data) {
        try {
            return new String(decrypt(hex2byte(data.getBytes()),
                    CRYPT_KEY));
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public final static String encrypt(String data) {
        try {
            return byte2hex(encrypt(data.getBytes(), CRYPT_KEY));
        } catch (Exception e) {
        }
        return null;
    }
}
