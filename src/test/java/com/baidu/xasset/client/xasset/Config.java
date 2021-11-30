package com.baidu.xasset.client.xasset;

import com.baidu.xasset.common.config.Config.XassetCliConfig;

class Config {
    static XassetCliConfig getXassetConfig() {
//        long appId = 300200;
//        String ak = "48e9decafcb01254e4a32d81ff9498f1";
//        String sk = "7b1bd8bb6e15ef5b159f99cccba642d2";


//        XassetCliConfig cfg = new XassetCliConfig();
//        cfg.setCredentials(appId, ak, sk);
//        cfg.setEndPoint("180.76.152.242:8360");
//        cfg.setEndPoint("120.48.16.137:8360");
//        cfg.setEndPoint("10.228.227.167:8009");


        long appId = 300100;
        String ak = "032b9af2f1b776d69c8a55031f2ae68e";
        String sk = "2cb51374f71d8d274b370685d36d2280";

        XassetCliConfig cfg = new XassetCliConfig();
        cfg.setCredentials(appId, ak, sk);
//        cfg.setEndPoint("180.76.152.242:8360");
//        cfg.setEndPoint("120.48.16.137:8360");
        cfg.setEndPoint("10.117.131.18:8360");
        return cfg;
    }
}
