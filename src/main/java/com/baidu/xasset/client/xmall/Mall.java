package com.baidu.xasset.client.xmall;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.xasset.auth.Crypto;
import com.baidu.xasset.client.base.Base;
import com.baidu.xasset.client.base.BaseDef;
import com.baidu.xasset.client.base.BaseDef.*;
import com.baidu.xasset.client.xmall.XmallDef.*;
import com.baidu.xasset.common.config.Config;
import com.baidu.xasset.utils.Utils;
import com.baidu.xuper.api.Account;

import java.util.HashMap;
import java.util.Map;


public class Mall {
    Base xassetBaseClient;

    public Mall(Config.XassetCliConfig cfg, java.util.logging.Logger logger) {
        this.xassetBaseClient = new Base(cfg, logger);
    }

    /**
     * 上架数字资产到售卖大厅
     *
     * @param account      资产上架区块链账户
     * @param assetId      资产id
     * @param saleItemInfo 资产售卖信息
     * @return {@link Resp}<{@link SellItemResp}>
     */
    public Resp<SellItemResp> sellItem(final Account account, final long assetId, final SaleItemInfo saleItemInfo) {
        if (account == null || assetId < 1 || saleItemInfo == null || saleItemInfo.price < 0 || saleItemInfo.oriPrice < 0) {
            Base.logger.warning("sell item param is invalid");
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
        final String saleItem = JSONObject.toJSONString(saleItemInfo);
        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
                put("sale_item", saleItem);
                put("addr", account.getAKAddress());
                put("pkey", account.getKeyPair().getJSONPublicKey());
                put("nonce", String.format("%d", nonce));
                put("sign", sign);
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.SELLITEM, body);
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
        SellItemResp resp = new SellItemResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getLong("sale_id"));
        if (resp.errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("sell item failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, resp.requestId, resp.errNo, res.traceId));
            return null;
        }

        Base.logger.info(String.format("sell item succ.[sale_id:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.saleId, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 从售卖大厅下架数字资产
     *
     * @param account 资产上架区块链账户
     * @param assetId 资产id
     * @param saleId  售卖单元id
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> withdrawItem(final Account account, final long assetId, final long saleId) {
        if (account == null || assetId < 1 || saleId < 1) {
            Base.logger.warning("withdraw item param is invalid");
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
                put("sale_id", String.format("%d", saleId));
                put("addr", account.getAKAddress());
                put("pkey", account.getKeyPair().getJSONPublicKey());
                put("nonce", String.format("%d", nonce));
                put("sign", sign);
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.WITHDRAWITEM, body);
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
        BaseResp resp = new BaseResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"));
        if (resp.errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("withdraw item failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, resp.requestId, resp.errNo, res.traceId));
            return null;
        }

        Base.logger.info(String.format("withdraw item succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 根据过滤策略获取当前在售资产列表
     *
     * @param filterId   数据拉取策略编号
     * @param filterCond 策略好对应参数json串（可选）
     * @param cursor     分页游标（可选）
     * @param limit      每页拉取资产数量。默认20，最多50（可选）
     * @return {@link Resp}<{@link ListCursorResp}>
     */
    public Resp<ListCursorResp> listByFilterItem(final int filterId, final String filterCond, final String cursor, final int limit) {
        if (filterId < 1 || limit < 1 || limit > BaseDef.MAXLIMIT) {
            Base.logger.warning("list by filter param is invalid");
            return null;
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("filter_id", String.format("%d", filterId));
                put("filter_cond", filterCond);
                put("limit", String.format("%d", limit));
                put("cursor", cursor);
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.LISTITEMBYFILTER, body);
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
        ListCursorResp resp = new ListCursorResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getJSONArray("list"), obj.getIntValue("has_more"), obj.getString("cursor"));
        if (resp.errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(
                    String.format("list by filter item failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                            res.reqUrl, resp.requestId, resp.errNo, res.traceId));
            return null;
        }

        Base.logger.info(String.format(
                "list by filter item succ.[list:%s] [has_more:%d] [cursor:%s] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.list, resp.hasMore, resp.cursor, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 查询售卖资产详情
     *
     * @param assetId 资产id
     * @return {@link Resp}<{@link QueryItemResp}>
     */
    public Resp<QueryItemResp> queryItem(final long assetId) {
        if (assetId < 1) {
            Base.logger.warning("query item param is invalid");
            return null;
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("asset_id", String.format("%d", assetId));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.QUERYITEM, body);
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
        QueryItemResp resp = new QueryItemResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getJSONObject("meta"), obj.getJSONArray("sale_list"));
        if (resp.errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("query item failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, resp.requestId, resp.errNo, res.traceId));
            return null;
        }

        Base.logger
                .info(String.format("query item succ.[meta:%s] [sale_list:%s] [url:%s] [request_id:%s] [trace_id:%s]",
                        resp.meta, resp.saleList, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 查询售卖资产列表
     *
     * @param address 资产上架区块链地址
     * @param status  资产在售状态。0：拉取全状态 1：售卖中 7：下架 8：封禁 9：删除
     * @param page    要拉取页数，第一页为1
     * @param limit   每页拉取资产数量。默认20，最多50（可选）
     * @return {@link Resp}<{@link ListPageResp}>
     */
    public Resp<ListPageResp> listItems(final String address, final int status, final int page, final int limit) {
        if ("".equals(address) || limit < 1 || limit > BaseDef.MAXLIMIT) {
            Base.logger.warning("list items param is invalid");
            return null;
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("addr", address);
                put("page", String.format("%d", page));
                put("limit", String.format("%d", limit));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.LISTITEMS, body);
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
        ListPageResp resp = new ListPageResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getJSONArray("list"), obj.getIntValue("total_cnt"));
        if (resp.errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("list items failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, resp.requestId, resp.errNo, res.traceId));
            return null;
        }

        Base.logger
                .info(String.format("list items succ.[list:%s] [total_cnt:%d] [url:%s] [request_id:%s] [trace_id:%s]",
                        resp.list, resp.totalCnt, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 创建订单
     *
     * @param account        创建订单区块链账户
     * @param oid            订单id
     * @param assetId        资产id
     * @param saleId         售卖单元id
     * @param buyerAddr      买家区块链地址
     * @param payType        支付渠道类型。1：微信 2：支付宝 9：免费
     * @param buyCnt         资产购买数量
     * @param assetPrice     资产总金额。单位：分
     * @param payAmount      支付总金额。单位：分
     * @param from           订单来源（可选）
     * @param profitList     分润方信息（可选）
     * @param payRelatedInfo 支付相关信息，非免费支付类型必填（可选）
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> createOrder(final Account account, final long oid, final long assetId, final long saleId, final long saleNum, final String buyerAddr, final int payType, final int buyCnt, final int assetPrice, final int payAmount, final int from, final ProfitShareInfo[] profitList, final JSONObject payRelatedInfo) {
        // check param
        if (account == null || oid < 1 || assetId < 1 || saleId < 1 || "".equals(buyerAddr) || buyCnt < 1 || assetPrice < 1 || payAmount < 1 || (payType == XmallDef.PAYBYSPECIAL && payRelatedInfo != null) || (payType != XmallDef.PAYBYSPECIAL && payRelatedInfo == null)) {
            Base.logger.warning("create order param is invalid");
            return null;
        }

        // sign
        String signMsg = String.format("%d%d", assetId, oid);
        String rawSign;
        try {
            rawSign = Crypto.xassetSignECDSA(account, signMsg.getBytes());
        } catch (Exception e) {
            Base.logger.warning("account esdsa sign failed" + e);
            return null;
        }

        final String sign = rawSign;
        String rawPayRelatedInfo = null;
        if (payRelatedInfo != null) {
            rawPayRelatedInfo = JSONObject.toJSONString(payRelatedInfo);
        }
        String rawProfitList = null;
        if (profitList != null) {
            rawProfitList = JSONArray.toJSONString(profitList);
        }
        final String finalPayRelatedInfo = rawPayRelatedInfo;
        final String finalProfitList = rawProfitList;
        Map<String, String> body = new HashMap<String, String>() {
            {
                put("oid", String.format("%d", oid));
                put("asset_id", String.format("%d", assetId));
                put("sale_id", String.format("%d", saleId));
                put("seller_addr", account.getAKAddress());
                put("buyer_addr", buyerAddr);
                put("pay_type", String.format("%d", payType));
                put("pay_related_info", (finalPayRelatedInfo == null) ? "" : finalPayRelatedInfo);
                put("buy_cnt", String.format("%d", buyCnt));
                put("asset_price", String.format("%d", assetPrice));
                put("pay_amount", String.format("%d", payAmount));
                put("seller_sign", sign);
                put("profit_list", (finalProfitList == null) ? "" : finalProfitList);
                put("from", String.format("%d", from));
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.CREATEORDER, body);
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
        BaseResp resp = new BaseResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"));
        if (resp.errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("create order failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, resp.requestId, resp.errNo, res.traceId));
            return null;
        }

        Base.logger.info(String.format("create order succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 关闭订单
     *
     * @param account    创建订单区块链账户
     * @param oid        订单id
     * @param cancelType 关闭类型类型。0：买家关闭 1：超时关闭
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> cancelOrder(final Account account, final long oid, final int cancelType) {
        // check param
        if (account == null || oid < 1 || cancelType < 0) {
            Base.logger.warning("cancel order param is invalid");
            return null;
        }

        // sign
        final long nonce = Utils.genNonce();
        String signMsg = String.format("%d%d", oid, nonce);
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
                put("oid", String.format("%d", oid));
                put("cancel_type", String.format("%d", cancelType));
                put("nonce", String.format("%d", nonce));
                put("buyer_addr", account.getAKAddress());
                put("buyer_pkey", account.getKeyPair().getJSONPublicKey());
                put("buyer_sign", sign);
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.CANCELORDER, body);
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
        BaseResp resp = new BaseResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"));
        if (resp.errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("cancel order failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, resp.requestId, resp.errNo, res.traceId));
            return null;
        }

        Base.logger.info(String.format("cancel order succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 删除订单
     *
     * @param account 创建订单区块链账户
     * @param oid     订单id
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> deleteOrder(final Account account, final long oid) {
        // check param
        if (account == null || oid < 1) {
            Base.logger.warning("delete order param is invalid");
            return null;
        }

        // sign
        final long nonce = Utils.genNonce();
        String signMsg = String.format("%d%d", oid, nonce);
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
                put("oid", String.format("%d", oid));
                put("nonce", String.format("%d", nonce));
                put("buyer_addr", account.getAKAddress());
                put("buyer_pkey", account.getKeyPair().getJSONPublicKey());
                put("buyer_sign", sign);
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.DELETEORDER, body);
        } catch (Exception e) {
            Base.logger.warning("post request xasset failed" + e);
            return null;
        }
        if (res.httpCode != 200) {
            System.out.printf("post request response is not 200.[http_code:%d] [url:%s] [body:%s] [trace_id:%s]",
                    res.httpCode, res.reqUrl, res.body, res.traceId);
            return null;
        }

        JSONObject obj = JSONObject.parseObject(res.body);
        BaseResp resp = new BaseResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"));
        if (resp.errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("delete order failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, resp.requestId, resp.errNo, res.traceId));
            return null;
        }

        Base.logger.info(String.format("delete order succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 获取支付参数
     *
     * @param account 订单买家区块链账户
     * @param oid     订单id
     * @param payType 支付渠道类型。1：微信 2：支付宝 9：免费
     * @return {@link Resp}<{@link PayInfoResp}>
     */
    public Resp<PayInfoResp> payInfo(final Account account, final long oid, final int payType) {
        // check param
        if (account == null || oid < 1 || payType < XmallDef.PAYBYWECHAT) {
            Base.logger.warning("pay info param is invalid");
            return null;
        }

        // sign
        final long nonce = Utils.genNonce();
        String signMsg = String.format("%d%d", oid, nonce);
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
                put("oid", String.format("%d", oid));
                put("nonce", String.format("%d", nonce));
                put("pay_type", String.format("%d", payType));
                put("buyer_addr", account.getAKAddress());
                put("buyer_pkey", account.getKeyPair().getJSONPublicKey());
                put("buyer_sign", sign);
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.GETORDERPAYINFO, body);
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
        PayInfoResp resp = new PayInfoResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getJSONObject("pay_info"));
        if (resp.errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("pay info failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, resp.requestId, resp.errNo, res.traceId));
            return null;
        }

        Base.logger.info(String.format("pay info succ.[pay_info:%s] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.payInfo, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 查询订单详情
     *
     * @param address       区块链账户地址
     * @param oid           订单id
     * @param orderUserType 订单用户类型。0：买家 1：卖家 2：分润方
     * @return {@link Resp}<{@link QueryOrderResp}>
     */
    public Resp<QueryOrderResp> queryOrder(final String address, final long oid, final int orderUserType) {
        // check param
        if ("".equals(address) || oid < 1 || orderUserType < 0) {
            Base.logger.warning("query order param is invalid");
            return null;
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("oid", String.format("%d", oid));
                put("order_user_type", String.format("%d", orderUserType));
                put("addr", address);
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.QUERYORDER, body);
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
        QueryOrderResp resp = new QueryOrderResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getJSONObject("info"));
        if (resp.errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("query order failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, resp.requestId, resp.errNo, res.traceId));
            return null;
        }

        Base.logger.info(String.format("query order succ.[info:%s] [url:%s] [request_id:%s] [trace_id:%s]", resp.info,
                res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 拉取订单列表
     *
     * @param address       区块链账户地址
     * @param orderUserType 订单用户类型。0：买家 1：卖家 2：分润方
     * @param displayStatus 订单状态。全部：1001 待付款：1002 已付款：1004 已使用：1011 已关闭：1012 已删除：1014。默认全部（可选）
     * @param cursor        分页游标（可选）
     * @param limit         每页拉取订单数量。默认20，最大50（可选）
     * @return {@link Resp}<{@link ListCursorResp}>
     */
    public Resp<ListCursorResp> queryOrderList(final String address, final int orderUserType, final int displayStatus, final String cursor, final int limit) {
        // check param
        if ("".equals(address) || orderUserType < 0 || limit < 1 || limit > BaseDef.MAXLIMIT) {
            Base.logger.warning("query order list param is invalid");
            return null;
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("order_user_type", String.format("%d", orderUserType));
                put("display_status", String.format("%d", displayStatus));
                put("cursor", cursor);
                put("limit", String.format("%d", limit));
                put("addr", address);
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.QUERYORDERLIST, body);
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
        ListCursorResp resp = new ListCursorResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"),
                obj.getJSONArray("list"), obj.getIntValue("has_more"), obj.getString("cursor"));
        if (resp.errNo != BaseDef.ERRNOSUCC) {
            Base.logger
                    .warning(String.format("query order list failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                            res.reqUrl, resp.requestId, resp.errNo, res.traceId));
            return null;
        }

        Base.logger.info(String.format(
                "query order list succ.[list:%s] [has_more:%d] [cursor:%s] [url:%s] [request_id:%s] [trace_id:%s]",
                resp.list, resp.hasMore, resp.cursor, res.reqUrl, resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }

    /**
     * 支付成功通知
     *
     * @param address 买家区块链账户地址
     * @param oid     订单id
     * @param payNum  支付交易号。非免费支付类型必填（可选）
     * @param payInfo 支付信息（可选）
     * @return {@link Resp}<{@link BaseResp}>
     */
    public Resp<BaseResp> payNotify(final String address, final long oid, final String payNum, final String payInfo) {
        // check param
        if ("".equals(address) || oid < 1) {
            Base.logger.warning("pay notify param is invalid");
            return null;
        }

        Map<String, String> body = new HashMap<String, String>() {
            {
                put("oid", String.format("%d", oid));
                put("pay_num", (payNum == null) ? "" : payNum);
                put("pay_info", (payInfo == null) ? "" : payInfo);
                put("buyer_addr", address);
            }
        };

        RequestRes res;
        try {
            res = Base.post(Api.PAYNOTIFYORDER, body);
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
        BaseResp resp = new BaseResp(obj.getLong("request_id"), obj.getIntValue("errno"), obj.getString("errmsg"));
        if (resp.errNo != BaseDef.ERRNOSUCC) {
            Base.logger.warning(String.format("pay notify failed.[url:%s] [request_id:%s] [err_no:%d] [trace_id:%s]",
                    res.reqUrl, resp.requestId, resp.errNo, res.traceId));
            return null;
        }

        Base.logger.info(String.format("pay notify succ.[url:%s] [request_id:%s] [trace_id:%s]", res.reqUrl,
                resp.requestId, res.traceId));
        return new Resp<>(resp, res);
    }
}