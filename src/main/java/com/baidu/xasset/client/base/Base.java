package com.baidu.xasset.client.base;

import com.alibaba.fastjson2.JSON;
import com.baidu.xasset.auth.Signer;
import com.baidu.xasset.client.base.BaseDef.RequestRes;
import com.baidu.xasset.common.config.Config.XassetCliConfig;
import com.baidu.xasset.common.httpcli.Client;
import com.baidubce.auth.BceCredentials;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.http.Headers;
import com.baidubce.http.HttpMethodName;
import com.baidubce.internal.InternalRequest;
import com.google.common.collect.Maps;

import javax.xml.bind.DatatypeConverter;
import java.net.URI;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用基础库
 */
public class Base {
    /**
     * 日志记录器
     */
    public static java.util.logging.Logger logger;
    private static XassetCliConfig cfg;

    /**
     * 通用基础服务
     *
     * @param config 通用基础配置
     * @param logger 日志记录器
     */
    public Base(XassetCliConfig config, java.util.logging.Logger logger) {
        if ((config == null) || !config.isValid()) {
            System.out.println("param invalid");
            return;
        }
        Base.cfg = config;
        Base.logger = logger;
    }

    /**
     * 获取通用基础配置
     *
     * @return {@link XassetCliConfig}
     */
    public static XassetCliConfig getConfig() {
        return cfg;
    }

    /**
     * 发送post请求
     *
     * @param uri  uri
     * @param data 数据
     * @return {@link RequestRes}
     * @throws Exception 异常
     */
    public static RequestRes post(String uri, Map<String, String> data) throws Exception {
        String reqUrl = String.format("%s%s", getConfig().EndPoint, uri);
        final URI u = new URI(reqUrl);
        MessageDigest md;
        md = MessageDigest.getInstance("MD5");
        String dataStr = JSON.toJSONString(data);
        final byte[] digest = md.digest(dataStr.getBytes("UTF-8"));
        HashMap<String, String> header = new HashMap<String, String>() {
            {
                put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
                put("Host", u.getHost());
                put("Timestamp", String.format("%d", (System.currentTimeMillis() / 1000L)));
                put("Content-Md5", DatatypeConverter.printHexBinary(digest));
            }
        };

        InternalRequest inReq = Client.genInternalRequest(HttpMethodName.POST, uri, data, header);
        BceCredentials cred = new DefaultBceCredentials(getConfig().Credentials.AccessKeyId,
                getConfig().Credentials.SecreteAccessKey);
        try {
            Signer.sign(inReq, cred, getConfig().SignOption);
        } catch (Exception e) {
            System.out.println("xasset access sign failed");
            throw (e);
        }

        String params = HttpUtil.genParamStr(inReq.getParameters());
        Map<String, String> headers = Maps.newHashMap();
        headers.put("Content-Type", header.get("Content-Type"));
        headers.put("Host", u.getHost());
        headers.put("Timestamp", String.format("%d", (System.currentTimeMillis() / 1000L)));
        headers.put("Authorization", inReq.getHeaders().get(Headers.AUTHORIZATION));
        headers.put("Content-Md5", header.get("Content-Md5"));

        return HttpUtil.post(reqUrl, params, headers);
    }
}
