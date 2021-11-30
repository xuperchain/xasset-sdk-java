package com.baidu.xasset.client.xmall;

public class Api {
    // market 请求
    final static String SELLITEM = "/xasset/market/v1/sell";
    final static String WITHDRAWITEM = "/xasset/market/v1/withdraw";
    final static String LISTITEMBYFILTER = "/xasset/market/v1/listbyfilter";
    final static String QUERYITEM = "/xasset/market/v1/query";
    final static String LISTITEMS = "/xasset/market/v1/listitems";

    // order 请求
    final static String CREATEORDER = "/xasset/order/v1/create";
    final static String CANCELORDER = "/xasset/order/v1/cancel";
    final static String DELETEORDER = "/xasset/order/v1/delete";
    final static String GETORDERPAYINFO = "/xasset/order/v1/pay";
    final static String QUERYORDER = "/xasset/order/v1/query";
    final static String QUERYORDERLIST = "/xasset/order/v1/list";
    final static String PAYNOTIFYORDER = "/xasset/order/v1/paynotify";
}
