//package com.baidu.xasset.client.xasset;
//
//import com.baidu.xasset.auth.XchainAccount;
//import com.baidu.xasset.client.base.BaseDef;
//import com.baidu.xasset.client.base.BaseDef.Resp;
//import com.baidu.xuper.api.Account;
//import com.baidu.xasset.client.xasset.XassetDef.*;
//import com.baidubce.services.ses.model.SendEmailRequest;
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
//        // 离线生成区块链账户
//        Account acc1 = XchainAccount.newXchainEcdsaAccount(2, 1);
//        Account acc2 = XchainAccount.newXchainEcdsaAccount(2, 1);
//        Asset handle = new Asset(Config.getXassetConfig(), Logger.getGlobal());
//
//        // 1. 使用手百小程序生成区块链账户
//        Resp<BdBoxRegisterResp> result1 = handle.bdboxRegister("open_id", "app_key");
//        System.out.printf("区块链账户地址: %s\n", result1.apiResp.address);
//
//        // 2. 手百小程序绑定已有区块链账户
//        handle.bdboxBind("open_id", "app_key", acc1.getMnemonic());
//
//        // 3. 第三方应用绑定区块链账户
//        handle.bindByUnionId("union_id", "mnemonic");
//
//        // 4. 创造数字资产
//        AssetInfo assetInfo = new AssetInfo(XassetDef.ASSETCATEART, "welcome",
//                new String[]{"bos_v1://bucket/object/1000_500"}, "welcome xasset",
//                new String[]{"bos_v1://bucket/object/1000_500"}, new String[]{"bos_v1://bucket/object/1000_500"},
//                null, null, 0);
//
//        Resp<CreateAssetResp> result4 = handle.createAsset(acc1, 10, assetInfo, 0, 0);
//        long assetId = result4.apiResp.assetId;
//        System.out.printf("创造资产: %d\n", assetId);
//        Thread.sleep(1000);
//
//        // 5. 发行数字资产
//        handle.publishAsset(acc1, assetId, 0);
//        System.out.printf("发行资产: %d\n", assetId);
//        Thread.sleep(100000);
//
//        // 6. 查询数字资产详情
//        Resp<QueryAssetResp> result6 = handle.queryAsset(assetId);
//        System.out.printf("查询资产: %s\n", result6.apiResp.meta);
//        Thread.sleep(1000);
//
//        // 7. 授予数字资产碎片
//        Resp<GrantShardResp> result7 = handle.grantShard(acc1,assetId, 0, acc2.getAKAddress(), 0, 0);
//        long shardId = result7.apiResp.shardId;
//        System.out.printf("授予资产碎片: %d\n", shardId);
//        Thread.sleep(10000);
//
//        // 8. 查询指定资产碎片信息
//        Resp<QueryShardsResp> result8 = handle.queryShards(assetId, shardId);
//        System.out.printf("查询资产碎片信息: %s\n", result8.apiResp.meta);
//        Thread.sleep(1000);
//
//        // 9. 分页拉取指定账户持有碎片列表
//        Resp<BaseDef.ListPageResp> result9 = handle.listShardsByAddr(acc2.getAKAddress(), 1, 20, 0);
//        System.out.printf("分页拉取指定账户持有碎片列表: %s\n", result9.apiResp.list);
//        Thread.sleep(1000);
//
//        // 10. 获取资产存证信息
//        Resp<GetEvidenceInfoResp> result10 = handle.getEvidenceInfo(assetId);
//        System.out.printf("获取存证信息: %s\n", result10.apiResp.assetInfo);
//
//        // 11. 核销数字资产碎片
//        handle.consumeShard(acc1, acc2, assetId, shardId);
//        Thread.sleep(1000);
//
//        // 12. 冻结数字资产
//        handle.freezeAsset(assetId, acc1);
//
//        // 13. 再次授予数字资产碎片
//        Resp<GrantShardResp> result13 = handle.grantShard(acc1,assetId, 0, acc2.getAKAddress(), 0, 0);
//        shardId = result13.apiResp.shardId;
//        System.out.printf("授予资产碎片: %d\n", shardId);
//
//        // 14. 获取碎片记录
//        Resp<BaseDef.ListPageResp> result14 = handle.history(assetId, 1, 20, shardId);
//        System.out.printf("拉取碎片记录：%s\n", result14.apiResp.list);
//
//        // 15. 应用场景拉取addr
//        Resp<BaseDef.ListAddrResp> result15 = handle.scenelistAddr("union_id");
//        AddrMeta[] list1 = (AddrMeta[]) result15.apiResp.list;
//        System.out.printf("应用场景授权地址：%s\n", list1[0].addr);
//
//        // 16. 应用场景拉取addr下允许访问的藏品列表
//        Resp<BaseDef.ListCursorResp> result16 = handle.scenelistsdsbyaddr(list1[0].addr, list1[0].token, "", 10);
//        SceneShardInfo[] list2 = (SceneShardInfo[]) result16.apiResp.list;
//        System.out.printf("应用场景查询藏品列表：%s\n", list2[0].title);
//
//        // 17. 应用场景判断addr下是否有指定数字藏品
//        Resp<HasAssetByAddrResp> result17 = handle.scenehasastbyaddr(list1[0].addr, list1[0].token, "[623062333210793858 , 623062333210793859 , 623062333210793856]");
//        System.out.println(result17.apiResp.result.get("623062333210793858").intValue());
//
//        // 18. 应用场景拉取addr下藏品变更记录
//        Resp<BaseDef.ListCursorResp> result18 = handle.scenelistDiffByAddr(list1[0].addr, list1[0].token, "", 10);
//        ShardDiffInfo[] list3 = (ShardDiffInfo[]) result18.apiResp.list;
//        System.out.println(list3.length);
//
//        // 19. 应用场景查询碎片详情
//        Resp<SceneQueryShardsResp> result19 = handle.scenequeryShards(list1[0].addr, list1[0].token, list2[0].assetId, list2[0].shardId);
//        System.out.printf("应用场景查询碎片详情：%s\n", result19.apiResp.meta.jumpLink);
//    }
//}
