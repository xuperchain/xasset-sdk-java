package com.baidu.xasset.client.base;

/**
 * 通用基础服务定义
 */
public class BaseDef {
    /**
     * 错误码
     */
    public final static int ERRNOSUCC = 0;

    /**
     * 分页拉取每页数量最大限制
     */
    public final static int MAXLIMIT = 50;

    public static class RequestRes {
        public int httpCode;
        public String reqUrl;
        public String traceId;
        public String body;

        RequestRes(int httpCode, String reqUrl, String traceId, String body) {
            this.httpCode = httpCode;
            this.reqUrl = reqUrl;
            this.traceId = traceId;
            this.body = body;
        }
    }

    public static class BaseResp {
        public long requestId;
        public int errNo;
        public String errMsg;

        public BaseResp(long requestId, int errNo, String errMsg) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
        }
    }

    /**
     * 通用接口返回值
     *
     * apiResp      请求接口返回值
     * res          http请求返回值
     */
    public static class Resp<T> {
        public T apiResp;
        public RequestRes res;

        public Resp(T resp, RequestRes res) {
            this.apiResp = resp;
            this.res = res;
        }
    }

    /**
     * 列表游标式分页返回值
     *
     * requestId    请求id
     * errNo        错误码
     * errMsg       错误信息
     * list         数据列表
     * hasMore      是否有更多数据。0：没有 1：有
     * cursor       分页游标。下次请求时带上
     */
    public static class ListCursorResp<T> {
        public long requestId;
        public int errNo;
        public String errMsg;
        public T list;
        public int hasMore;
        public String cursor;

        public ListCursorResp(long requestId, int errNo, String errMsg, T list, int hasMore,
                              String cursor) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.list = list;
            this.hasMore = hasMore;
            this.cursor = cursor;
        }
    }

    /**
     * 列表页码式分页返回值
     *
     * requestId    请求id
     * errNo        错误码
     * errMsg       错误信息
     * list         数据列表
     * totalCnt     总数据数量
     */
    public static class ListPageResp<T> {
        public long requestId;
        public int errNo;
        public String errMsg;
        public T list;
        public int totalCnt;

        public ListPageResp(long requestId, int errNo, String errMsg, T list, int totalCnt) {
            this.requestId = requestId;
            this.errNo = errNo;
            this.errMsg = errMsg;
            this.list = list;
            this.totalCnt = totalCnt;
        }
    }
}
