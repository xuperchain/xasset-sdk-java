package com.baidu.xasset.auth;

import com.baidu.xuper.api.Account;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CryptoTest {
    @Test
    public void TestEncodeSign() {
        String msg = Crypto.EncodeSign("Hello".getBytes());
        assertEquals(msg, "48656c6c6f");
    }

    @Test
    public void TestXassetSignECDSA() {
        Account acc = XchainAccount.newXchainEcdsaAccount(XchainAccount.mnemStrgthStrong, XchainAccount.mnemLangCN);
        String sign = null;
        try {
            sign = Crypto.xassetSignECDSA(acc, "Hello".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(sign);
    }
}
