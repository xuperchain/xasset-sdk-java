package com.baidu.xasset.auth;

import com.baidu.xuper.api.Account;
import com.baidu.xuper.crypto.Ecc;
import com.baidu.xuper.crypto.Hash;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;

/**
 * 区块链加密库
 */
public class Crypto {
    /**
     * xasset签名完整方法
     *
     * @param account 签名账号
     * @param oriMsg  签名的原始数据
     * @return {@link String}
     * @throws Exception 异常
     */
    public static String xassetSignECDSA(Account account, byte[] oriMsg) throws Exception {
        // 1. 对消息统一做SHA256
        byte[] msg = Hash.sha256(oriMsg);

        // 2. 使用ECC私钥来签名
        BigInteger k = account.getKeyPair().getPrivateKey();
        byte[] signature;
        signature = Ecc.sign(msg, k);

        // 3. 对签名转化为16进制字符串显示
        return EncodeSign(signature);
    }

    static String EncodeSign(byte[] src) {
        return Hex.toHexString(src);
    }
}
