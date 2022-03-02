package com.baidu.xasset.client.xasset;

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
        public JSONObject accessInfo;

        GetStokenResp(long requestId, int errNo, String errMsg, JSONObject accessInfo) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.accessInfo = accessInfo;
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
        public JSONObject meta;

        QueryAssetResp(long requestId, int errNo, String errMsg, JSONObject meta) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.meta = meta;
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
        public JSONObject meta;

        QueryShardsResp(long requestId, int errNo, String errMsg, JSONObject meta) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.meta = meta;
        }
    }

    /**
     * 查询数字资产流通量返回值
     *
     * requestId    请求id
     * errNo        错误码
     * errMsg       错误信息
     * amount       数字资产流通量
     */
    public static class SrdsCirResp {
        public long requestId;
        public int errNo;
        public String errMsg;
        public long amount;

        public SrdsCirResp(long requestId, int errNo, String errMsg, long amount) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.amount = amount;
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
