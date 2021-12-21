package com.baidu.xasset.auth;

import com.baidubce.BceClientException;
import com.baidubce.auth.BceCredentials;
import com.baidubce.auth.SignOptions;
import com.baidubce.http.Headers;
import com.baidubce.internal.InternalRequest;
import com.baidubce.util.DateUtils;
import com.baidubce.util.HttpUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * BCE签名库
 */
public class Signer {
    private static final String BCE_AUTH_VERSION = "bce-auth-v1";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final int DEFAULT_EXPIRE_SECONDS = 1800;
    private static final Charset UTF8 = Charset.forName(DEFAULT_ENCODING);

    // Default headers to sign with the BCE signing protocol
    private static final Set<String> defaultHeadersToSign = Sets.newHashSet();
    private static final Joiner headerJoiner = Joiner.on('\n');
    private static final Joiner signedHeaderStringJoiner = Joiner.on(';');

    /**
     * 签名
     *
     * @param request     请求
     * @param credentials 凭证
     * @param options     选项
     */
    public static void sign(InternalRequest request, BceCredentials credentials, SignOptions options) {
        // Check param
        checkNotNull(request, "request should not be null");
        if ((credentials == null) || (options == null)) {
            throw new IllegalArgumentException("param error");
        }

        // Prepare parameters
        String ak = credentials.getAccessKeyId();
        String sk = credentials.getSecretKey();
        String signDate = DateUtils.formatAlternateIso8601Date(new Date());

        // Modify the sign time if it is not the default value but specified by client
        if (options.getTimestamp() != null) {
            signDate = DateUtils.formatAlternateIso8601Date(options.getTimestamp());
        }
        if (options.getHeadersToSign() == null) {
            options.setHeadersToSign(new HashSet<String>() {
                {
                    add("host");
                }
            });
        }
        if (options.getExpirationInSeconds() < 1) {
            options.setExpirationInSeconds(DEFAULT_EXPIRE_SECONDS);
        }

        // Prepare the canonical request components
        String authString = BCE_AUTH_VERSION + "/" + ak + "/" + signDate + "/" + options.getExpirationInSeconds();
        String signKey = null;
        signKey = sha256Hex(sk, authString);

        // Generate signed header and signature
        List<String> res = getSignature(request, options, signKey);
        String signedHeaders = res.get(0);
        String signature = res.get(1);

        String authorizationHeader = authString + "/" + signedHeaders + "/" + signature;

        // Set header authorization field
        request.addHeader(Headers.AUTHORIZATION, authorizationHeader);
    }

    /**
     * 检查BCE签名
     *
     * @param request     请求
     * @param credentials 凭证
     */
    static void checkSign(InternalRequest request, BceCredentials credentials) {
        // Check param
        checkNotNull(request, "request should not be null");
        if (credentials == null) {
            throw new IllegalArgumentException("param invalid");
        }

        // 1. Check the format of authorization of header
        String author = request.getHeaders().get(Headers.AUTHORIZATION);
        List<String> authStrs = Arrays.asList(author.split("/"));
        if (authStrs.size() != 6) {
            throw new IllegalArgumentException("author format size error.auth:" + author);
        }
        if ((authStrs.get(0) == null) || (!authStrs.get(0).equals(BCE_AUTH_VERSION))) {
            throw new IllegalArgumentException("author formart version error.auth:" + author);
        }

        // 2. Check if signature is expired or not
        int expirationInSeconds = Integer.parseInt(authStrs.get(3));
        if ((expirationInSeconds < 0) || (expirationInSeconds > 3600)) {
            throw new IllegalArgumentException("author sign expiration set errro.author:" + author);
        }

        Date timestamp = DateUtils.parseAlternateIso8601Date(authStrs.get(2));
        if (timestamp.getTime() + expirationInSeconds < (System.currentTimeMillis() / 1000L)) {
            throw new IllegalArgumentException("author sign expiration.auth:" + author);
        }

        // 3. Check signed header
        List<String> headers = Arrays.asList(authStrs.get(4).split(";"));
        HashSet<String> HeadersToSign = new HashSet<>(headers);
        if (!headers.contains("host")) {
            throw new IllegalArgumentException("author sign headers unset host.auth:" + author);
        }

        SignOptions opt = new SignOptions();
        opt.setHeadersToSign(HeadersToSign);
        opt.setTimestamp(timestamp);

        // 4. Check digest sign
        String signingKey = sha256Hex(credentials.getSecretKey(), String.join("/", authStrs.subList(0, 4)));
        List<String> res = getSignature(request, opt, signingKey);
        String signature = res.get(1);
        if (!signature.equals(authStrs.get(5))) {
            throw new AssertionError("check signature failed.sign:" + signature + "-" + authStrs.get(5));
        }
    }

