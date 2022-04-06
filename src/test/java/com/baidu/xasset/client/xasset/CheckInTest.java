//package com.baidu.xasset.client.xasset;
//
//import com.baidu.xasset.auth.XchainAccount;
//import com.baidu.xasset.client.base.BaseDef;
//import com.baidu.xasset.client.base.BaseDef.Resp;
//import com.baidu.xuper.api.Account;
//import com.baidu.xasset.client.xasset.XassetDef.*;
//import org.junit.Test;
//
//import java.util.logging.Logger;
//
//public class CheckInTest {
//    @Test
//    public void TestGetStoken() {
//        Account acc = XchainAccount.newXchainEcdsaAccount(2, 1);
//        Asset handle = new Asset(Config.getXassetConfig(), Logger.getGlobal());
//        Resp<GetStokenResp> result = handle.getStoken(acc);
//        System.out.println(result.apiResp.accessInfo);
//    }
//
//    @Test
//    public void TestE2E() throws Exception {
//        Account acc1 = XchainAccount.newXchainEcdsaAccount(2, 1);
//        Account acc2 = XchainAccount.newXchainEcdsaAccount(2, 1);
//        Asset handle = new Asset(Config.getXassetConfig(), Logger.getGlobal());
//
//        // 1. 创造数字资产
//        AssetInfo assetInfo = new AssetInfo(XassetDef.ASSETCATEART, "welcome",
//                new String[]{"bos_v1://bucket/object/1000_500"}, "welcome xasset",
//                new String[]{"bos_v1://bucket/object/1000_500"}, new String[]{"bos_v1://bucket/object/1000_500"},
//                null, null, 0);
//
//        Resp<CreateAssetResp> result1 = handle.createAsset(acc1, 10, assetInfo, 0, 0);
//        long assetId = result1.apiResp.assetId;
//        System.out.printf("创造资产: %d\n", assetId);
//        Thread.sleep(1000);
//
//        // 2. 发行数字资产
//        handle.publishAsset(acc1, assetId, 1);
//        System.out.printf("发行资产: %d\n", assetId);
//        Thread.sleep(10000);
//
//        // 3. 查询数字资产详情
//        Resp<QueryAssetResp> result3 = handle.queryAsset(assetId);
//        System.out.printf("查询资产: %s\n", result3.apiResp.meta);
//        Thread.sleep(1000);
//
//        // 4. 授予数字资产碎片
//        Resp<GrantShardResp> result4 = handle.grantShard(acc1,assetId, 0, acc2.getAKAddress(), 0, 0);
//        long shardId = result4.apiResp.shardId;
//        System.out.printf("授予资产碎片: %d\n", shardId);
//        Thread.sleep(10000);
//
//        // 5. 查询指定资产碎片信息
//        Resp<QueryShardsResp> result5 = handle.queryShards(assetId, shardId);
//        System.out.printf("查询资产碎片信息: %s\n", result5.apiResp.meta);
//        Thread.sleep(1000);
//
//        // 6. 分页拉取指定账户持有碎片列表
//        Resp<BaseDef.ListPageResp> result6 = handle.listShardsAddr(acc2.getAKAddress(), 1, 20);
//        System.out.printf("分页拉取指定账户持有碎片列表: %s\n", result6.apiResp.list);
//        Thread.sleep(1000);
//
//        // 7. 获取资产存证信息
//        Resp<GetEvidenceInfoResp> result7 = handle.getEvidenceInfo(assetId);
//        System.out.printf("获取存证信息: %s\n", result7.apiResp.assetInfo);
//    }
//}
