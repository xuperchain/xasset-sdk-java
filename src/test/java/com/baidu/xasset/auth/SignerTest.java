//package com.baidu.xasset.auth;
//
//import com.baidubce.auth.BceCredentials;
//import com.baidubce.auth.DefaultBceCredentials;
//import com.baidubce.auth.SignOptions;
//import com.baidubce.http.HttpMethodName;
//import com.baidubce.internal.BaseRequest;
//import com.baidubce.internal.InternalRequest;
//import org.junit.Test;
//
//import java.util.*;
//
//public class SignerTest {
//    static String ak = "xxx";
//    static String sk = "xxx";
//    static BceCredentials cred = new DefaultBceCredentials(ak, sk);
//
//    @Test
//    public void TestSign() {
//        SignOptions opt = new SignOptions();
//        opt.setTimestamp(new Date(System.currentTimeMillis() / 1000L));
//        opt.setExpirationInSeconds(1800);
//        opt.setHeadersToSign(null);
//
//        List<reqInfos> args = Arrays.asList(
//                new reqInfos(HttpMethodName.POST, "http://www.baidu.com", new HashMap<String, String>()),
//                new reqInfos(HttpMethodName.POST, "http://www.baidu.com/?toke=123", new HashMap<String, String>()),
//                new reqInfos(HttpMethodName.POST, "http://www.baidu.com/?toke=123&name=林&age=",
//                        new HashMap<String, String>() {
//                            {
//                                put("addr", "0xsss");
//                            }
//                        }));
//
//        for (reqInfos arg : args) {
//            BaseRequest request = new BaseRequest(arg.method, arg.url, arg.body,
//                    new HashMap<String, String>() {
//                        {
//                            put("Content-Type", "application/json");
//                            put("X-Bce-Request-Id", "15304c2e-381b-45ab-bc1e-488b183f4293");
//                            put("Host", "www.baidu.com");
//                        }
//                    });
//
//            System.out.println(request.getHeaders());
//            InternalRequest internalReq = BaseRequest.toInternalRequest(request);
//            try {
//                Signer.sign(internalReq, cred, opt);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            System.out.println(internalReq);
//        }
//        ;
//    }
//
//    @Test
//    public void TestCheckSign() {
//        SignOptions opt = new SignOptions();
//        opt.setTimestamp(new Date(System.currentTimeMillis() / 1000L));
//        opt.setExpirationInSeconds(1800);
//        opt.setHeadersToSign(new HashSet<String>() {
//            {
//                add("host");
//            }
//        });
//
//        List<reqInfos> args = Arrays.asList(
//                new reqInfos(HttpMethodName.POST, "http://www.baidu.com", new HashMap<String, String>()),
//                new reqInfos(HttpMethodName.POST, "http://www.baidu.com/?toke=123", new HashMap<String, String>()),
//                new reqInfos(HttpMethodName.POST, "http://www.baidu.com/?toke=123&name=林&age=",
//                        new HashMap<String, String>() {
//                            {
//                                put("addr", "0xsss");
//                            }
//                        }));
//
//        for (reqInfos arg : args) {
//            BaseRequest request = new BaseRequest(arg.method, arg.url, arg.body,
//                    new HashMap<String, String>() {
//                        {
//                            put("Content-Type", "application/json");
//                            put("X-Bce-Request-Id", "15304c2e-381b-45ab-bc1e-488b183f4293");
//                            put("Host", "www.baidu.com");
//                        }
//                    });
//
//            InternalRequest internalReq = BaseRequest.toInternalRequest(request);
//            try {
//                Signer.sign(internalReq, cred, opt);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            Signer.checkSign(internalReq, cred);
//        }
//        ;
//    }
//
//    class reqInfos {
//        HttpMethodName method;
//        String url;
//        Map<String, String> body;
//
//        reqInfos(HttpMethodName method, String url, Map<String, String> body) {
//            this.method = method;
//            this.url = url;
//            this.body = body;
//        }
//    }
//}