    private static List<String> getSignature(InternalRequest request, SignOptions options, String signKey) {
        // Formatting the URI with signing protocols
        String canonicalURI = getCanonicalURIPath(request.getUri().getPath());
        // Formatting the query string with signing protocol
        Map<String, String> queryParams = new HashMap<String, String>();
        if (request.getUri().getRawQuery() != null && !request.getUri().getRawQuery().isEmpty()) {
            String[] querys = request.getUri().getRawQuery().split("&");
            for (String query : querys) {
                if (query.equals("")) {
                    break;
                }
                List<String> param = Arrays.asList(query.split("="));
                if (param.size() == 1) {
                    queryParams.put(param.get(0), "");
                } else {
                    queryParams.put(param.get(0), param.get(1));
                }
            }
        }
        String canonicalQueryString = HttpUtils.getCanonicalQueryString(queryParams, true);
        // Sorted the headers should be signed from the request.
        SortedMap<String, String> headersToSign = getHeadersToSign(request.getHeaders(), options.getHeadersToSign());
        // Formatting the headers from the request based on signing protocol.
        String canonicalHeader = getCanonicalHeaders(headersToSign);
        String signedHeaders = "";
        if (options.getHeadersToSign() != null) {
            signedHeaders = signedHeaderStringJoiner.join(headersToSign.keySet());
            signedHeaders = signedHeaders.trim().toLowerCase();
        }

        String canonicalRequest = String.format("%s\n%s\n%s\n%s", request.getHttpMethod(), canonicalURI,
                canonicalQueryString, canonicalHeader);
        // Signing the canonical request using key with sha-256 algorithm.
        String signature = sha256Hex(signKey, canonicalRequest);
        return Arrays.asList(signedHeaders, signature);
    }

    private static String getCanonicalURIPath(String path) {
        if (path == null) {
            return "/";
        } else if (path.startsWith("/")) {
            return HttpUtils.normalizePath(path);
        } else {
            return "/" + HttpUtils.normalizePath(path);
        }
    }

    private static String getCanonicalHeaders(SortedMap<String, String> headers) {
        if (headers.isEmpty()) {
            return "";
        }

        List<String> headerStrings = Lists.newArrayList();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            String value = entry.getValue();
            if (value == null) {
                value = "";
            }
            headerStrings.add(HttpUtils.normalize(key.trim().toLowerCase()) + ':' + HttpUtils.normalize(value.trim()));
        }
        Collections.sort(headerStrings);

        return headerJoiner.join(headerStrings);
    }

    private static SortedMap<String, String> getHeadersToSign(Map<String, String> headers, Set<String> headersToSign) {
        SortedMap<String, String> ret = Maps.newTreeMap();
        if (headersToSign != null) {
            Set<String> tempSet = Sets.newHashSet();
            for (String header : headersToSign) {
                tempSet.add(header.trim().toLowerCase());
            }
            headersToSign = tempSet;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if ((headersToSign == null && isDefaultHeaderToSign(key)) || (headersToSign != null
                        && headersToSign.contains(key.toLowerCase()) && !Headers.AUTHORIZATION.equalsIgnoreCase(key))) {
                    ret.put(key, entry.getValue());
                }
            }
        }
        return ret;
    }

    private static boolean isDefaultHeaderToSign(String header) {
        header = header.trim().toLowerCase();
        return header.startsWith(Headers.BCE_PREFIX) || defaultHeadersToSign.contains(header);
    }

    private static String sha256Hex(String signingKey, String stringToSign) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingKey.getBytes(UTF8), "HmacSHA256"));
            return new String(Hex.encodeHex(mac.doFinal(stringToSign.getBytes(UTF8))));
        } catch (Exception e) {
            throw new BceClientException("Fail to generate the signature", e);
        }
    }
}
