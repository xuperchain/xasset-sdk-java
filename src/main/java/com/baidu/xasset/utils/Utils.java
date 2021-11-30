package com.baidu.xasset.utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class Utils {
    public static long genAssetId(long appId) {
        return genIdHelp(appId, 0);
    }

    public static long genRandId() {
        long nano = System.currentTimeMillis() * 1000000L + System.nanoTime() % 1000000L;
        Random rand = new Random(nano);
        long randNum1 = rand.nextLong();
        long randNum2 = rand.nextLong();
        int shift1 = rand.nextInt(16) + 2;
        int shift2 = rand.nextInt(8) + 1;

        return ((randNum1 >> shift1) + (randNum2 >> shift2) + (nano >> 1)) & 0x7FFFFFFFFFFFFFFFL;
    }

    public static long genNonce() {
        long randId1 = genRandId();
        long randId2 = genRandId();
        long timestamp = System.currentTimeMillis() * 1000000L + System.nanoTime() % 1000000L;
        String content = String.format("%d#%d#%d", randId1, randId2, timestamp);
        long sign = strSignToInt(content);
        long high = sign & 0x7FFFFFFF00000000L;
        long low = (System.currentTimeMillis() / 1000L) & 0x00000000FFFFFFFFL;
        return high + low;
    }

    /**
     * | 0 - 31 | 32 | 33 - 40 | 41 - 56 | 57 - 62 | 63 | | 32位 | 1位 | 8位 | 16位 | 6位
     * | 1位| | baseId低32位 | 标记 | 随机值 | 签名低16位| 预留 | 0 |
     *
     * 生成包含基础值的伪唯一ID，保证极小概率出现重复
     *
     * @param baseId 要编码进的低32为的基础值
     * @param flag   32位标记位的值，默认填0
     * @return long
     */
    public static long genIdHelp(long baseId, int flag) {
        long s, r, lk;
        long timestamp = System.currentTimeMillis() * 1000000L + System.nanoTime() % 1000000L;
        String content = String.format("%d#%d#%d", baseId, flag, timestamp);
        s = strSignToInt(content);
        r = new Random().nextInt() % 256;
        lk = baseId;

        long id;
        id = (lk & 0x00000000ffffffffL);
        if (flag == 1) {
            id += (0x0000000000000001L << 32);
        }
        id += ((r & 0x00000000000000ffL) << 33);
        id += ((s & 0x000000000000ffffL) << 41);

        return id;
    }

    private static long strSignToInt(String content) {
        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            digest = md.digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] seg1Byte = null, seg2Byte = null, seg3Byte = null, seg4Byte = null;
        assert digest != null;
        seg1Byte = Arrays.copyOfRange(digest, 0, 4);
        seg2Byte = Arrays.copyOfRange(digest, 4, 8);
        seg3Byte = Arrays.copyOfRange(digest, 8, 12);
        seg4Byte = Arrays.copyOfRange(digest, 12, 16);

        int seg1, seg2, seg3, seg4 = 0;
        ByteBuffer bb1 = ByteBuffer.wrap(seg1Byte);
        bb1.order(ByteOrder.LITTLE_ENDIAN);
        seg1 = bb1.getInt();

        ByteBuffer bb2 = ByteBuffer.wrap(seg2Byte);
        bb2.order(ByteOrder.LITTLE_ENDIAN);
        seg2 = bb2.getInt();

        ByteBuffer bb3 = ByteBuffer.wrap(seg3Byte);
        bb3.order(ByteOrder.LITTLE_ENDIAN);
        seg3 = bb3.getInt();

        ByteBuffer bb4 = ByteBuffer.wrap(seg4Byte);
        bb4.order(ByteOrder.LITTLE_ENDIAN);
        seg4 = bb4.getInt();

        long sign1 = (long) (seg1 + seg3);
        long sign2 = (long) (seg2 + seg4);
        return (sign1 & 0x00000000ffffffffL) | (sign2 << 32);
    }
}
