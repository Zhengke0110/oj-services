package fun.timu.oj.common.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Slf4j
public class CommonUtil {

    /**
     * 获取用户真实IP地址，适用于处理通过代理的情况
     * 该方法尝试从请求头中获取IP地址，如果获取失败或IP地址无效，则尝试从其他请求头中获取
     * 如果所有请求头都未能提供有效的IP地址，则返回服务器直接获取的客户端IP地址
     * 在处理通过多个代理的情况时，该方法会提取出第一个有效的IP地址作为用户真实IP地址
     *
     * @param request 用户请求对象，用于获取请求头信息
     * @return 用户真实IP地址，如果无法获取，则返回空字符串
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        try {
            // 尝试从请求头中获取IP地址
            ipAddress = request.getHeader("x-forwarded-for");
            // 如果获取失败或IP地址无效，则尝试从其他请求头中获取
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                // 如果所有请求头都未能提供有效的IP地址，则直接获取客户端IP地址
                ipAddress = request.getRemoteAddr();
                // 如果获取的IP地址为本地回环地址，则尝试获取本机配置的IP地址
                if (ipAddress.equals("127.0.0.1")) {
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，提取出第一个有效的IP地址作为用户真实IP地址
            if (ipAddress != null && ipAddress.length() > 15) {
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            // 如果发生异常，则返回空字符串
            ipAddress = "";
        }
        return ipAddress;
    }


    /**
     * 获取全部请求头
     *
     * @param request HttpServletRequest对象，用于获取请求头信息
     * @return 返回一个Map对象，其中包含所有的请求头信息
     */
    public static Map<String, String> getAllRequestHeader(HttpServletRequest request) {
        // 获取请求头的名称集合
        Enumeration<String> headerNames = request.getHeaderNames();
        // 创建一个HashMap用于存储请求头的键值对
        Map<String, String> map = new HashMap<>();
        // 遍历请求头的名称集合
        while (headerNames.hasMoreElements()) {
            // 获取下一个请求头的名称
            String key = (String) headerNames.nextElement();
            // 根据名称获取请求头的值
            String value = request.getHeader(key);
            // 将请求头的键值对存入Map中
            map.put(key, value);
        }

        // 返回包含所有请求头信息的Map对象
        return map;
    }


