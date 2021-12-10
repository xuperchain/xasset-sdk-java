package com.baidu.xasset.auth;

import com.baidu.xuper.api.Account;
import org.junit.Test;

public class AccountTest {
    @Test
    public void TestNewXchainEcdsaAccount() {
        Account acc = XchainAccount.newXchainEcdsaAccount(XchainAccount.mnemStrgthStrong, XchainAccount.mnemLangCN);
        System.out.println(acc.getAKAddress());
    }
}
