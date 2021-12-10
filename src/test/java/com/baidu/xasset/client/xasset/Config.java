package com.baidu.xasset.client.xasset;

import com.baidu.xasset.common.config.Config.XassetCliConfig;

class Config {
    static XassetCliConfig getXassetConfig() {
        long appId = 0;
        String ak = "xxx";
        String sk = "xxx";

        XassetCliConfig cfg = new XassetCliConfig();
        cfg.setCredentials(appId, ak, sk);
        cfg.setEndPoint("http://120.48.16.137:8360");
        return cfg;
    }
}
