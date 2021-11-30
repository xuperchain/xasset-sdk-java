package com.baidu.xasset.client.base;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * http组件
 */
public class HttpUtil {

    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.97 Safari/537.36";
    private final static String BOUNDARY = UUID.randomUUID().toString()
            .toLowerCase().replaceAll("-", "");// 边界标识
    private final static String PREFIX = "--";// 必须存在
    private final static String LINE_END = "\r\n";


    static String get(String url, Map<String, String> params, Map<String, String> headers) throws IOException {

        String paramsStr = genParamStr(params);
        return get(url, paramsStr, headers);
    }


    private static String get(String url, String param, Map<String, String> headers) throws IOException {

        BufferedReader in = null;
        StringBuilder builder = new StringBuilder();
        try {
            String urlNameString = url;
            if (param != null) {
                urlNameString += "?" + param;
            }
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", USER_AGENT);
            if (headers != null) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 定义 BufferedReader输入流来读取URL的响应
                in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), DEFAULT_CHARSET));
                String line;
                while ((line = in.readLine()) != null) {
                    builder.append(line).append("\n");
                }
            } else {
                String str = String.format("get request fail, responseCode : %s, message : %s",
                        conn.getResponseCode(), conn.getResponseMessage());
                System.out.println(str);
            }
        } catch (IOException e) {
            System.out.println("get request fail");
            throw e;
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                System.out.println("HttpUtil.get finally error.");
            }
        }
        return builder.toString();
    }

    /**
     * 向指定URL发送GET方法的请求(不带参数，可以设置头信息)
     *
     * @param url     发送请求的 URL
     * @param headers 请求参数。
     *                编码自行处理
     * @return 所代表远程资源的响应结果
     */
    public static String get(String url, Map<String, String> headers) throws IOException {

        return get(url, "", headers);
    }

    static BaseDef.RequestRes post(String url, String params, Map<String, String> headers) throws IOException {

        String result = "{}";

        // post参数
        byte[] postDataBytes = params.toString().getBytes(DEFAULT_CHARSET);

        //开始访问
        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        if (headers != null) {

            for (Map.Entry<String, String> header : headers.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        conn.setConnectTimeout(2000);
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), DEFAULT_CHARSET));

        StringBuilder sb = new StringBuilder();
        for (int c; (c = in.read()) >= 0; ) {
            sb.append((char) c);
        }
        in.close();
        conn.disconnect();

        String traceId = conn.getHeaderField("X-Trace-Id");
        return new BaseDef.RequestRes(conn.getResponseCode(), url, traceId, sb.toString());
    }

    public static String multiPartPost(String requestUrl,
                                       Map<String, Object> params, Map<String, String> requestFile, Map<String, String> headers) throws Exception {
        HttpURLConnection conn = null;
        InputStream input;
        OutputStream os = null;
        BufferedReader br = null;
        StringBuffer buffer;
        try {
            URL url = new URL(requestUrl);
            conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(1000 * 10);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    conn.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            conn.connect();

            // 往服务器端写内容 也就是发起http请求需要带的参数
            os = new DataOutputStream(conn.getOutputStream());
            // 请求参数部分
            writeParams(params, os);
            // 请求上传文件部分
            writeFile(requestFile, os);
            // 请求结束标志
            String endTarget = PREFIX + BOUNDARY + PREFIX + LINE_END;
            os.write(endTarget.getBytes());
            os.flush();

            // 读取服务器端返回的内容
            if (conn.getResponseCode() == 200) {
                input = conn.getInputStream();
            } else {
                input = conn.getErrorStream();
            }

            br = new BufferedReader(new InputStreamReader(input, DEFAULT_CHARSET));
            buffer = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }

        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                    conn = null;
                }

                if (os != null) {
                    os.close();
                    os = null;
                }

                if (br != null) {
                    br.close();
                    br = null;
                }
            } catch (IOException ex) {
                throw new Exception(ex);
            }
        }
        return buffer.toString();
    }

    private static void writeParams(Map<String, Object> params,
                                    OutputStream os) throws Exception {
        try {
            if (params != null && !params.isEmpty()) {
                StringBuilder requestParams = new StringBuilder();

                for (Entry<String, Object> param : params.entrySet()) {
                    requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    requestParams.append("Content-Disposition: form-data; name=\"")
                            .append(param.getKey()).append("\"").append(LINE_END);
                    requestParams.append("Content-Type: text/plain; charset=utf-8")
                            .append(LINE_END);
                    requestParams.append("Content-Transfer-Encoding: 8bit").append(
                            LINE_END);
                    requestParams.append(LINE_END);// 参数头设置完以后需要两个换行，然后才是参数内容
                    requestParams.append(param.getValue());
                    requestParams.append(LINE_END);
                }
                os.write(requestParams.toString().getBytes());
                os.flush();
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    private static void writeFile(Map<String, String> requestFile,
                                  OutputStream os) throws Exception {
        InputStream is = null;
        try {
            if (requestFile != null && !requestFile.isEmpty()) {
                StringBuilder requestParams = new StringBuilder();

                for (Entry<String, String> fileInfo : requestFile.entrySet()) {

                    File file = new File(fileInfo.getValue());
                    requestParams.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    requestParams.append("Content-Disposition: form-data; name=\"")
                            .append(fileInfo.getKey()).append("\"; filename=\"")
                            .append(file.getName()).append("\"")
                            .append(LINE_END);
                    requestParams.append("Content-Type:")
                            .append(getContentType(file))
                            .append(LINE_END);
                    requestParams.append("Content-Transfer-Encoding: 8bit").append(
                            LINE_END);
                    requestParams.append(LINE_END);// 参数头设置完以后需要两个换行，然后才是参数内容

                    os.write(requestParams.toString().getBytes());

                    is = new FileInputStream(file);

                    byte[] buffer = new byte[1024 * 1024];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                    os.write(LINE_END.getBytes());
                    os.flush();
                }
            }
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                throw new Exception(e);
            }
        }
    }

    public static String getContentType(File file) throws Exception {
        String streamContentType = "application/octet-stream";
        String imageContentType = "";
        ImageInputStream image = null;
        try {
            image = ImageIO.createImageInputStream(file);
            if (image == null) {
                return streamContentType;
            }
            Iterator<ImageReader> it = ImageIO.getImageReaders(image);
            if (it.hasNext()) {
                imageContentType = "image/" + it.next().getFormatName();
                return imageContentType;
            }
        } catch (IOException e) {
            throw new Exception(e);
        } finally {
            try {
                if (image != null) {
                    image.close();
                }
            } catch (IOException e) {
                throw new Exception(e);
            }
        }
        return streamContentType;
    }

    static String genParamStr(Map<String, String> params) throws UnsupportedEncodingException {

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (postData.length() != 0) {
                postData.append('&');
            }
            postData.append(URLEncoder.encode(param.getKey(), DEFAULT_CHARSET));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), DEFAULT_CHARSET));
        }
        return postData.toString();
    }
}
