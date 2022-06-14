package com.baidu.xasset.client.xasset;

import com.alibaba.fastjson.JSONObject;
import com.baidu.xasset.auth.Crypto;
import com.baidu.xasset.client.base.Base;
import com.baidu.xasset.client.base.BaseDef;
import com.baidu.xasset.client.base.BaseDef.*;
import com.baidu.xasset.client.xasset.XassetDef.*;
import com.baidu.xasset.common.config.Config.XassetCliConfig;
import com.baidu.xasset.utils.Utils;
import com.baidu.xuper.api.Account;
import com.baidubce.auth.DefaultBceSessionCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 数字资产平台登记SaaS
 * 支持创建数字资产、发行数字资产、查询数字资产等功能。
 */
public class Asset {
    Base xassetBaseClient;

    /**
     * 构建登记SaaS服务
     *
     * @param cfg    数字资产平台配置
     * @param logger 日志记录器
     */
    public Asset(XassetCliConfig cfg, java.util.logging.Logger logger) {
        this.xassetBaseClient = new Base(cfg, logger);
    }

    /**
     * 通过手百小程序注册链上账户（接口内部会帮助进行SK加解密）
     *
     * @param open_id 手百小程序openid
     * @param app_key 手百小程序app_key
     * @return {@link Resp}<{@link BdBoxRegisterResp}
     */
    public Resp<BdBoxRegisterResp> bdboxRegister(final String open_id, final String app_key) {
        // 参数校验
        if ("".equals(open_id) || "".equals(app_key)) {
            Base.logger.warning("bdbox register param invalid");
            return null;
        }

        // 使用 账户sk 加密 open_id & app_key
        String sk = Base.getConfig().Credentials.SecreteAccessKey;
        String encryptOpenId = Utils.encrypt(sk, open_id);
        String encryptAppKey = Utils.encrypt(sk, app_key);

        // 设置请求参数
        Map<String, String> body = new HashMap<String, String>() {
            {
                put("open_id", encryptOpenId);
                put("app_key", encryptAppKey);
            }
        };

        // 发送请求
        RequestRes res;
        try {
            res = Base.post(Api.BDBOXREGISTER, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("bdbox register failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        // 使用 账户sk 解密区块链账户助记词
        String mnemonic = Utils.decrypt(sk, obj.getString("mnemonic"));
        BdBoxRegisterResp resp = new BdBoxRegisterResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getString("address"), mnemonic, obj.getInteger("is_new"));

        Base.logger.info(String.format("bdbox register succ.[address:%s] [mnemonic:%s] [is_new:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.address, resp.mnemonic, resp.isNew, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }


    /**
     * 第三方应用绑定链上账户（接口内部会帮助进行SK加解密）
     *
     * @param union_id 第三方应用通过OAuth获取到的union_id
     * @param mnemonic 待绑定区块链账户助记词
     * @return {@link Resp}<{@link BaseResp}
     */
    public Resp<BaseResp> bindByUnionId(final String union_id, final String mnemonic) {
        // 参数校验
        if ("".equals(union_id) || "".equals(mnemonic)) {
            Base.logger.warning("bind by union_id param invalid");
            return null;
        }

        // 使用 账户sk 加密 uniond_id & mnemonic
        String sk = Base.getConfig().Credentials.SecreteAccessKey;
        String encryptUnionId = Utils.encrypt(sk, union_id);
        String encryptMnemonic = Utils.encrypt(sk, mnemonic);

        // 设置请求参数
        Map<String, String> body = new HashMap<String, String>() {
            {
                put("union_id", encryptUnionId);
                put("mnemonic", encryptMnemonic);
            }
        };

        // 发送请求
        RequestRes res;
        try {
            res = Base.post(Api.BINDBYUNIONID, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("bind by union_id failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        BaseResp resp = new BaseResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"));
        Base.logger.info(String.format("bind by union_id succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 获取访问BOS临时STS资源
     *
     * @param account 创建资产区块链账户
     * @return {@link Resp}<{@link GetStokenResp}>
     */
    public Resp<GetStokenResp> getStoken(final Account account) {
        if (account == null) {
            Base.logger.warning("get stoken param invalid");
            return null;
        }

        // sign
        final long nonce = Utils.genNonce();
        String signMsg = String.format("%d", nonce);
        String rawSign;
        try {
            rawSign = Crypto.xassetSignECDSA(account, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("account esdsa sign failed" + e);
            return null;
        }

        final String sign = rawSign;
        Map<String, String> body = new HashMap<String, String>() {
            {
                put("addr", account.getAKAddress());
                put("sign", sign);
                put("pkey", account.getKeyPair().getJSONPublicKey());
                put("nonce", String.format("%d", nonce));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.GETSTOKEN, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("get stoken failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        GetStokenResp resp = new GetStokenResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getJSONObject("accessInfo"));

        Base.logger.info(String.format("get stoken succ.[assetInfo:%s] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.accessInfo, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * BOS上传文件
     *
     * @param account  创建资产区块链账户
     * @param fileName 文件名称
     * @param filePath 文件路径
     * @param dataByte 文件二进制串
     * @param property 文件属性 例如图片类型，则为图片宽高，格式为 width_height（可选）
     * @return {@link UploadFile}
     * <p>
     * 注意：文件路径和文件二进制串二选一，默认文件路径
     */
    public UploadFile uploadFile(final Account account, String fileName, String filePath, byte[] dataByte, String property) {
        if (account == null || ("".equals(filePath) && dataByte == null) || "".equals(fileName)) {
            Base.logger.warning("upload file param invalid");
            return null;
        }

        Resp<GetStokenResp> result = this.getStoken(account);

        GetStokenResp getStoken = result.apiResp;
        JSONObject accessInfo = getStoken.accessInfo;
        String bucketName = accessInfo.getString("bucket");
        String ak = accessInfo.getString("access_key_id");
        String sk = accessInfo.getString("secret_access_key");
        String sessionToken = accessInfo.getString("session_token");
        String endPoint = accessInfo.getString("endpoint");
        String objectPath = accessInfo.getString("object_path");

        BosClientConfiguration config = new BosClientConfiguration();
        config.setEnableHttpAsyncPut(false);
        config.setCredentials(new DefaultBceSessionCredentials(ak, sk, sessionToken));
        config.setEndpoint(endPoint);
        BosClient client = new BosClient(config);

        String key = String.format("/%s%s", objectPath, fileName);

        if (!"".equals(filePath)) {
            File file = new File(filePath);
            client.putObject(bucketName, key, file);
        } else if (dataByte.length > 1) {
            client.putObject(bucketName, key, dataByte);
        } else {
            Base.logger.warning("unsupported upload file method");
            client.shutdown();
            return new UploadFile(null, getStoken, result.res);
        }
        client.shutdown();

        String link = String.format("bos_v1://%s%s/%s", bucketName, key, property);
        Base.logger.info(String.format("upload file succ [link:%s]", link));

        return new UploadFile(link, getStoken, result.res);
    }

    /**
     * 创建数字资产
     *
     * @param account   创建资产区块链账户
     * @param amount    数字资产数量。0：无限授予碎片
     * @param assetInfo 数字资产信息
     * @param userId    业务侧用户id（可选）
     * @param price     资产价格（可选）
     * @return {@link Resp}<{@link CreateAssetResp}>
     */
    public Resp<CreateAssetResp> createAsset(final Account account, final long amount, AssetInfo assetInfo, final long userId, final long price) {
        // check param
        if (account == null || amount < 0 || assetInfo == null || assetInfo.assetCate < XassetDef.ASSETCATEART || "".equals(assetInfo.title) || assetInfo.thumb == null || "".equals(assetInfo.shortDesc)) {
            Base.logger.warning("create asset param is invalid");
            return null;
        }

        // sign
        final long nonce = Utils.genNonce();
        final long assetId = Utils.genAssetId(Base.getConfig().Credentials.AppId);
        String signMsg = String.format("%d%d", assetId, nonce);
        String rawSign;
        try {
            rawSign = Crypto.xassetSignECDSA(account, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("account esdsa sign failed" + e);
            return null;
        }
        final String sign = rawSign;

        final String finalAssetInfo = JSONObject.toJSONString(assetInfo);
        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
                put("amount", String.format("%d", amount));
                put("asset_info", finalAssetInfo);
                put("addr", account.getAKAddress());
                put("sign", sign);
                put("pkey", account.getKeyPair().getJSONPublicKey());
                put("nonce", String.format("%d", nonce));
                put("user_id", String.format("%d", userId));
                put("price", String.format("%d", price));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.CREATEASSET, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("create asset failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        CreateAssetResp resp = new CreateAssetResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getLong("asset_id"));

        Base.logger.info(String.format("create asset succ.[asset_id:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                assetId, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 修改未发行的数字资产
     *
     * @param account   创建资产区块链账户
     * @param assetId   资产id
     * @param amount    资产数量。-1：不修改数量 0：无限授予碎片
     * @param assetInfo 资产信息
     * @param price     资产价格（可选），不修改原价格请将此值设置为 -1
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> alterAsset(final Account account, final long assetId, final long amount, AssetInfo assetInfo, final long price) {
        if (account == null || assetId < 1 || (assetInfo == null && amount < -1)) {
            Base.logger.warning("alter asset param is invalid");
            return null;
        }

        // sign
        final long nonce = Utils.genNonce();
        String signMsg = String.format("%d%d", assetId, nonce);
        String rawSign;
        try {
            rawSign = Crypto.xassetSignECDSA(account, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("account esdsa sign failed" + e);
            return null;
        }

        final String sign = rawSign;
        String rawAssetInfo = null;
        if (assetInfo != null) {
            rawAssetInfo = JSONObject.toJSONString(assetInfo);
        }
        final String finalAssetInfo = rawAssetInfo;
        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
                put("amount", String.format("%d", amount));
                put("asset_info", (finalAssetInfo == null) ? "" : finalAssetInfo);
                put("addr", account.getAKAddress());
                put("sign", sign);
                put("pkey", account.getKeyPair().getJSONPublicKey());
                put("nonce", String.format("%d", nonce));
                put("price", String.format("%d", price));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.ALTERASSET, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("alter asset failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        BaseResp resp = new BaseResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"));

        Base.logger.info(String.format("alter asset succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 链上发行数字资产
     *
     * @param account    创建资产区块链账户
     * @param assetId    资产id
     * @param isEvidence 是否存证。0：不存证 1：普通存证。默认 0（可选）
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> publishAsset(final Account account, final long assetId, final int isEvidence) {
        if (account == null || assetId < 1 || isEvidence < 0 || isEvidence > 1) {
            Base.logger.warning("publish asset param is invalid");
            return null;
        }

        // sign
        final long nonce = Utils.genNonce();
        String signMsg = String.format("%d%d", assetId, nonce);
        String rawSign;
        try {
            rawSign = Crypto.xassetSignECDSA(account, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("account esdsa sign failed" + e);
            return null;
        }

        final String sign = rawSign;
        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
                put("addr", account.getAKAddress());
                put("sign", sign);
                put("pkey", account.getKeyPair().getJSONPublicKey());
                put("nonce", String.format("%d", nonce));
                put("is_evidence", String.format("%d", isEvidence));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.PUBLISHASSET, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("publish asset failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        BaseResp resp = new BaseResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"));

        Base.logger.info(String.format("publish asset succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 链上冻结数字资产
     *
     * @param account    创建资产区块链账户
     * @param assetId    资产id
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> freezeAsset(final long assetId, final Account account) {
        if (assetId < 1 || account == null) {
            Base.logger.warning("freeze asset param is invalid");
            return null;
        }

        // sign
        final long nonce = Utils.genNonce();
        String signMsg = String.format("%d%d", assetId, nonce);
        String rawSign;
        try {
            rawSign = Crypto.xassetSignECDSA(account, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("account esdsa sign failed" + e);
            return null;
        }

        final String sign = rawSign;
        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
                put("addr", account.getAKAddress());
                put("sign", sign);
                put("pkey", account.getKeyPair().getJSONPublicKey());
                put("nonce", String.format("%d", nonce));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.FREEZEASSET, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("freeze asset failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        BaseResp resp = new BaseResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"));

        Base.logger.info(String.format("freeze asset succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 查询数字资产详情
     *
     * @param assetId 资产id
     * @return {@link Resp}<{@link QueryAssetResp}>
     */
    public Resp<QueryAssetResp> queryAsset(final long assetId) {
        if (assetId < 1) {
            Base.logger.warning("query asset param is invalid");
            return null;
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.QUERYASSET, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("query asset failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        QueryAssetResp resp = new QueryAssetResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getJSONObject("meta"));

        Base.logger.info(String.format("query asset succ.[meta:%s] [url:%s] [request_id:%s] [trace_id:%s]", resp.meta,
                res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 拉取账户创造资产列表
     *
     * @param addr   要拉取的区块链账户地址
     * @param page   要拉取页数，第一页为1
     * @param limit  每页拉取数量，默认20，最大50（可选）
     * @param status 资产状态。0：全部 1：初试 3：发行中 4：发行成功。默认 0（可选）
     * @return {@link Resp}<{@link ListPageResp}>
     */
    public Resp<ListPageResp> listAssetsByAddr(final int status, final String addr, final int page, final int limit) {
        if ("".equals(addr) || page < 1 || limit < 0 || limit > BaseDef.MAXLIMIT) {
            Base.logger.warning("list assets by addr param is invalid");
            return null;
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("addr", addr);
                put("page", String.format("%d", page));
                put("limit", String.format("%d", limit));
                put("status", String.format("%d", status));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.LISTASSETBYADDR, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("list assets by addr failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        ListPageResp resp = new ListPageResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getJSONArray("list"), obj.getInteger("total_cnt"));

        Base.logger.info(String.format(
                "list assets by addr succ.[list:%s] [total_cnt:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.list, resp.totalCnt, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 授予数字资产碎片
     *
     * @param account  创建资产区块链账户
     * @param assetId  资产id
     * @param toAddr   资产接收者区块链账户地址
     * @param toUserId 资产接收者用户id（可选）
     * @param shardId  碎片id（可选）
     * @param price    碎片价格（可选）
     * @return {@link Resp}<{@link GrantShardResp}>
     */
    public Resp<GrantShardResp> grantShard(final Account account, final long assetId, long shardId, final String toAddr, final long toUserId, final long price) {
        if (account == null || assetId < 1 || "".equals(toAddr)) {
            Base.logger.warning("grant shard param is invalid");
            return null;
        }

        // sign
        final long nonce = Utils.genNonce();
        if (shardId < 1) {
            shardId = Utils.genNonce();
        }
        final long finalShardId = shardId;
        String signMsg = String.format("%d%d", assetId, nonce);
        String rawSign;
        try {
            rawSign = Crypto.xassetSignECDSA(account, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("account ecdsa sign failed" + e);
            return null;
        }

        final String sign = rawSign;
        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
                put("shard_id", String.format("%d", finalShardId));
                put("addr", account.getAKAddress());
                put("sign", sign);
                put("pkey", account.getKeyPair().getJSONPublicKey());
                put("nonce", String.format("%d", nonce));
                put("to_addr", toAddr);
                put("to_userid", String.format("%d", toUserId));
                put("price", String.format("%d", price));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.GRANTSHARD, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("grant shard failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        GrantShardResp resp = new GrantShardResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getLong("asset_id"), obj.getLong("shard_id"));

        Base.logger.info(String.format(
                "grant shard succ.[asset_id:%s] [shard_id:%s] [to_addr:%s] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.assetId, resp.shardId, toAddr, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 转移数字资产碎片
     *
     * @param account  资产拥有者区块链账户
     * @param assetId  资产id
     * @param shardId  碎片id
     * @param toAddr   资产接收者区块链地址
     * @param toUserId 资产接收者用户id（可选）
     * @param price    碎片价格（可选）
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> transferShard(final Account account, final long assetId, final long shardId, final String toAddr, final long toUserId, final long price) {
        if (account == null || assetId < 1 || shardId < 1 || "".equals(toAddr)) {
            Base.logger.warning("transfer shard param is invalid");
            return null;
        }

        // sign
        final long nonce = Utils.genNonce();
        String signMsg = String.format("%d%d", assetId, nonce);
        String rawSign;
        try {
            rawSign = Crypto.xassetSignECDSA(account, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("account ecdsa sign failed" + e);
            return null;
        }

        final String sign = rawSign;
        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
                put("shard_id", String.format("%d", shardId));
                put("addr", account.getAKAddress());
                put("sign", sign);
                put("pkey", account.getKeyPair().getJSONPublicKey());
                put("nonce", String.format("%d", nonce));
                put("to_addr", toAddr);
                put("to_userid", String.format("%d", toUserId));
                put("price", String.format("%d", price));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.TRANASFERSHARD, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("transfer shard failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        BaseResp resp = new BaseResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"));

        Base.logger.info(String.format("transfer shard succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 核销数字资产碎片
     *
     * @param cAccount  资产创建者区块链账户
     * @param uAccount 资产碎片拥有者账户
     * @param assetId  资产id
     * @param shardId  碎片id
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> consumeShard(final Account cAccount, final Account uAccount, final long assetId, final long shardId) {
        if (cAccount == null || uAccount == null || assetId < 1 || shardId < 1) {
            Base.logger.warning("consume shard param is invalid");
            return null;
        }

        // sign
        final long nonce = Utils.genNonce();
        String signMsg = String.format("%d%d", assetId, nonce);
        String rawCSign;
        String rawUSign;
        try {
            rawCSign = Crypto.xassetSignECDSA(cAccount, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("cAccount ecdsa sign failed" + e);
            return null;
        }

        try {
            rawUSign = Crypto.xassetSignECDSA(uAccount, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("uAccount ecdsa sign failed" + e);
            return null;
        }

        final String cSign = rawCSign;
        final String uSign = rawUSign;
        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
                put("shard_id", String.format("%d", shardId));
                put("addr", cAccount.getAKAddress());
                put("sign", cSign);
                put("pkey", cAccount.getKeyPair().getJSONPublicKey());
                put("user_addr", uAccount.getAKAddress());
                put("user_sign", uSign);
                put("user_pkey", uAccount.getKeyPair().getJSONPublicKey());
                put("nonce", String.format("%d", nonce));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.CONSUMESHARD, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger
                    .warning(String.format("consume shard failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                            res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        BaseResp resp = new BaseResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"));

        Base.logger.info(String.format("consume shard succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 查询指定资产碎片信息
     *
     * @param assetId 资产id
     * @param shardId 碎片id
     * @return {@link Resp}<{@link QueryShardsResp}>
     */
    public Resp<QueryShardsResp> queryShards(final long assetId, final long shardId) {
        if (assetId < 1 || shardId < 1) {
            Base.logger.warning("query shards param is invalid");
            return null;
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
                put("shard_id", String.format("%d", shardId));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.QUERYSHARDS, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("query shard failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        QueryShardsResp resp = new QueryShardsResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getJSONObject("meta"));

        Base.logger.info(String.format("query shards succ.[meta:%s] [url:%s] [request_id:%s] [trace_id:%s]", resp.meta,
                res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 分页拉取指定账户持有碎片列表
     *
     * @param addr  要拉取的区块链账户地址
     * @param page  要拉取页数，第一页为1
     * @param limit 每页拉取数量，默认20，最大50（可选）
     * @return {@link Resp}<{@link ListPageResp}>
     */
    public Resp<ListPageResp> listShardsAddr(final String addr, final int page, final int limit) {
        if ("".equals(addr) || page < 1 || limit < 1 || limit > BaseDef.MAXLIMIT) {
            Base.logger.warning("list shards by addr param is invalid");
            return null;
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("addr", addr);
                put("page", String.format("%d", page));
                put("limit", String.format("%d", limit));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.LISTSHARDSBYADDR, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("list shards by addr failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        ListPageResp resp = new ListPageResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getJSONArray("list"), obj.getInteger("total_cnt"));

        Base.logger.info(String.format(
                "list shards by addr succ.[list:%s] [total_cnt:%d] [url:%s] [request_id:%s] [trace_id:%s]", resp.list,
                resp.totalCnt, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 分页拉取指定资产已授予碎片列表
     *
     * @param assetId 资产id
     * @param cursor  分页游标，首页设置空字符串（可选）
     * @param limit   每页拉取数量，默认20，最多50（可选）
     * @return {@link Resp}<{@link ListCursorResp}>
     */
    public Resp<ListCursorResp> listShardsByAsset(final long assetId, final String cursor, final int limit) {
        if (assetId < 1 || limit < 1 || limit > BaseDef.MAXLIMIT) {
            Base.logger.warning("list shards by asset param is invalid");
            return null;
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
                put("cursor", cursor);
                put("limit", String.format("%d", limit));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.LISTSHARDSBYASSET, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("list shards by asset failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        ListCursorResp resp = new ListCursorResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getJSONArray("list"), obj.getInteger("has_more"), obj.getString("cursor"));

        Base.logger.info(String.format(
                "list shards by asset succ.[list:%s] [cursor:%s] [has_more:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.list, resp.cursor, resp.hasMore, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 拉取数字资产历史登记记录
     *
     * @param assetId 资产id
     * @param page    要拉取页面，第一页为1
     * @param limit   每页拉取数量，默认20，最多50（可选）
     * @return {@link Resp}<{@link ListPageResp}>
     */
    public Resp<ListPageResp> history(final long assetId, final int page, final int limit) {
        if (assetId < 1 || page < 1 || limit < 1 || limit > BaseDef.MAXLIMIT) {
            Base.logger.warning("history param is invalid");
            return null;
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
                put("page", String.format("%d", page));
                put("limit", String.format("%d", limit));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.HISTORYASSET, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("history failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        ListPageResp resp = new ListPageResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getJSONArray("list"), obj.getInteger("total_cnt"));

        Base.logger.info(String.format("history succ.[list:%s] [total_cnt:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.list, resp.totalCnt, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 获取资产存证信息
     *
     * @param assetId 资产id
     * @return {@link Resp}<{@link GetEvidenceInfoResp}>
     */
    public Resp<GetEvidenceInfoResp> getEvidenceInfo(final long assetId) {
        if (assetId < 1) {
            Base.logger.warning("get evidence info param is invalid");
            return null;
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.GETEVIDENCEINFO, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLong("request_id");
        int errNo = obj.getIntValue("errno");
        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("get evidence info failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            return null;
        }

        GetEvidenceInfoResp resp = new GetEvidenceInfoResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getString("create_addr"), obj.getString("tx_id"), obj.getJSONObject("asset_info"), obj.getLong("ctime"));

        Base.logger.info(String.format(
                "get evidence info succ.[create_addr:%s] [tx_id:%s] [asset_info:%s] [ctime:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.createAddr, resp.txId, resp.assetInfo, resp.cTime, res.reqUrl, resp.requestId,
                res.traceId));
        return new Resp<>(resp, res);
    }
}

