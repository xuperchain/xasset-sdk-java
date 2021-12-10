package com.baidu.xasset.common.config;

import com.baidubce.auth.SignOptions;

import java.time.Duration;
import java.util.Date;
import java.util.HashSet;

public class Config {
    public static int ReadWriteTimeoutMsDef = 3000;
    /**
     * 联调环境
     */
    private static String EndPointDefault = "http://120.48.16.137:8360";
    private static String UserAgentDefault = "xasset-sdk-java";
    private static Duration ConnectTimeoutMsDef = Duration.ofMillis(1000);

    static public class Credentials {
        public long AppId;
        public String AccessKeyId;
        public String SecreteAccessKey;

        public Credentials(long appId, String ak, String sk) {
            this.AppId = appId;
            this.AccessKeyId = ak;
            this.SecreteAccessKey = sk;
        }
    }

    static public class XassetCliConfig {
        public String EndPoint;
        public String UserAgent;
        public Credentials Credentials;
        public SignOptions SignOption;
        public Duration ConnectTimeoutMs;

        public XassetCliConfig() {
            this.EndPoint = EndPointDefault;
            this.UserAgent = UserAgentDefault;
            this.SignOption = new SignOptions() {
                {
                    setHeadersToSign(new HashSet<String>() {
                        {
                            add("host");
                            add("content-type");
                            add("content-length");
                            add("content-md5");
                        }
                    });
                    setTimestamp(new Date());
                    setExpirationInSeconds(DEFAULT_EXPIRATION_IN_SECONDS);
                }
            };
            this.ConnectTimeoutMs = ConnectTimeoutMsDef;
        }

        public void setCredentials(long appId, String ak, String sk) {
            this.Credentials = new Credentials(appId, ak, sk);
        }

        public void setEndPoint(String endpoint) {
            this.EndPoint = endpoint;
        }

        public String string() {
            return String.format(
                    "[Endpoint:%s] [UserAgent:%s] [Credentials:%s] [SignOption:%s] " + "[ConnectTimeoutMs:%dms]",
                    this.EndPoint, this.UserAgent, this.Credentials, this.SignOption,
                    this.ConnectTimeoutMs.getSeconds() * 1000L);
        }

        public boolean isValid() {
            if ((this.EndPoint.equals("") || (this.Credentials == null) || (this.SignOption == null))) {
                return false;
            }

            if (this.UserAgent.equals("")) {
                this.UserAgent = UserAgentDefault;
            }

            if (this.ConnectTimeoutMs == null) {
                this.ConnectTimeoutMs = ConnectTimeoutMsDef;
            }
            return true;
        }
    }

}
