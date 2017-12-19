package com.hemaapp;

/**
 * 该项目配置信息
 */
public class PoplarConfig {
    /**
     * 是否打印信息
     */
    public static boolean LOG = true;
    /**
     * 网络请求尝试次数
     */
    public static final int TRYTIMES_HTTP = 1;
    /**
     * 图片请求尝试次数
     */
    public static int TRYTIMES_IMAGE = 3;
    /**
     * 网络请求编码方式
     */
    public static String ENCODING = "UTF-8";
    /**
     * 网络请求连接超时时限(单位:毫秒)
     */
    public static final int TIMEOUT_HTTP = 30000;
    /**
     * 网络请求连接超时时限(单位:毫秒)
     */
    public static int TIMEOUT_CONNECT_HTTP = 10000;
    /**
     * 网络请求读取超时时限(单位:毫秒)
     */
    public static int TIMEOUT_READ_HTTP = 10000;
    /**
     * 图片缓存（外部缓存）的最大数量
     */
    public static int IMAGES_EXTERNAL = 200;
    /**
     * 图片缓存（内部缓存）的最大数量
     */
    public static int IMAGES_INTERNAL = 100;
    /**
     * 图片请求连接超时时限(单位:毫秒)
     */
    public static int TIMEOUT_CONNECT_IMAGE = 10000;
    /**
     * 图片请求读取超时时限(单位:毫秒)
     */
    public static int TIMEOUT_READ_IMAGE = 10000;
    /**
     * 文件下载请求连接超时时限(单位:毫秒)
     */
    public static int TIMEOUT_CONNECT_FILE = 10000;
    /**
     * 文件下载读取超时时限(单位:毫秒)
     */
    public static int TIMEOUT_READ_FILE = 1000000;
    /**
     * 是否只在WIFI下下载图片
     */
    public static boolean IMAGELOAD_ONLYWIFI = false;

    /**
     * 图片压缩的最大值（单位k,默认值200）
     */
    public static int MAX_IMAGE_SIZE = 200;

    /**
     * 是否开启数字验签
     */
    public static boolean DIGITAL_CHECK = false;

    /**
     * DATAKEY为服务器分配的项目唯一字符串
     */
    public static String DATAKEY = "";

    /**
     * 密码使用的公钥
     */
    public static String PASSWORD_KEY = "";

    public static String APP_ID = "";

    /**
     * 登录请求id
     */
    public static final int ID_LOGIN = -1;
    /**
     * 第三方登录请求id
     */
    public static final int ID_THIRDSAVE = -2;
    /**
     * 是否启用友盟统计
     */
    public static boolean UMENG_ENABLE = false;
    /**
     * 是否显示网络不给力异常Toast提示
     */
    public static boolean TOAST_NET_ENABLE = true;
    /**
     * 加载大图时最大边长
     */
    public static final int DEFAULT_IMAGE_SIZE = 1024;
}