    /**
     * MD5加密
     *
     * @param data 待加密的数据
     * @return 加密后的数据，如果加密过程中发生异常则返回null
     */
    public static String MD5(String data) {
        try {
            // 获取MD5加密器实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 对数据进行加密
            byte[] array = md.digest(data.getBytes("UTF-8"));
            // 创建字符串构建器用于拼接加密后的数据
            StringBuilder sb = new StringBuilder();
            // 遍历字节数组，将每个字节转换为十六进制字符串并拼接
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            }
            // 返回转换为大写后的加密字符串
            return sb.toString().toUpperCase();
        } catch (Exception exception) {
            // 异常处理：如果发生异常则返回null
        }
        return null;
    }


    /**
     * 获取验证码随机数
     * <p>
     * 该方法用于生成指定长度的随机数字字符串，常用于生成验证码
     *
     * @param length 验证码的长度，即随机生成的数字字符串的长度
     * @return 生成的随机数字字符串
     */
    public static String getRandomCode(int length) {

        // 定义随机数源，这里只包含数字
        String sources = "0123456789";
        // 创建Random对象用于生成随机数
        Random random = new Random();
        // 使用StringBuilder来构建最终的随机数字符串
        StringBuilder sb = new StringBuilder();
        // 循环length次，每次从sources中随机取一个字符添加到sb中
        for (int j = 0; j < length; j++) {
            // random.nextInt(9)生成一个0到8之间的随机数，用于从sources中取字符
            sb.append(sources.charAt(random.nextInt(9)));
        }
        // 将StringBuilder对象转换为String并返回
        return sb.toString();
    }


    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳，单位为毫秒
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }


    /**
     * 生成uuid
     *
     * @return 生成的UUID字符串，无连字符，长度为32
     */
    public static String generateUUID() {
        // 使用UUID的randomUUID方法生成一个全局唯一标识符
        // 转换为字符串后移除所有连字符，然后截取前32个字符
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
    }

    /**
     * 获取随机长度的串
     *
     * @param length
     * @return
     */
    private static final String ALL_CHAR_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * 生成指定长度的随机字符串，包含数字和字母
     *
     * @param length 随机字符串的长度
     * @return 生成的随机字符串
     */
    public static String getStringNumRandom(int length) {
        // 生成随机数字和字母,
        Random random = new Random();
        // 初始化StringBuilder，用于拼接生成的随机字符串
        StringBuilder saltString = new StringBuilder(length);
        // 循环指定次数，每次随机选取一个字符添加到StringBuilder中
        for (int i = 1; i <= length; ++i) {
            // 从ALL_CHAR_NUM中随机选取一个字符，并将其添加到saltString中
            saltString.append(ALL_CHAR_NUM.charAt(random.nextInt(ALL_CHAR_NUM.length())));
        }
        // 将生成的随机字符串转换为String类型并返回
        return saltString.toString();
    }


    /**
     * 响应json数据给前端
     *
     * @param response
     * @param obj
     */
    public static void sendJsonMessage(HttpServletResponse response, Object obj) {

        response.setContentType("application/json; charset=utf-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.print(JsonUtil.obj2Json(obj));
            response.flushBuffer();

        } catch (IOException e) {
            log.warn("响应json数据给前端异常:{}", e);
        }


    }

    /**
     * 计算字符串的MurmurHash32值（不使用Guava库）
     * MurmurHash是一个非密码学的哈希函数，适用于一般的数据结构和算法中
     * 它以其良好的分布性、高性能和低碰撞率而著称
     *
     * @param param 需要计算哈希值的字符串
     * @return 计算得到的哈希值
     */
    public static long murmurHash32(String param) {
        byte[] data = param.getBytes(StandardCharsets.UTF_8);
        int seed = 0x9747b28c; // 使用固定的种子值

        // MurmurHash3算法实现
        final int c1 = 0xcc9e2d51;
        final int c2 = 0x1b873593;

        int h1 = seed;
        int length = data.length;
        int roundedEnd = length & 0xfffffffc; // 向下取整到4的倍数

        // 主体部分，每次处理4个字节
        for (int i = 0; i < roundedEnd; i += 4) {
            int k1 = (data[i] & 0xff) |
                    ((data[i + 1] & 0xff) << 8) |
                    ((data[i + 2] & 0xff) << 16) |
                    ((data[i + 3] & 0xff) << 24);

            k1 *= c1;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= c2;

            h1 ^= k1;
            h1 = Integer.rotateLeft(h1, 13);
            h1 = h1 * 5 + 0xe6546b64;
        }

        // 处理剩余字节
        int k1 = 0;
        switch (length & 0x03) {
            case 3:
                k1 = (data[roundedEnd + 2] & 0xff) << 16;
            case 2:
                k1 |= (data[roundedEnd + 1] & 0xff) << 8;
            case 1:
                k1 |= (data[roundedEnd] & 0xff);
                k1 *= c1;
                k1 = Integer.rotateLeft(k1, 15);
                k1 *= c2;
                h1 ^= k1;
        }

        // 最终混合
        h1 ^= length;
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;

        // 转换为无符号长整型
        return h1 & 0xffffffffL;
    }

    /**
     * URL增加前缀
     * 该方法用于给输入的URL字符串添加一个唯一的前缀
     * 主要目的是为了生成具有唯一标识的URL，以便于后续的处理和识别
     *
     * @param url 待处理的URL字符串
     * @return 返回添加前缀后的URL字符串
     */
    public static String addUrlPrefix(String url) {
        // 使用IDUtil工具类生成一个唯一的Snowflake ID作为前缀，并与原始URL使用 '&' 符号连接
        return IDUtil.geneSnowFlakeID() + "&" + url;
    }

    /**
     * 移除URL前缀
     * 该方法用于从给定的URL中移除特定的前缀部分
     * 前缀部分被定义为URL中第一个 '&' 字符之前的所有内容
     * 这在解析或处理查询参数时特别有用，其中 '&' 通常用于分隔不同的参数
     *
     * @param url 完整的URL字符串，包含需要被移除的前缀
     * @return 返回移除前缀后的URL字符串
     * 如果输入的URL不包含 '&' 字符，则返回的字符串将为空
     */
    public static String removeUrlPrefix(String url) {
        // 从第一个 '&' 字符后面开始截取字符串
        // 这里假设URL的格式是正确的，且至少包含一个 '&'
        String originalUrl = url.substring(url.indexOf("&") + 1);
        return originalUrl;
    }


    /**
     * 当短链码出现重复情况时，调用此方法来更新URL
     * 该方法通过将URL前缀的版本号递增1，以确保URL的唯一性
     * 鉴于使用雪花算法可能导致客户端和服务器端不一致的问题，此处采用简单的递增方式
     * <p>
     * 示例：
     * 输入：123132432212&https://timu.fun/download.html
     * 处理过程：
     * 1. 提取版本号：123132432212
     * 2. 提取原始URL：https://timu.fun/download.html
     * 3. 生成新版本号：123132432213
     * 输出：123132432213&https://timu.fun/download.html
     *
     * @param url 需要更新的URL，包含版本号和原始URL地址
     * @return 更新后的URL，版本号递增1
     */
    public static String addUrlPrefixVersion(String url) {

        // 提取版本号
        String version = url.substring(0, url.indexOf("&"));

        // 提取原始URL地址
        String originalUrl = url.substring(url.indexOf("&") + 1);

        // 生成新的版本号
        Long newVersion = Long.parseLong(version) + 1;

        // 构建并返回新的URL
        String newUrl = newVersion + "&" + originalUrl;
        return newUrl;
    }

    public static void sendHtmlMessage(HttpServletResponse response, JsonData jsonData) {

        response.setContentType("text/html; charset=utf-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.write(jsonData.getData().toString());
            writer.flush();
        } catch (IOException e) {
            log.warn("响应json数据给前端异常:{}", e);
        }
    }

}