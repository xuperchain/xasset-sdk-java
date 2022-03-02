package com.baidu.xasset.client.xasset;

/**
 * 登记SaaS服务接口
 */
class Api {
    final static String GETSTOKEN = "/xasset/file/v1/getstoken";

    final static String CREATEASSET = "/xasset/horae/v1/create";
    final static String ALTERASSET = "/xasset/horae/v1/alter";
    final static String PUBLISHASSET = "/xasset/horae/v1/publish";
    final static String QUERYASSET = "/xasset/horae/v1/query";
    final static String LISTASSETBYADDR = "/xasset/horae/v1/listastbyaddr";
    final static String GRANTSHARD = "/xasset/horae/v1/grant";
    final static String TRANASFERSHARD = "/xasset/damocles/v1/transfer";
    final static String QUERYSHARDS = "/xasset/horae/v1/querysds";
    final static String LISTSHARDSBYADDR = "/xasset/horae/v1/listsdsbyaddr";
    final static String LISTSHARDSBYASSET = "/xasset/horae/v1/listsdsbyast";
    final static String HISTORYASSET = "/xasset/horae/v1/history";
    final static String GETEVIDENCEINFO = "/xasset/horae/v1/getevidenceinfo";
    final static String FREEZEASSET = "/xasset/horae/v1/freeze";
    final static String CONSUMESHARD = "/xasset/horae/v1/consume";
    final static String SHARDINCIRCULATION = "/xasset/horae/v1/srdscir";
}
