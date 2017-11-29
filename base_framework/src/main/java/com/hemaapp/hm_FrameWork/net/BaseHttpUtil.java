package com.hemaapp.hm_FrameWork.net;

import com.hemaapp.PoplarConfig;
import com.hemaapp.hm_FrameWork.exception.DataParseException;
import com.hemaapp.hm_FrameWork.exception.HttpException;
import com.hemaapp.hm_FrameWork.util.FileTypeUtil;
import com.hemaapp.hm_FrameWork.util.JsonUtil;
import com.hemaapp.hm_FrameWork.util.HemaLogger;
import com.hemaapp.hm_FrameWork.util.StreamUtil;
import com.hemaapp.hm_FrameWork.util.TimeUtil;
import com.hemaapp.hm_FrameWork.util.Md5Util;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 网络请求相关工具类
 */
public class BaseHttpUtil {
    private static final String TAG = "BaseHttpUtil";
    public static String sessionID = null;

    private static final String END = "\r\n";
    private static final String TWOHYPHENS = "--";
    private static final String BOUNDARY = "yztdhr";

    /**
     * 上传文件
     *
     * @param path     请求接口
     * @param files    文件集合(<参数名,文件路径>)
     * @param params   其他参数集合(<参数名,参数值>)
     * @param encoding 编码方式
     * @return JSONObject
     * @throws HttpException
     * @throws DataParseException
     */
    public static JSONObject sendPOSTWithFilesForJSONObject(String path, HashMap<String, String> files, HashMap<String, String> params,
                                                            String encoding) throws DataParseException, HttpException {
        return JsonUtil.toJsonObject(sendPOSTWithFilesForString(path,
                files, params, encoding));
    }

    public static <T> T sendPOSTWithFilesForBaseResult(String path, HashMap<String, String> files, HashMap<String, String> params,
                                                       String encoding, Class<T> responseClass) {
        return null;
    }

    /**
     * 上传文件
     *
     * @param path     文件上传接口
     * @param files    文件集合(<参数名,文件路径>)
     * @param params   其他参数集合
     * @param encoding 编码方式
     * @return String
     * @throws IOException
     */
    public static String sendPOSTWithFilesForString(String path,
                                                    HashMap<String, String> files, HashMap<String, String> params,
                                                    String encoding) throws HttpException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(path);
            HemaLogger.d(TAG, "The HttpUrl is \n" + path);
            conn = (HttpURLConnection) url.openConnection();
            // 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
            // 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
            // 设置此参数后导致请求有时成功有时失败（网上说是服务器不支持,具体原因未明）
            // conn.setChunkedStreamingMode(0);// 128K 128 * 1024

