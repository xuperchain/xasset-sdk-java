package com.baidu.xasset.auth;

import com.baidu.xuper.api.Account;

/**
 * 区块链账户库
 */
public class XchainAccount {
    /**
     * 弱强度（12个助记词）
     */
    public final static int mnemStrgthWeak = 1;
    /**
     * 中强度（18个助记词）
     */
    public final static int mnemStrgthMedium = 2;
    /**
     * 强强度（24个助记词）
     */
    public final static int mnemStrgthStrong = 3;

    /**
     * 中文助记词
     */
    public final static int mnemLangCN = 1;
    /**
     * 英文助记词
     */
    public final static int mnemLangEN = 2;

    /**
     * 创建区块链账户
     *
     * @param mnemStrgth 助记词强度
     * @param mnemLang   助记词语言类型
     * @return {@link Account}
     */
    public static Account newXchainEcdsaAccount(int mnemStrgth, int mnemLang) {
        return Account.create(mnemStrgth, mnemLang);
    }
}
