package com.baidu.xasset.client.xasset;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.baidu.xasset.client.base.BaseDef.RequestRes;

/**
 * 数字资产平台登记SaaS定义
 */
public class XassetDef {
    /**
     * 资产类型：艺术品
     */
    public final static int ASSETCATEART = 1;
    /**
     * 资产类型：收藏品
     */
    public final static int ASSETCATECOLLECT = 2;
    /**
     * 资产类型：门票
     */
    public final static int ASSETCATETICKET = 3;
    /**
     * 资产类型：酒店
     */
    public final static int ASSETCATEHOTEL = 4;

    /**
     * 数字资产信息。配置资产分类、名称、缩略图等
     */
    public static class AssetInfo {
        @JSONField(name = "asset_cate")
        public int assetCate;
        @JSONField(name = "title")
        public String title;
        @JSONField(name = "thumb")
        public String[] thumb;
        @JSONField(name = "short_desc")
        public String shortDesc;
        @JSONField(name = "img_desc")
        public String[] imgDesc;
        @JSONField(name = "asset_url")
        public String[] assetUrl;
        @JSONField(name = "long_desc")
        public String longDesc;
        @JSONField(name = "asset_ext")
        public String assetExt;
        @JSONField(name = "group_id")
        public long groupId;

        /**
         * 创建资产信息
         *
         * @param assetCate 资产分类
         * @param title     资产名称
         * @param thumb     资产缩略图。格式：bos_v1://{bucket}/{object_path}/{width}_{height}
         * @param shortDesc 资产短文本描述
         * @param imgDesc   资产详情介绍长图。格式：bos_v1://{bucket}/{object_path}/{width}_{height}
         * @param assetUrl  资产原始属性。格式：bos_v1://{bucket}/{object_path}/
         * @param longDesc  资产长文本描述（可选）
         * @param assetExt  资产额外描述信息json字符串（可选）
         * @param groupId   资产组id
         */
        public AssetInfo(int assetCate, String title, String[] thumb, String shortDesc, String[] imgDesc,
                         String[] assetUrl, String longDesc, String assetExt, long groupId) {
            this.assetCate = assetCate;
            this.title = title;
            this.thumb = thumb;
            this.shortDesc = shortDesc;
            this.imgDesc = imgDesc;
            this.assetUrl = assetUrl;
            this.longDesc = longDesc;
            this.assetExt = assetExt;
            this.groupId = groupId;
        }
    }

    /**
     * 通过手百小程序注册链上账户返回值
     *
     * requestId    请求id
     * errNo        错误码
     * errMsg       错误信息
     * address      区块链账户地址
     * mnemonic     区块链账户助记词
     * isNew        是否是新账户 0：老账户 1：新账户
     */
    public static class BdBoxRegisterResp {
        public long requestId;
        public int errNo;
        public String errMsg;
        public String address;
        public String mnemonic;
        public int isNew;