            conn.setConnectTimeout(PoplarConfig.TIMEOUT_CONNECT_HTTP);
            conn.setReadTimeout(PoplarConfig.TIMEOUT_READ_FILE);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + BOUNDARY);

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", encoding);
            if (sessionID != null)
                conn.setRequestProperty("Cookie", sessionID);// 设置cookie
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            // 参数
            writeParams(path, dos, params, encoding);
            // 文件
            writeFiles(dos, files);
            dos.writeBytes(TWOHYPHENS + BOUNDARY + TWOHYPHENS + END);
            dos.flush();
            dos.close();
            String cookie = conn.getHeaderField("set-cookie");
            if (cookie != null)
                sessionID = cookie.substring(0, cookie.indexOf(";"));// 获取sessionID

            String data = StreamUtil.iputStreamToString(get(conn));
            HemaLogger.d(TAG, "The back data is \n" + data);
            if (conn != null)
                conn.disconnect();
            return data;
        } catch (Exception e) {
            if (conn != null)
                conn.disconnect();
            throw new HttpException(e);
        }

    }

    /**
     * 处理文件集
     *
     * @param dos   数据输出流
     * @param files 文件集
     * @throws IOException
     */
    private static void writeFiles(DataOutputStream dos, HashMap<String, String> files) throws IOException {
        for (Map.Entry<String, String> entry : files.entrySet()) {
            FileInputStream fStream = null;
            try {
                dos.writeBytes(TWOHYPHENS + BOUNDARY + END);
                dos.writeBytes("Content-Disposition: form-data; " + "name=\""
                        + entry.getKey() + "\";filename=\"" + entry.getValue()
                        + "\"" + END);
                HemaLogger.d(TAG, "The file path is \n" + entry.getValue());
                String filetype = FileTypeUtil.getFileTypeByPath(entry
                        .getValue());// 获取文件类型
                HemaLogger.d(TAG, "The file type is " + filetype);
                dos.writeBytes("Content-type: " + filetype + END);
                dos.writeBytes(END);
                int bufferSize = 1024 * 10;
                byte[] buffer = new byte[bufferSize];
                int length = -1;
                File file = new File(entry.getValue());
                fStream = new FileInputStream(file);
                while ((length = fStream.read(buffer)) != -1) {
                    dos.write(buffer, 0, length);
                }
                dos.writeBytes(END);
                // dos.writeBytes(TWOHYPHENS + BOUNDARY + TWOHYPHENS + END);
            } catch (IOException e) {
                throw e;
            } finally {
                if (fStream != null)
                    try {
                        fStream.close();
                    } catch (IOException e) {
                        throw e;
                    }
            }
        }
    }

    /**
     * 处理参数集
     *
     * @param dos      数据输出流
     * @param params   参数集
     * @param encoding 编码方式
     * @throws IOException
     */
    private static void writeParams(String path, DataOutputStream dos, HashMap<String, String> params, String encoding) throws IOException {
        StringBuilder data = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                // 方便查看发送参数，无实际意义
                data.append(entry.getKey()).append("=");
                data.append(entry.getValue());
                data.append("&");
                if (entry.getValue() != null) {
                    dos.writeBytes(TWOHYPHENS + BOUNDARY + END);
                    dos.writeBytes("Content-Disposition: form-data; "
                            + "name=\"" + entry.getKey() + "\"" + END);
                    dos.writeBytes(END);
                    dos.write(entry.getValue().getBytes(encoding));// writeBytes方法默认以ISO-8859-1编码,此处易出现汉字乱码问题
                    dos.writeBytes(END);
                    // dos.writeBytes(TWOHYPHENS + BOUNDARY + TWOHYPHENS + END);
                }
            }
            data.deleteCharAt(data.length() - 1);
            if (PoplarConfig.DIGITAL_CHECK) {  //开启数字签名
                String datetime = TimeUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss");
                data.append("&").append("datetime=").append(datetime).append("&");
                String[] tempPath = path.split("/");
                String sign = Md5Util.getMd5(PoplarConfig.DATAKEY + "|" + datetime + "|" + tempPath[tempPath.length - 1]);
                data.append("sign=").append(sign);
                dos.writeBytes(TWOHYPHENS + BOUNDARY + END);
                dos.writeBytes("Content-Disposition: form-data; "
                        + "name=\"datetime\"" + END);
                dos.writeBytes(END);
                dos.write(datetime.getBytes(encoding));// writeBytes方法默认以ISO-8859-1编码,此处易出现汉字乱码问题
                dos.writeBytes(END);

                dos.writeBytes(TWOHYPHENS + BOUNDARY + END);
                dos.writeBytes("Content-Disposition: form-data; "
                        + "name=\"sign\"" + END);
                dos.writeBytes(END);
                dos.write(sign.getBytes(encoding));// writeBytes方法默认以ISO-8859-1编码,此处易出现汉字乱码问题
                dos.writeBytes(END);
            }

        } else {
            if (PoplarConfig.DIGITAL_CHECK) {  //开启数字签名
                String datetime = TimeUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss");
                data.append("datetime=").append(datetime).append("&");
                String[] tempPath = path.split("/");
                String sign = Md5Util.getMd5(PoplarConfig.DATAKEY + "|" + datetime + "|" + tempPath[tempPath.length - 1]);
                data.append("sign=").append(sign);

                dos.writeBytes(TWOHYPHENS + BOUNDARY + END);
                dos.writeBytes("Content-Disposition: form-data; "
                        + "name=\"datetime\"" + END);
                dos.writeBytes(END);
                dos.write(datetime.getBytes(encoding));// writeBytes方法默认以ISO-8859-1编码,此处易出现汉字乱码问题
                dos.writeBytes(END);

                dos.writeBytes(TWOHYPHENS + BOUNDARY + END);
                dos.writeBytes("Content-Disposition: form-data; "
                        + "name=\"sign\"" + END);
                dos.writeBytes(END);
                dos.write(sign.getBytes(encoding));// writeBytes方法默认以ISO-8859-1编码,此处易出现汉字乱码问题
                dos.writeBytes(END);
            }
        }
        HemaLogger.d(TAG, "The send data is \n" + data.toString());
    }

    /**
     * 发送POST请求
     *
     * @param path   请求接口
     * @param params 发送参数集合(<参数名,参数值>)
     * @return JSONObject
     * @throws HttpException
     * @throws DataParseException
     */
    public static JSONObject sendPOSTForJSONObject(String path, HashMap<String, String> params, String encoding)
            throws DataParseException, HttpException {
//        return JsonUtil.toJsonObject(sendOkHttpPOSTForString(path, params, encoding));
        return JsonUtil.toJsonObject(sendPOSTForString(path, params, encoding));
    }

    /**
     * 发送POST请求
     *
     * @param path   请求接口
     * @param params 发送参数集合(<参数名,参数值>)
     * @return String
     * @throws HttpException
     * @throws IOException
     * @throws MalformedURLException
     */
    public static String sendPOSTForString(String path, HashMap<String, String> params, String encoding)
            throws HttpException {
        StringBuilder data = new StringBuilder();
        HemaLogger.d(TAG, "The HttpUrl is \n" + path);
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                data.append(entry.getKey()).append("=");
                String value;
                if (entry.getValue() != null) {
                    value = entry.getValue().replace("&", "%26"); // 转义&
                    value = value.toString().replace("+", "%2B");  //转义+
                } else {
                    value = entry.getValue();
                }
                data.append(value);
                data.append("&");
            }
            data.deleteCharAt(data.length() - 1);
            if (PoplarConfig.DIGITAL_CHECK) {  //开启数字签名
                String datetime = TimeUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss");
                data.append("&").append("datetime=").append(datetime).append("&");
                String[] tempPath = path.split("/");
                String sign = Md5Util.getMd5(PoplarConfig.DATAKEY + "|" + datetime + "|" + tempPath[tempPath.length - 1]);
                data.append("sign=").append(sign);
            }
        } else {
            if (PoplarConfig.DIGITAL_CHECK) {  //开启数字签名
                String datetime = TimeUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss");
                data.append("datetime=").append(datetime).append("&");
                String[] tempPath = path.split("/");
                String sign = Md5Util.getMd5(PoplarConfig.DATAKEY + "|" + datetime + "|" + tempPath[tempPath.length - 1]);
                data.append("sign=").append(sign);
            }
        }
        HemaLogger.d(TAG, "The send data is \n" + data.toString());
        HttpURLConnection conn = null;
        try {
            byte[] entity = data.toString().getBytes();
            conn = (HttpURLConnection) new URL(path).openConnection();
            conn.setConnectTimeout(PoplarConfig.TIMEOUT_CONNECT_HTTP);
            conn.setReadTimeout(PoplarConfig.TIMEOUT_READ_HTTP);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            // conn.setChunkedStreamingMode(0);
            conn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            conn.setUseCaches(false);
            conn.setRequestProperty("Charset", encoding);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length",
                    String.valueOf(entity.length));
            if (sessionID != null)
                conn.setRequestProperty("Cookie", sessionID);// 设置cookie
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.write(entity);
            dos.flush();
            dos.close();
            String cookie = conn.getHeaderField("set-cookie");
            if (cookie != null)
                sessionID = cookie.substring(0, cookie.indexOf(";"));// 获取sessionID

            int code = conn.getResponseCode();
            HemaLogger.d(TAG, "The responsecode is " + code);

            InputStream in = (code == HttpURLConnection.HTTP_OK) ? conn
                    .getInputStream() : null;

            String indata = StreamUtil.iputStreamToString(in);
            HemaLogger.d(TAG, "The back data is \n" + indata);
            if (conn != null)
                conn.disconnect();
            return indata;
        } catch (Exception e) {
            if (conn != null)
                conn.disconnect();
            throw new HttpException(e);
        }
    }


    public static String sendOkHttpPOSTForString(String path, HashMap<String, String> params, String encoding) {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder builder = new FormBody.Builder();
        HemaLogger.d(TAG, "The HttpUrl is \n" + path);
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String value;
                if (entry.getValue() != null) {
                    value = entry.getValue().replace("&", "%26"); // 转义&
                    value = value.toString().replace("+", "%2B");  //转义+
                } else {
                    value = entry.getValue();
                }
                builder.add(entry.getKey(), value);
            }
        }
        if (PoplarConfig.DIGITAL_CHECK) {  //开启数字签名
            String datetime = TimeUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss");
            builder.add("datetime", datetime);
            String[] tempPath = path.split("/");
            String sign = Md5Util.getMd5(PoplarConfig.DATAKEY + "|" + datetime + "|" + tempPath[tempPath.length - 1]);
            builder.add("sign", sign);
        }
        HemaLogger.d(TAG, "The send data is \n" + builder.toString());
        FormBody body = builder.build();
        Request request = new Request.Builder().url(path).post(body).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String data = response.body().string();
                HemaLogger.d("response", data);
                return data;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取服务器返回流
     *
     * @param conn 连接
     * @return InputStream or null
     * @throws IOException
     */
    private static InputStream get(HttpURLConnection conn) throws IOException {
        int code = conn.getResponseCode();
        HemaLogger.d(TAG, "The responsecode is " + code);
        return (code == HttpURLConnection.HTTP_OK) ? conn.getInputStream()
                : null;
    }

    /**
     * 清除Session
     */
    public static void clearSession() {
        sessionID = null;
    }

}
