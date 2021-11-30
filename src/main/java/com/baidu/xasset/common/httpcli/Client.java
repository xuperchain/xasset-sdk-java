package com.baidu.xasset.common.httpcli;

import com.baidubce.http.HttpMethodName;
import com.baidubce.internal.BaseRequest;
import com.baidubce.internal.InternalRequest;

import java.util.Map;

public class Client {
    public static InternalRequest genInternalRequest(HttpMethodName method, String url, Map<String, String> body,
                                                     Map<String, String> header) {
        BaseRequest request = new BaseRequest(method, url, body, header);
        return BaseRequest.toInternalRequest(request);
    }

}