        BdBoxRegisterResp(long requestId, int errNo, String errMsg, String address, String mnemonic, int isNew) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.address = address;
            this.mnemonic = mnemonic;
            this.isNew = isNew;
        }
    }

    /**
     * 获取访问BOS临时STS资源返回值
     *
     * requestId    请求id
     * errNo        错误码
     * errMsg       错误信息
     * accessInfo   访问信息
     */
    public static class GetStokenResp {
        public long requestId;
        public int errNo;
        public String errMsg;
        public AccessInfo accessInfo;

        GetStokenResp(long requestId, int errNo, String errMsg, AccessInfo accessInfo) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.accessInfo = accessInfo;
        }
    }

    /**
     * 获取访问BOS临时STS资源 访问信息数据结构
     */
    public static class AccessInfo {
        public String bucket;
        public String endPoint;
        public String objectPath;
        public String accessKeyId;
        public String secreteAccessKey;
        public String sessionToken;
        public String createTime;
        public String expiration;

        AccessInfo(String bucket, String endPoint, String objectPath, String accessKeyId, String secreteAccessKey, String sessionToken, String createTime, String expiration) {
            this.bucket = bucket;
            this.endPoint = endPoint;
            this.objectPath = objectPath;
            this.accessKeyId = accessKeyId;
            this.secreteAccessKey = secreteAccessKey;
            this.sessionToken = sessionToken;
            this.createTime = createTime;
            this.expiration = expiration;
        }
    }

    /**
     * 上传文件返回值
     *
     * link     资产链接。例如创建资产时设置的缩略图链接、长图链接、原始属性链接
     * resp     获取访问BOS临时STS资源返回值
     * res      http请求返回值
     */
    public static class UploadFile {
        public String link;
        public GetStokenResp resp;
        public RequestRes res;

        UploadFile(String link, GetStokenResp resp, RequestRes res) {
            this.link = link;
            this.resp = resp;
            this.res = res;
        }
    }

    /**
     * 创建数字资产返回值
     *
     * requestId    请求id
     * errno        错误码
     * errMsg       错误信息
     * assetId      数字资产id
     */
    public static class CreateAssetResp {
        public long requestId;
        public int errNo;
        public String errMsg;
        public long assetId;

        CreateAssetResp(long requestId, int errNo, String errMsg, long assetId) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.assetId = assetId;
        }
    }

    /**
     * 查询数字资产详情返回值
     *
     * requestId    请求id
     * errNo        错误码
     * errMsg       错误信息
     * meta         数字资产详情
     */
    public static class QueryAssetResp {
        public long requestId;
        public int errNo;
        public String errMsg;
        public AssetMeta meta;

        QueryAssetResp(long requestId, int errNo, String errMsg, AssetMeta meta) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.meta = meta;
        }
    }

    /**
     * 数字资产详情数据结构
     *
     * assetId      资产ID
     * assetCate    资产分类。1：艺术品 2：收藏品 3：门票 4：酒店
     * title        资产名称
     * thumb        资产缩略图
     * shortDesc    短文字描述
     * longDesc     长文字描述
     * imgDesc      资产详情介绍长图
     * assetUrl     资产原始文件
     * amount       资产发行数量
     * price        资产显示价格，单位为分
     * status       资产状态。1：初始 3：发行中 4：发行成功 5:冻结中 6:已冻结 7：封禁中 8:已封禁
     * assetExt     资产额外描述信息json字符串
     * createAddr   资产创建者区块链地址
     * groupId      资产组ID。用于关联业务层ID
     * txId         区块链资产发行交易ID，只有资产发行成功后该字段才不为空
     */
    public static class AssetMeta {
        public long assetId;
        public int assetCate;
        public String title;
        public Thumb[] thumb;
        public String shortDesc;
        public String longDesc;
        public JSONArray imgDesc;
        public JSONArray assetUrl;
        public int amount;
        public long price;
        public int status;
        public String assetExt;
        public String createAddr;
        public long groupId;
        public String txId;

        AssetMeta (long assetId, int assetCate, String title, Thumb[] thumb, String shortDesc, String longDesc, JSONArray imgDesc, JSONArray assetUrl, int amount, long price, int status, String assetExt, String createAddr, long groupId, String txId) {
            this.assetId = assetId;
            this.assetCate = assetCate;
            this.title = title;
            this.thumb = thumb;
            this.shortDesc = shortDesc;
            this.longDesc = longDesc;
            this.imgDesc = imgDesc;
            this.assetUrl = assetUrl;
            this.amount = amount;
            this.price = price;
            this.status = status;
            this.assetExt = assetExt;
            this.createAddr = createAddr;
            this.groupId = groupId;
            this.txId = txId;
        }
    }

    /**
     * 缩略图数据结构
     *
     * urls 缩略图链接
     * width 原图宽
     * height 原图高
     */
    public static class Thumb {
        public Urls urls;
        public String width;
        public String height;

        Thumb (Urls urls, String width, String height) {
            this.urls = urls;
            this.width = width;
            this.height = height;
        }
    }

    /**
     * 缩略图链接数据结构
     *
     * icon 宽为 60 的等比缩放缩略图
     * url1 宽为 140 的等比缩放缩略图
     * url2 宽为 360 的等比缩放缩略图
     * url3 宽为 850 的等比缩放缩略图
     */
    public static class Urls {
        public String icon;
        public String url1;
        public String url2;
        public String url3;

        Urls (String icon, String url1, String url2, String url3) {
            this.icon = icon;
            this.url1 = url1;
            this.url2 = url2;
            this.url3= url3;
        }
    }

    /**
     * 授予数字资产碎片返回值
     *
     * requestId    请求id
     * errNo        错误码
     * errMsg       错误信息
     * assetId      数字资产id
     * shardId      数字资产碎片id
     */
    public static class GrantShardResp {
        public long requestId;
        public int errNo;
        public String errMsg;
        public long assetId;
        public long shardId;

        GrantShardResp(long requestId, int errNo, String errMsg, long assetId, long shardId) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.assetId = assetId;
            this.shardId = shardId;
        }
    }

    /**
     * 查询数字资产碎片详情返回值
     *
     * requestId    请求id
     * errNo        错误码
     * errMsg       错误信息
     * meta         数字资产碎片详情
     */
    public static class QueryShardsResp {
        public long requestId;
        public int errNo;
        public String errMsg;
        public ShardMeta meta;

        QueryShardsResp(long requestId, int errNo, String errMsg, ShardMeta meta) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.meta = meta;
        }
    }

    /**
     * 数字资产碎片详情数据结构
     *
     * assetId      资产ID
     * shardId      资产碎片ID
     * ownerAddr    资产碎片拥有者区块链账户地址
     * uid          业务传递的关联ID
     * price        资产碎片交易价格，单位为分
     * status       资产碎片状态。1:授予中 4:转移中 5: 核销中 6: 已核销 10:异常
     * txId         资产碎片上链交易ID
     * assetInfo    碎片资产信息
     * ctime        资产碎片创建时间
     */
    public static class ShardMeta {
        public long assetId;
        public long shardId;
        public String ownerAddr;
        public long uid;
        public long price;
        public int status;
        public String txId;
        public ShardAssetInfo assetInfo;
        public long ctime;

        ShardMeta (long assetId, long shardId, String ownerAddr, long uid, long price, int status, String txId, ShardAssetInfo assetInfo, long ctime) {
            this.assetId = assetId;
            this.shardId = shardId;
            this.ownerAddr = ownerAddr;
            this.uid = uid;
            this.price = price;
            this.status = status;
            this.txId = txId;
            this.assetInfo = assetInfo;
            this.ctime = ctime;
        }
    }

    /**
     * 碎片资产信息数据结构
     *
     * title        资产标题
     * assetCate    资产类别
     * thumb        资产缩略图
     * shortDesc    短文本描述
     * createAddr   资产创建者区块链地址
     * groupId      资产组ID
     */
    public static class ShardAssetInfo {
        public String title;
        public int assetCate;
        public Thumb[] thumb;
        public String shortDesc;
        public String createAddr;
        public long groupId;

        ShardAssetInfo (String title, int assetCate, Thumb[] thumb, String shortDesc, String createAddr, long groupId) {
            this.title = title;
            this.assetCate = assetCate;
            this.thumb = thumb;
            this.shortDesc = shortDesc;
            this.createAddr = createAddr;
            this.groupId = groupId;
        }
    }

    /**
     * 历史登记记录数据结构
     *
     * assetId  资产ID
     * type     资产操作类型。1：资产创造 2：碎片授予 3：碎片转移
     * shardId  资产碎片ID
     * price    资产操作价格，单位为分
     * txId     资产交易上链ID
     * from     资产操作发起者地址，type=1时为空字符串
     * to       资产操作目的者地址
     * ctime    资产操作时间
     */
    public static class History {
        long assetId;
        int type;
        long shardId;
        long price;
        String txId;
        String from;
        String to;
        long ctime;

        History (long assetId, int type, long shardId, long price, String txId, String from, String to, long ctime) {
            this.assetId = assetId;
            this.type = type;
            this.shardId = shardId;
            this.price = price;
            this.txId = txId;
            this.from = from;
            this.to = to;
            this.ctime = ctime;
        }
    }

    /**
     * 获取资产存证信息返回值
     *
     * requestId    请求id
     * errNo        错误码
     * errMsg       错误信息
     * createAddr   数字资产创建地址
     * txId         数字资产存证交易id
     * assetInfo    数字资产存证信息
     * cTime        数字资产存证时间
     */
    public static class GetEvidenceInfoResp {
        public long requestId;
        public int errNo;
        public String errMsg;
        public String createAddr;
        public String txId;
        public JSONObject assetInfo;
        public long cTime;

        GetEvidenceInfoResp(long requestId, int errNo, String errMsg, String createAddr, String txId,
                            JSONObject assetInfo, long cTime) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.createAddr = createAddr;
            this.txId = txId;
            this.assetInfo = assetInfo;
            this.cTime = cTime;
        }
    }
}
