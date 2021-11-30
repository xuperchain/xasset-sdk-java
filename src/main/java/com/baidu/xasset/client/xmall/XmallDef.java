package com.baidu.xasset.client.xmall;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.baidu.xasset.client.base.BaseDef;
import com.baidu.xuper.api.Account;

public class XmallDef {
    public static class SaleItemInfo {
        @JSONField(name = "price")
        long price;
        @JSONField(name = "ori_price")
        long oriPrice;
        @JSONField(name = "sale_info")
        String saleInfo;
        @JSONField(name = "sale_type")
        int saleType;
        @JSONField(name = "sale_num")
        long saleNum;

        /**
         * 商品上架信息
         *
         * @param price    资产折扣价，单位：分
         * @param oriPrice 资产原价，单位：分
         * @param saleType 售卖方式，0：一口价 1：拍卖。默认0
         * @param saleNum  售卖单元编号，默认0
         * @param saleInfo 售卖单元描述信息，json串。比如拍卖信息
         */
        public SaleItemInfo(long price, long oriPrice, int saleType, long saleNum, String saleInfo) {
            this.price = price;
            this.oriPrice = oriPrice;
            this.saleInfo = saleInfo;
            this.saleType = saleType;
            this.saleNum = saleNum;
        }
    }

    /**
     * 上架数字资产到售卖大厅返回值
     *
     * requestId：请求id
     * errNo：错误码
     * errMsg：错误信息
     * saleId：售卖单元id
     */
    public static class SellItemResp {
        public long requestId;
        public int errNo;
        public String errMsg;
        public long saleId;

        SellItemResp(long requestId, int errNo, String errMsg, long saleId) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.saleId = saleId;
        }
    }

    /**
     * 查询售卖资产详情
     *
     * requestId：请求id
     * errNo：错误码
     * errMsg：错误信息
     * meta：资产详情
     * saleList：资产售卖列表
     */
    public static class QueryItemResp {
        public long requestId;
        public int errNo;
        public String errMsg;
        public JSONObject meta;
        public JSONArray saleList;

        QueryItemResp(long requestId, int errNo, String errMsg, JSONObject meta, JSONArray saleList) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.meta = meta;
            this.saleList = saleList;
        }
    }


    /**
     * 微信支付
     */
    public final static int PAYBYWECHAT = 1;
    /**
     * 阿里支付
     */
    public final static int PAYBYALI = 2;
    /**
     * 特殊支付（比如免费）
     */
    public final static int PAYBYSPECIAL = 9;

    /**
     * 分润方信息
     *
     * profitAddr：分账区块链地址
     * reciverType：分账接收方类型
     * profitId：分账接收方账号
     * profitPercent：分润比例
     */
    public static class ProfitShareInfo {
        @JSONField(name = "profit_share_addr")
        public String profitAddr;
        @JSONField(name = "reciver_type")
        public String reciverType;
        @JSONField(name = "profit_id")
        public String profitId;
        @JSONField(name = "profit_percent")
        public int profitPercent;

        public ProfitShareInfo(String profitAddr, String reciverType, String profitId, int profitPercent) {
            this.profitAddr = profitAddr;
            this.reciverType = reciverType;
            this.profitId = profitId;
            this.profitPercent = profitPercent;
        }
    }

    /**
     * 微信支付信息
     *
     * wxPlatAppId：服务商应用id
     * wxPlatMchId：服务商户号
     * wxSellerMchId：子商户号
     * wxBuyerOpenId：用户服务标识
     */
    public static class WechatPay {
        @JSONField(name = "wx_plat_appid")
        public String wxPlatAppId;
        @JSONField(name = "wx_plat_mchid")
        public String wxPlatMchId;
        @JSONField(name = "wx_seller_mchid")
        public String wxSellerMchId;
        @JSONField(name = "wx_buyer_openid")
        public String wxBuyerOpenId;

        public WechatPay(String wxPlatAppId, String wxPlatMchId, String wxSellerMchId, String wxBuyerOpenId) {
            this.wxPlatAppId = wxPlatAppId;
            this.wxPlatMchId = wxPlatMchId;
            this.wxSellerMchId = wxSellerMchId;
            this.wxBuyerOpenId = wxBuyerOpenId;
        }
    }

    public static class PayInfoResp {
        public long requestId;
        public int errNo;
        public String errMsg;
        public JSONObject payInfo;

        PayInfoResp(long requestId, int errNo, String errMsg, JSONObject payInfo) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.payInfo = payInfo;
        }
    }

    /**
     * 查询订单详情
     * requestId：请求id
     * errNo：错误码
     * errMsg：错误信息
     * info：订单详情
     */
    public static class QueryOrderResp {
        public long requestId;
        public int errNo;
        public String errMsg;
        public JSONObject info;

        QueryOrderResp(long requestId, int errNo, String errMsg, JSONObject info) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.info = info;
        }
    }
}
