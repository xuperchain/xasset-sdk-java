package com.baidu.xasset.client.xasset;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
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

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 【身份管理】通过手百小程序注册链上账户（接口内部会帮助进行SK加解密）
     *
     * @param openId 手百小程序openid
     * @param appKey 手百小程序app_key
     * @return {@link Resp}<{@link BdBoxRegisterResp}
     */
    public Resp<BdBoxRegisterResp> bdboxRegister(final String openId, final String appKey) throws BaseException {
        // 参数校验
        if (isNullOrEmpty(openId) || isNullOrEmpty(appKey)) {
            Base.logger.warning("bdbox register param invalid");
            throw new BaseException("param error");
        }

        // 使用 账户sk 加密 open_id & app_key
        String sk = Base.getConfig().Credentials.SecreteAccessKey;
        String encryptOpenId = Utils.encrypt(sk, openId);
        String encryptAppKey = Utils.encrypt(sk, appKey);

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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("bdbox register failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        // 使用 账户sk 解密区块链账户助记词
        String mnemonic = Utils.decrypt(sk, obj.getString("mnemonic"));
        BdBoxRegisterResp resp = new BdBoxRegisterResp(requestId, errNo, errMsg,
                obj.getString("address"), mnemonic, obj.getIntValue("is_new"));

        Base.logger.info(String.format("bdbox register succ.[address:%s] [mnemonic:%s] [is_new:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.address, resp.mnemonic, resp.isNew, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【身份管理】手百小程序绑定已有链上账户（接口内部会帮助进行SK加解密）
     *
     * @param openId   手百小程序openid
     * @param appKey   手百小程序app_key
     * @param mnemonic 待绑定区块链账户助记词
     * @return {@link Resp}<{@link BaseResp}
     */
    public Resp<BaseResp> bdboxBind(final String openId, final String appKey, final String mnemonic) throws BaseException {
        // 参数校验
        if (isNullOrEmpty(openId) || isNullOrEmpty(appKey) || isNullOrEmpty(mnemonic)) {
            Base.logger.warning("bdbox bind param invalid");
            throw new BaseException("param error");
        }

        // 使用 账户sk 加密 open_id & app_key & mnemonic
        String sk = Base.getConfig().Credentials.SecreteAccessKey;
        String encryptOpenId = Utils.encrypt(sk, openId);
        String encryptAppKey = Utils.encrypt(sk, appKey);
        String encryptMnemonic = Utils.encrypt(sk, mnemonic);

        // 设置请求参数
        Map<String, String> body = new HashMap<String, String>() {
            {
                put("open_id", encryptOpenId);
                put("app_key", encryptAppKey);
                put("mnemonic", encryptMnemonic);
            }
        };

        // 发送请求
        RequestRes res;
        try {
            res = Base.post(Api.BDBOXBIND, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("bdbox bind failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        BaseResp resp = new BaseResp(requestId, errNo, errMsg);
        Base.logger.info(String.format("bdbox bind succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【身份管理】第三方应用绑定链上账户（接口内部会帮助进行SK加解密）
     *
     * @param unionId  第三方应用通过OAuth获取到的union_id
     * @param mnemonic 待绑定区块链账户助记词
     * @return {@link Resp}<{@link BaseResp}
     */
    public Resp<BaseResp> bindByUnionId(final String unionId, final String mnemonic) throws BaseException {
        // 参数校验
        if (isNullOrEmpty(unionId) || isNullOrEmpty(mnemonic)) {
            Base.logger.warning("bind by union_id param invalid");
            throw new BaseException("param error");
        }

        // 使用 账户sk 加密 union_id & mnemonic
        String sk = Base.getConfig().Credentials.SecreteAccessKey;
        String encryptUnionId = Utils.encrypt(sk, unionId);
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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("bind by union_id failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        BaseResp resp = new BaseResp(requestId, errNo, errMsg);
        Base.logger.info(String.format("bind by union_id succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【身份管理】第三方应用查询链上绑定账户（接口内部会帮助进行SK加解密）
     *
     * @param unionId  第三方应用通过OAuth获取到的union_id
     * @return {@link Resp}<{@link GetAddrByUnionIdResp}
     */
    public Resp<GetAddrByUnionIdResp> getAddrByUnionId(final String unionId) throws BaseException {
        // 参数校验
        if (isNullOrEmpty(unionId)) {
            Base.logger.warning("get addr by union_id param invalid");
            throw new BaseException("param error");
        }

        // 使用 账户sk 加密 union_id & mnemonic
        String sk = Base.getConfig().Credentials.SecreteAccessKey;
        String encryptUnionId = Utils.encrypt(sk, unionId);

        // 设置请求参数
        Map<String, String> body = new HashMap<String, String>() {
            {
                put("union_id", encryptUnionId);
            }
        };

        // 发送请求
        RequestRes res;
        try {
            res = Base.post(Api.GETADDRBYUNIONID, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("get addr by union_id failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        GetAddrByUnionIdResp resp = new GetAddrByUnionIdResp(requestId, errNo, errMsg, obj.getString("address"));

        Base.logger.info(String.format("get addr by union_id succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【资产登记】获取访问BOS临时STS资源
     *
     * @param account 创建资产区块链账户
     * @return {@link Resp}<{@link GetStokenResp}>
     */
    public Resp<GetStokenResp> getStoken(final Account account) throws BaseException {
        if (account == null) {
            Base.logger.warning("get stoken param invalid");
            throw new BaseException("param error");
        }

        // sign
        final long nonce = Utils.genNonce();
        String signMsg = String.format("%d", nonce);
        String rawSign;
        try {
            rawSign = Crypto.xassetSignECDSA(account, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("account ecdsa sign failed" + e);
            throw new BaseException("ecdsa sign error");
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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("get stoken failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        JSONObject info = obj.getJSONObject("accessInfo");
        AccessInfo accessInfo = new AccessInfo(info.getString("bucket"), info.getString("endpoint"), info.getString("object_path"), info.getString("access_key_id"), info.getString("secret_access_key"), info.getString("session_token"), info.getString("createTime"), info.getString("expiration"));
        GetStokenResp resp = new GetStokenResp(requestId, errNo, errMsg, accessInfo);

        Base.logger.info(String.format("get stoken succ.[assetInfo:%s] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.accessInfo, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【资产登记】BOS上传文件
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
    public UploadFile uploadFile(final Account account, String fileName, String filePath, byte[] dataByte, String property) throws BaseException {
        if (account == null || (isNullOrEmpty(filePath) && dataByte == null) || isNullOrEmpty(fileName)) {
            Base.logger.warning("upload file param invalid");
            throw new BaseException("param error");
        }

        Resp<GetStokenResp> result = this.getStoken(account);

        GetStokenResp getStoken = result.apiResp;
        AccessInfo accessInfo = getStoken.accessInfo;
        String bucketName = accessInfo.bucket;
        String ak = accessInfo.accessKeyId;
        String sk = accessInfo.secreteAccessKey;
        String sessionToken = accessInfo.sessionToken;
        String endPoint = accessInfo.endPoint;
        String objectPath = accessInfo.objectPath;

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
     * 【资产登记】创建数字资产
     *
     * @param account   创建资产区块链账户
     * @param amount    数字资产数量。0：无限授予碎片
     * @param assetInfo 数字资产信息
     * @param userId    业务侧用户id（可选）
     * @param price     资产价格（可选）
     * @return {@link Resp}<{@link CreateAssetResp}>
     */
    public Resp<CreateAssetResp> createAsset(final Account account, final long amount, AssetInfo assetInfo, final long userId, final long price) throws BaseException {
        // check param
        if (account == null || amount < 0 || assetInfo == null || assetInfo.assetCate < XassetDef.ASSETCATEART || isNullOrEmpty(assetInfo.title) || assetInfo.thumb == null || isNullOrEmpty(assetInfo.shortDesc)) {
            Base.logger.warning("create asset param is invalid");
            throw new BaseException("param error");
        }

        // sign
        final long nonce = Utils.genNonce();
        final long assetId = Utils.genAssetId(Base.getConfig().Credentials.AppId);
        String signMsg = String.format("%d%d", assetId, nonce);
        String rawSign;
        try {
            rawSign = Crypto.xassetSignECDSA(account, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("account ecdsa sign failed" + e);
            throw new BaseException("ecdsa sign error");
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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("create asset failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        CreateAssetResp resp = new CreateAssetResp(requestId, errNo, errMsg,
                obj.getLongValue("asset_id"));

        Base.logger.info(String.format("create asset succ.[asset_id:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                assetId, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【资产登记】修改未发行的数字资产
     *
     * @param account   创建资产区块链账户
     * @param assetId   资产id
     * @param amount    资产数量。-1：不修改数量 0：无限授予碎片
     * @param assetInfo 资产信息
     * @param price     资产价格（可选），不修改原价格请将此值设置为 -1
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> alterAsset(final Account account, final long assetId, final long amount, AssetInfo assetInfo, final long price) throws BaseException {
        if (account == null || assetId < 1 || (assetInfo == null && amount < -1)) {
            Base.logger.warning("alter asset param is invalid");
            throw new BaseException("param error");
        }

        // sign
        final long nonce = Utils.genNonce();
        String signMsg = String.format("%d%d", assetId, nonce);
        String rawSign;
        try {
            rawSign = Crypto.xassetSignECDSA(account, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("account ecdsa sign failed" + e);
            throw new BaseException("ecdsa sign error");
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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("alter asset failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        BaseResp resp = new BaseResp(requestId, errNo, errMsg);

        Base.logger.info(String.format("alter asset succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【资产登记】链上发行数字资产
     *
     * @param account    创建资产区块链账户
     * @param assetId    资产id
     * @param isEvidence 是否存证。0：不存证 1：普通存证。默认 0（可选）
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> publishAsset(final Account account, final long assetId, final int isEvidence) throws BaseException {
        if (account == null || assetId < 1 || isEvidence < 0 || isEvidence > 1) {
            Base.logger.warning("publish asset param is invalid");
            throw new BaseException("param error");
        }

        // sign
        final long nonce = Utils.genNonce();
        String signMsg = String.format("%d%d", assetId, nonce);
        String rawSign;
        try {
            rawSign = Crypto.xassetSignECDSA(account, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("account ecdsa sign failed" + e);
            throw new BaseException("ecdsa sign error");
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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("publish asset failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        BaseResp resp = new BaseResp(requestId, errNo, errMsg);

        Base.logger.info(String.format("publish asset succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【资产冻结】链上冻结数字资产
     *
     * @param account 创建资产区块链账户
     * @param assetId 资产id
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> freezeAsset(final long assetId, final Account account) throws BaseException {
        if (assetId < 1 || account == null) {
            Base.logger.warning("freeze asset param is invalid");
            throw new BaseException("param error");
        }

        // sign
        final long nonce = Utils.genNonce();
        String signMsg = String.format("%d%d", assetId, nonce);
        String rawSign;
        try {
            rawSign = Crypto.xassetSignECDSA(account, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("account ecdsa sign failed" + e);
            throw new BaseException("ecdsa sign error");
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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("freeze asset failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        BaseResp resp = new BaseResp(requestId, errNo, errMsg);

        Base.logger.info(String.format("freeze asset succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【资产查询】查询数字资产详情
     *
     * @param assetId 资产id
     * @return {@link Resp}<{@link QueryAssetResp}>
     */
    public Resp<QueryAssetResp> queryAsset(final long assetId) throws BaseException {
        if (assetId < 1) {
            Base.logger.warning("query asset param is invalid");
            throw new BaseException("param error");
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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("query asset failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        JSONObject meta = obj.getJSONObject("meta");
        JSONArray rawThumb = meta.getJSONArray("thumb");

        Thumb[] thumb = new Thumb[rawThumb.size()];
        for (int i = 0; i < rawThumb.size(); i++) {
            JSONObject objThumb = (JSONObject) rawThumb.get(i);
            JSONObject objUrls = objThumb.getJSONObject("urls");
            Urls urls = new Urls(objUrls.getString("icon"), objUrls.getString("url1"), objUrls.getString("url2"),objUrls.getString("url3"));
            thumb[i] = new Thumb(urls, objThumb.getString("width"), objThumb.getString("height"));
        }

        AssetMeta assetMeta = new AssetMeta(meta.getLongValue("asset_id"), meta.getIntValue("asset_cate"), meta.getString("title"), thumb, meta.getString("short_desc"), meta.getString("long_desc"), meta.getJSONArray("img_desc"), meta.getJSONArray("asset_url"), meta.getIntValue("amount"), meta.getLongValue("price"), meta.getIntValue("status"), meta.getString("asset_ext"), meta.getString("create_addr"), meta.getLongValue("group_id"), meta.getString("tx_id"));

        QueryAssetResp resp = new QueryAssetResp(requestId, errNo, errMsg, assetMeta);

        Base.logger.info(String.format("query asset succ.[meta:%s] [url:%s] [request_id:%s] [trace_id:%s]", resp.meta,
                res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【资产查询】拉取账户创造资产列表
     *
     * @param addr   要拉取的区块链账户地址
     * @param page   要拉取页数，第一页为1
     * @param limit  每页拉取数量，默认20，最大50（可选）
     * @param status 资产状态。0：全部 1：初试 3：发行中 4：发行成功。默认 0（可选）
     * @return {@link Resp}<{@link ListPageResp}>
     */
    public Resp<ListPageResp> listAssetsByAddr(final int status, final String addr, final int page, final int limit) throws BaseException {
        if (isNullOrEmpty(addr) || page < 1 || limit < 0 || limit > BaseDef.MAXLIMIT) {
            Base.logger.warning("list assets by addr param is invalid");
            throw new BaseException("param error");
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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("list assets by addr failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        JSONArray rawList = obj.getJSONArray("list");
        AssetMeta[] list = new AssetMeta[rawList.size()];
        for (int i = 0; i < rawList.size(); i++) {
            JSONObject meta = (JSONObject) rawList.get(i);
            JSONArray rawThumb = meta.getJSONArray("thumb");
            Thumb[] thumb = new Thumb[rawThumb.size()];
            for (int j = 0; j < rawThumb.size(); j++) {
                JSONObject objThumb = (JSONObject) rawThumb.get(j);
                JSONObject objUrls = objThumb.getJSONObject("urls");
                Urls urls = new Urls(objUrls.getString("icon"), objUrls.getString("url1"), objUrls.getString("url2"),objUrls.getString("url3"));
                thumb[j] = new Thumb(urls, objThumb.getString("width"), objThumb.getString("height"));
            }

            list[i] = new AssetMeta(meta.getLongValue("asset_id"), meta.getIntValue("asset_cate"), meta.getString("title"), thumb, meta.getString("short_desc"), meta.getString("long_desc"), meta.getJSONArray("img_desc"), meta.getJSONArray("asset_url"), meta.getIntValue("amount"), meta.getLongValue("price"), meta.getIntValue("status"), meta.getString("asset_ext"), meta.getString("create_addr"), meta.getLongValue("group_id"), meta.getString("tx_id"));
        }

        ListPageResp resp = new ListPageResp(requestId, errNo, errMsg, list, obj.getIntValue("total_cnt"));

        Base.logger.info(String.format(
                "list assets by addr succ.[list:%s] [total_cnt:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.list, resp.totalCnt, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【碎片授予】授予数字资产碎片
     *
     * @param account  创建资产区块链账户
     * @param assetId  资产id
     * @param toAddr   资产接收者区块链账户地址
     * @param toUserId 资产接收者用户id（可选）
     * @param shardId  碎片id（可选）
     * @param price    碎片价格（可选）
     * @return {@link Resp}<{@link GrantShardResp}>
     */
    public Resp<GrantShardResp> grantShard(final Account account, final long assetId, long shardId, final String toAddr, final long toUserId, final long price) throws BaseException {
        if (account == null || assetId < 1 || isNullOrEmpty(toAddr)) {
            Base.logger.warning("grant shard param is invalid");
            throw new BaseException("param error");
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
            throw new BaseException("ecdsa sign error");
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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("grant shard failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        GrantShardResp resp = new GrantShardResp(requestId, errNo, errMsg,
                obj.getLongValue("asset_id"), obj.getLongValue("shard_id"));

        Base.logger.info(String.format(
                "grant shard succ.[asset_id:%s] [shard_id:%s] [to_addr:%s] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.assetId, resp.shardId, toAddr, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【碎片转移】转移数字资产碎片
     *
     * @param account  资产拥有者区块链账户
     * @param assetId  资产id
     * @param shardId  碎片id
     * @param toAddr   资产接收者区块链地址
     * @param toUserId 资产接收者用户id（可选）
     * @param price    碎片价格（可选）
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> transferShard(final Account account, final long assetId, final long shardId, final String toAddr, final long toUserId, final long price) throws BaseException {
        if (account == null || assetId < 1 || shardId < 1 || isNullOrEmpty(toAddr)) {
            Base.logger.warning("transfer shard param is invalid");
            throw new BaseException("param error");
        }

        // sign
        final long nonce = Utils.genNonce();
        String signMsg = String.format("%d%d", assetId, nonce);
        String rawSign;
        try {
            rawSign = Crypto.xassetSignECDSA(account, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("account ecdsa sign failed" + e);
            throw new BaseException("ecdsa sign error");
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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("transfer shard failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        BaseResp resp = new BaseResp(requestId, errNo, errMsg);

        Base.logger.info(String.format("transfer shard succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【碎片核销】核销数字资产碎片
     *
     * @param cAccount 资产创建者区块链账户
     * @param uAccount 资产碎片拥有者账户
     * @param assetId  资产id
     * @param shardId  碎片id
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> consumeShard(final Account cAccount, final Account uAccount, final long assetId, final long shardId) throws BaseException {
        if (cAccount == null || uAccount == null || assetId < 1 || shardId < 1) {
            Base.logger.warning("consume shard param is invalid");
            throw new BaseException("param error");
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
            throw new BaseException("ecdsa sign error");
        }

        try {
            rawUSign = Crypto.xassetSignECDSA(uAccount, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("uAccount ecdsa sign failed" + e);
            throw new BaseException("ecdsa sign error");
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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger
                    .warning(String.format("consume shard failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                            res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        BaseResp resp = new BaseResp(requestId, errNo, errMsg);

        Base.logger.info(String.format("consume shard succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【碎片查询】查询指定资产碎片信息
     *
     * @param assetId 资产id
     * @param shardId 碎片id
     * @return {@link Resp}<{@link QueryShardsResp}>
     */
    public Resp<QueryShardsResp> queryShards(final long assetId, final long shardId) throws BaseException {
        if (assetId < 1 || shardId < 1) {
            Base.logger.warning("query shards param is invalid");
            throw new BaseException("param error");
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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("query shard failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        JSONObject rawMeta = obj.getJSONObject("meta");
        JSONObject rawAssetInfo = rawMeta.getJSONObject("asset_info");
        JSONArray rawThumb = rawAssetInfo.getJSONArray("thumb");

        Thumb[] thumb = new Thumb[rawThumb.size()];
        for (int i = 0; i < rawThumb.size(); i++) {
            JSONObject objThumb = (JSONObject) rawThumb.get(i);
            JSONObject objUrls = objThumb.getJSONObject("urls");
            Urls urls = new Urls(objUrls.getString("icon"), objUrls.getString("url1"), objUrls.getString("url2"),objUrls.getString("url3"));
            thumb[i] = new Thumb(urls, objThumb.getString("width"), objThumb.getString("height"));
        }

        ShardAssetInfo assetInfo = new ShardAssetInfo(rawAssetInfo.getString("title"), rawAssetInfo.getIntValue("asset_cate"), thumb, rawAssetInfo.getString("short_desc"), rawAssetInfo.getString("create_addr"), rawAssetInfo.getLongValue("group_id"));
        ShardMeta meta = new ShardMeta(rawMeta.getLongValue("asset_id"), rawMeta.getLongValue("shard_id"), rawMeta.getString("owner_addr"), rawMeta.getLongValue("uid"), rawMeta.getLongValue("price"),rawMeta.getIntValue("status"), rawMeta.getString("tx_id"), assetInfo, rawMeta.getLongValue("ctime"));

        QueryShardsResp resp = new QueryShardsResp(requestId, errNo, errMsg, meta);

        Base.logger.info(String.format("query shards succ.[meta:%s] [url:%s] [request_id:%s] [trace_id:%s]", resp.meta,
                res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【碎片查询】分页拉取指定账户持有碎片列表
     *
     * @param addr    要拉取的区块链账户地址
     * @param page    要拉取页数，第一页为1
     * @param limit   每页拉取数量，默认20，最大50（可选）
     * @param assetId 拉取指定藏品的碎片列表（可选）
     * @return {@link Resp}<{@link ListPageResp}>
     */
    public Resp<ListPageResp> listShardsByAddr(final String addr, final int page, final int limit, final long assetId) throws BaseException {
        if (isNullOrEmpty(addr) || page < 1 || limit < 1 || limit > BaseDef.MAXLIMIT) {
            Base.logger.warning("list shards by addr param is invalid");
            throw new BaseException("param error");
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("addr", addr);
                put("page", String.format("%d", page));
                put("limit", String.format("%d", limit));
                put("asset_id", String.format("%d", assetId));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.LISTSHARDSBYADDR, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("list shards by addr failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        JSONArray rawList = obj.getJSONArray("list");
        ShardMeta[] list = new ShardMeta[rawList.size()];
        for (int i = 0; i < rawList.size(); i++) {
            JSONObject rawMeta = (JSONObject) rawList.get(i);
            JSONObject rawAssetInfo = rawMeta.getJSONObject("asset_info");
            JSONArray rawThumb = rawAssetInfo.getJSONArray("thumb");
            Thumb[] thumb = new Thumb[rawThumb.size()];
            for (int j = 0; j < rawThumb.size(); j++) {
                JSONObject objThumb = (JSONObject) rawThumb.get(j);
                JSONObject objUrls = objThumb.getJSONObject("urls");
                Urls urls = new Urls(objUrls.getString("icon"), objUrls.getString("url1"), objUrls.getString("url2"),objUrls.getString("url3"));
                thumb[j] = new Thumb(urls, objThumb.getString("width"), objThumb.getString("height"));
            }

            ShardAssetInfo assetInfo = new ShardAssetInfo(rawAssetInfo.getString("title"), rawAssetInfo.getIntValue("asset_cate"), thumb, rawAssetInfo.getString("short_desc"), rawAssetInfo.getString("create_addr"), rawAssetInfo.getLongValue("group_id"));
            list[i] = new ShardMeta(rawMeta.getLongValue("asset_id"), rawMeta.getLongValue("shard_id"), rawMeta.getString("owner_addr"), rawMeta.getLongValue("uid"), rawMeta.getLongValue("price"),rawMeta.getIntValue("status"), rawMeta.getString("tx_id"), assetInfo, rawMeta.getLongValue("ctime"));
        }

        ListPageResp resp = new ListPageResp(requestId, errNo, errMsg, list, obj.getIntValue("total_cnt"));

        Base.logger.info(String.format(
                "list shards by addr succ.[list:%s] [total_cnt:%d] [url:%s] [request_id:%s] [trace_id:%s]", resp.list,
                resp.totalCnt, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【碎片查询】分页拉取指定资产已授予碎片列表
     *
     * @param assetId 资产id
     * @param cursor  分页游标，首页设置空字符串（可选）
     * @param limit   每页拉取数量，默认20，最多50（可选）
     * @return {@link Resp}<{@link ListCursorResp}>
     */
    public Resp<ListCursorResp> listShardsByAsset(final long assetId, final String cursor, final int limit) throws BaseException {
        if (assetId < 1 || limit < 1 || limit > BaseDef.MAXLIMIT) {
            Base.logger.warning("list shards by asset param is invalid");
            throw new BaseException("param error");
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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("list shards by asset failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        JSONArray rawList = obj.getJSONArray("list");
        ShardMeta[] list = new ShardMeta[rawList.size()];
        for (int i = 0; i < rawList.size(); i++) {
            JSONObject rawMeta = (JSONObject) rawList.get(i);
            list[i] = new ShardMeta(rawMeta.getLongValue("asset_id"), rawMeta.getLongValue("shard_id"), rawMeta.getString("owner_addr"), 0, rawMeta.getLongValue("price"),rawMeta.getIntValue("status"), rawMeta.getString("tx_id"), null, rawMeta.getLongValue("ctime"));
        }

        ListCursorResp resp = new ListCursorResp(requestId, errNo, errMsg, list, obj.getIntValue("has_more"), obj.getString("cursor"));

        Base.logger.info(String.format(
                "list shards by asset succ.[list:%s] [cursor:%s] [has_more:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.list, resp.cursor, resp.hasMore, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【碎片查询】分页拉取指定address下的藏品变更记录
     *
     * @param addr      要查询的区块链账户地址
     * @param cursor    分页游标，首页设置空字符串（可选）
     * @param limit     每页拉取数量，默认30，最多50（可选）
     * @param opTypes   要查询的操作类型，[]int格式JSON串，1:授予 2:转出 3:转入 4:核销
     * @return {@link Resp}<{@link ListCursorResp}>
     */
    public Resp<ListCursorResp> listDiffByAddr(final String addr, final String cursor, final int limit, final String opTypes) throws BaseException {
        if (isNullOrEmpty(addr) || limit < 1 || limit > BaseDef.MAXLIMIT) {
            Base.logger.warning("list diff by addr param is invalid");
            throw new BaseException("param error");
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("addr", addr);
                put("cursor", cursor);
                put("limit", String.format("%d", limit));
                put("op_types", opTypes);
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.LISTDIFFBYADDR, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("list diff by addr failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        JSONArray rawList = obj.getJSONArray("list");
        ShardDiffInfo[] list = new ShardDiffInfo[rawList.size()];
        for (int i = 0; i < rawList.size(); i++) {
            JSONObject rawMeta = (JSONObject) rawList.get(i);
            JSONArray rawThumb = rawMeta.getJSONArray("thumb");
            Thumb[] thumb = new Thumb[rawThumb.size()];
            for (int j = 0; j < rawThumb.size(); j++) {
                JSONObject objThumb = (JSONObject) rawThumb.get(j);
                JSONObject objUrls = objThumb.getJSONObject("urls");
                Urls urls = new Urls(objUrls.getString("icon"), objUrls.getString("url1"), objUrls.getString("url2"), objUrls.getString("url3"));
                thumb[j] = new Thumb(urls, objThumb.getString("width"), objThumb.getString("height"));
            }

            list[i] = new ShardDiffInfo(rawMeta.getLongValue("asset_id"), rawMeta.getLongValue("shard_id"), rawMeta.getIntValue("operate"), rawMeta.getString("title"), thumb, rawMeta.getLongValue("ctime"));
        }

        ListCursorResp resp = new ListCursorResp(requestId, errNo, errMsg, list, obj.getIntValue("has_more"), obj.getString("cursor"));

        Base.logger.info(String.format(
                "list diff by addr succ.[list:%s] [cursor:%s] [has_more:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.list, resp.cursor, resp.hasMore, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【资产记录】拉取数字资产历史登记记录
     *
     * @param assetId 资产id
     * @param page    要拉取页面，第一页为1
     * @param limit   每页拉取数量，默认20，最多50（可选）
     * @param shardId 查询指定藏品，指定碎片的历史记录（可选）
     * @return {@link Resp}<{@link ListPageResp}>
     */
    public Resp<ListPageResp> history(final long assetId, final int page, final int limit, final long shardId) throws BaseException {
        if (assetId < 1 || page < 1 || limit < 1 || limit > BaseDef.MAXLIMIT) {
            Base.logger.warning("history param is invalid");
            throw new BaseException("param error");
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
                put("page", String.format("%d", page));
                put("limit", String.format("%d", limit));
                put("shard_id", String.format("%d", shardId));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.HISTORYASSET, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("history failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        JSONArray rawList = obj.getJSONArray("list");
        History[] list = new History[rawList.size()];
        for (int i = 0; i < rawList.size(); i++) {
            JSONObject history = (JSONObject) rawList.get(i);
            list[i] = new History(history.getLongValue("asset_id"), history.getIntValue("type"), history.getLongValue("shard_id"), history.getLongValue("price"),history.getString("tx_id"), history.getString("from"), history.getString("to"), history.getLongValue("ctime"));
        }

        ListPageResp resp = new ListPageResp(requestId, errNo, errMsg, list, obj.getIntValue("total_cnt"));

        Base.logger.info(String.format("history succ.[list:%s] [total_cnt:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.list, resp.totalCnt, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【存证信息】获取资产存证信息
     *
     * @param assetId 资产id
     * @return {@link Resp}<{@link GetEvidenceInfoResp}>
     */
    public Resp<GetEvidenceInfoResp> getEvidenceInfo(final long assetId) throws BaseException {
        if (assetId < 1) {
            Base.logger.warning("get evidence info param is invalid");
            throw new BaseException("param error");
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
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("get evidence info failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        GetEvidenceInfoResp resp = new GetEvidenceInfoResp(requestId, errNo, errMsg,
                obj.getString("create_addr"), obj.getString("tx_id"), obj.getJSONObject("asset_info"), obj.getLongValue("ctime"));

        Base.logger.info(String.format(
                "get evidence info succ.[create_addr:%s] [tx_id:%s] [asset_info:%s] [ctime:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.createAddr, resp.txId, resp.assetInfo, resp.cTime, res.reqUrl, resp.requestId,
                res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【应用场景】拉取账户下允许访问的address列表（接口内部会帮助进行SK加解密）
     *
     * @param unionId 第三方应用获取的union_id
     * @return {@link Resp}<{@link ListAddrResp}>
     */
    public Resp<ListAddrResp> scenelistAddr(final String unionId) throws BaseException {
        if (isNullOrEmpty(unionId)) {
            Base.logger.warning("scene list addr param is invalid");
            throw new BaseException("param error");
        }

        // 使用 账户sk 加密 union_id
        String sk = Base.getConfig().Credentials.SecreteAccessKey;
        String encryptUnionId = Utils.encrypt(sk, unionId);

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("union_id", encryptUnionId);
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.SCENELISTADDR, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("list addr failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        JSONArray rawList = obj.getJSONArray("list");
        AddrMeta[] list = new AddrMeta[rawList.size()];
        for (int i = 0; i < rawList.size(); i++) {
            JSONObject rawMeta = (JSONObject) rawList.get(i);
            list[i] = new AddrMeta(rawMeta.getString("addr"), rawMeta.getString("token"), rawMeta.getLongValue("group_id"));
        }

        ListAddrResp resp = new ListAddrResp(requestId, errNo, errMsg, list);


        Base.logger.info(String.format(
                "list addr succ.[list:%s] [url:%s] [request_id:%s] [trace_id:%s]", resp.list, res.reqUrl, resp.requestId,
                res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【应用场景】拉取address下允许访问的藏品列表
     *
     * @param addr   要查询的区块链账户地址
     * @param token  listaddr接口下发的授权token
     * @param cursor 分页游标，首页设置空字符串（可选）
     * @param limit  每页拉取数量，默认30，最多50（可选）
     * @return {@link Resp}<{@link ListCursorResp}>
     */
    public Resp<ListCursorResp> scenelistsdsbyaddr(final String addr, final String token, final String cursor, final int limit) throws BaseException {
        if (isNullOrEmpty(addr) || isNullOrEmpty(token) || limit < 1 || limit > BaseDef.MAXLIMIT) {
            Base.logger.warning("scene list sds by addr param is invalid");
           throw new BaseException("param error");
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("addr", addr);
                put("token", token);
                put("cursor", cursor);
                put("limit", String.format("%d", limit));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.SCENELISTSHRADSBYADDR, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("list shards by addr failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        JSONArray rawList = obj.getJSONArray("list");
        SceneShardInfo[] list = new SceneShardInfo[rawList.size()];
        for (int i = 0; i < rawList.size(); i++) {
            JSONObject rawMeta = (JSONObject) rawList.get(i);
            JSONArray rawThumb = rawMeta.getJSONArray("thumb");
            Thumb[] thumb = new Thumb[rawThumb.size()];
            for (int j = 0; j < rawThumb.size(); j++) {
                JSONObject objThumb = (JSONObject) rawThumb.get(j);
                JSONObject objUrls = objThumb.getJSONObject("urls");
                Urls urls = new Urls(objUrls.getString("icon"), objUrls.getString("url1"), objUrls.getString("url2"), objUrls.getString("url3"));
                thumb[j] = new Thumb(urls, objThumb.getString("width"), objThumb.getString("height"));
            }

            list[i] = new SceneShardInfo(rawMeta.getLongValue("asset_id"), rawMeta.getLongValue("shard_id"), rawMeta.getString("title"), thumb, rawMeta.getLongValue("ctime"));
        }

        ListCursorResp resp = new ListCursorResp(requestId, errNo, errMsg, list, obj.getIntValue("has_more"), obj.getString("cursor"));

        Base.logger.info(String.format(
                "list shards by addr succ.[list:%s] [cursor:%s] [has_more:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.list, resp.cursor, resp.hasMore, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【应用场景】判断address下是否拥有指定数字藏品
     *
     * @param addr     要查询的区块链账户地址
     * @param token    listaddr接口下发的授权token
     * @param assetIds 要查询的资产ID列表json字符串，一次查询不超过10个
     * @return {@link Resp}<{@link HasAssetByAddrResp}>
     */
    public Resp<HasAssetByAddrResp> scenehasastbyaddr(final String addr, final String token, final String assetIds) throws BaseException {
        if (isNullOrEmpty(addr) || isNullOrEmpty(token) || isNullOrEmpty(assetIds)) {
            Base.logger.warning("scene has asset by addr param is invalid");
            throw new BaseException("param error");
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("addr", addr);
                put("token", token);
                put("asset_ids", assetIds);
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.SCENEHASASSETBYADDR, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("has asset by addr failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        Map<String, Integer> result = JSONObject.parseObject(obj.getJSONObject("result").toString(), new TypeReference<Map<String, Integer>>(){});

        HasAssetByAddrResp resp = new HasAssetByAddrResp(requestId, errNo, errMsg, result);

        Base.logger.info(String.format(
                "has asset by addr succ.[result:%s] [url:%s] [request_id:%s] [trace_id:%s]", resp.result, res.reqUrl, resp.requestId,
                res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【应用场景】拉取address下的藏品变更记录
     *
     * @param addr      要查询的区块链账户地址
     * @param token     listaddr接口下发的授权token
     * @param cursor    分页游标，首页设置空字符串（可选）
     * @param limit     每页拉取数量，默认30，最多50（可选）
     * @param opTypes   要查询的操作类型，[]int格式JSON串，1:授予 2:转出 3:转入 4:核销
     * @return {@link Resp}<{@link ListCursorResp}>
     */
    public Resp<ListCursorResp> scenelistDiffByAddr(final String addr, final String token, final String cursor, final int limit, final String opTypes) throws BaseException {
        if (isNullOrEmpty(addr) || isNullOrEmpty(token) || limit < 1 || limit > BaseDef.MAXLIMIT) {
            Base.logger.warning("scene list diff by addr param is invalid");
            throw new BaseException("param error");
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("addr", addr);
                put("token", token);
                put("cursor", cursor);
                put("limit", String.format("%d", limit));
                put("op_types", opTypes);
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.SCENELISTDIFFBYADDR, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("list diff by addr failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        JSONArray rawList = obj.getJSONArray("list");
        ShardDiffInfo[] list = new ShardDiffInfo[rawList.size()];
        for (int i = 0; i < rawList.size(); i++) {
            JSONObject rawMeta = (JSONObject) rawList.get(i);
            JSONArray rawThumb = rawMeta.getJSONArray("thumb");
            Thumb[] thumb = new Thumb[rawThumb.size()];
            for (int j = 0; j < rawThumb.size(); j++) {
                JSONObject objThumb = (JSONObject) rawThumb.get(j);
                JSONObject objUrls = objThumb.getJSONObject("urls");
                Urls urls = new Urls(objUrls.getString("icon"), objUrls.getString("url1"), objUrls.getString("url2"), objUrls.getString("url3"));
                thumb[j] = new Thumb(urls, objThumb.getString("width"), objThumb.getString("height"));
            }

            list[i] = new ShardDiffInfo(rawMeta.getLongValue("asset_id"), rawMeta.getLongValue("shard_id"), rawMeta.getIntValue("operate"), rawMeta.getString("title"), thumb, rawMeta.getLongValue("ctime"));
        }

        ListCursorResp resp = new ListCursorResp(requestId, errNo, errMsg, list, obj.getIntValue("has_more"), obj.getString("cursor"));

        Base.logger.info(String.format(
                "list diff by addr succ.[list:%s] [cursor:%s] [has_more:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.list, resp.cursor, resp.hasMore, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 【应用场景】查询用户碎片信息
     *
     * @param addr    碎片拥有者区块链账户地址
     * @param token   listaddr接口下发的授权token
     * @param assetId 资产id
     * @param shardId 碎片id
     * @return {@link Resp}<{@link SceneQueryShardsResp}>
     */
    public Resp<SceneQueryShardsResp> scenequeryShards(final String addr, final String token, final long assetId, final long shardId) throws BaseException {
        if (assetId < 1 || shardId < 1 || isNullOrEmpty(addr) || isNullOrEmpty(token)) {
            Base.logger.warning("scene query shards param is invalid");
            throw new BaseException("param error");
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
                put("shard_id", String.format("%d", shardId));
                put("addr", addr);
                put("token", token);
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.SCENEQUERYSHARDINFO, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            throw new BaseException("post request error");
        }

        // 解析结果
        JSONObject obj = JSONObject.parseObject(res.body);
        long requestId = obj.getLongValue("request_id");
        int errNo = obj.getIntValue("errno");
        String errMsg = obj.getString("errmsg");

        if (res.httpCode != 200) {
            Base.logger.warning(String.format("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        if (errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("query shard failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, requestId, errNo, res.traceId));
            throw new BaseException(errNo, errMsg, requestId);
        }

        JSONObject rawMeta = obj.getJSONObject("meta");
        JSONArray rawThumb = rawMeta.getJSONArray("thumb");

        Thumb[] thumb = new Thumb[rawThumb.size()];
        for (int i = 0; i < rawThumb.size(); i++) {
            JSONObject objThumb = (JSONObject) rawThumb.get(i);
            JSONObject objUrls = objThumb.getJSONObject("urls");
            Urls urls = new Urls(objUrls.getString("icon"), objUrls.getString("url1"), objUrls.getString("url2"), objUrls.getString("url3"));
            thumb[i] = new Thumb(urls, objThumb.getString("width"), objThumb.getString("height"));
        }

        SceneShardMeta meta = new SceneShardMeta(rawMeta.getLongValue("asset_id"), rawMeta.getLongValue("shard_id"), rawMeta.getString("owner_addr"), rawMeta.getLongValue("price"), rawMeta.getIntValue("status"), rawMeta.getString("tx_id"), rawMeta.getLongValue("ctime"),
                rawMeta.getString("jump_link"), rawMeta.getString("title"), thumb, rawMeta.getJSONArray("asset_url"), rawMeta.getJSONArray("img_desc"), rawMeta.getString("short_desc"), rawMeta.getString("create_addr"));

        SceneQueryShardsResp resp = new SceneQueryShardsResp(requestId, errNo, errMsg, meta);

        Base.logger.info(String.format("query shards succ.[meta:%s] [url:%s] [request_id:%s] [trace_id:%s]", resp.meta,
                res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }
}

