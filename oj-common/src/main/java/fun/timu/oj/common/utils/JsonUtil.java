package fun.timu.oj.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {

        //设置可用单引号
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        //序列化的时候序列对象的所有属性
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        //反序列化的时候如果多了其他属性,不抛出异常
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //如果是空对象的时候,不抛异常
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        //取消时间的转化格式,默认是时间戳,可以取消,同时需要设置要表现的时间格式
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }


    /**
     * 将Java对象转换为JSON字符串
     * 此方法主要用于序列化Java对象，将其转换为JSON格式的字符串表示形式
     * 使用Jackson库的ObjectMapper来完成转换工作
     *
     * @param obj 要转换为JSON字符串的Java对象
     * @return 返回转换后的JSON字符串，如果转换过程中发生异常，则返回null
     */
    public static String obj2Json(Object obj) {
        String jsonStr = null;
        try {
            // 使用ObjectMapper将Java对象转换为JSON字符串
            jsonStr = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            // 处理JSON处理异常，记录错误日志而不是打印堆栈跟踪
            // e.printStackTrace();
            log.error("json格式化异常:{}", e);
        }
        return jsonStr;
    }

    /**
     * 将JSON字符串转换为指定类型的对象
     *
     * @param jsonStr  待转换的JSON字符串
     * @param beanType 指定的类型
     * @param <T>      泛型参数，表示任意类型
     * @return 转换后的对象，如果转换失败则返回null
     */
    public static <T> T json2Obj(String jsonStr, Class<T> beanType) {
        T obj = null;
        try {
            // 使用Jackson库的ObjectMapper将JSON字符串转换为指定类型的对象
            obj = mapper.readValue(jsonStr, beanType);
        } catch (Exception e) {
            // 当转换过程中发生异常时，记录错误日志而不抛出异常，保持程序的健壮性
            log.error("json格式化异常:{}", e);
        }
        // 返回转换后的对象，如果转换失败则返回null
        return obj;
    }


    /**
     * 将json数据转换成指定类型的pojo对象list
     * 该方法使用了泛型，可以在调用时指定实际的bean类型
     *
     * @param jsonData json格式的字符串数据，应包含一个列表
     * @param beanType 目标pojo对象的类类型，用于指定列表中元素的类型
     * @return 返回一个由json数据转换得到的pojo对象列表，如果转换失败或发生异常，则返回null
     */
    public static <T> List<T> json2List(String jsonData, Class<T> beanType) {
        // 构造一个参数化的Java类型实例，表示List<T>
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, beanType);
        try {
            // 使用ObjectMapper读取json数据，并将其转换为指定类型的List
            List<T> list = mapper.readValue(jsonData, javaType);
            return list;
        } catch (Exception e) {
            // 当转换过程中发生异常时，记录错误日志
            log.error("json格式化异常:{}", e);
        }
        // 如果发生异常，方法返回null
        return null;
    }

    /**
     * 将对象转换为byte数组
     * 此方法主要用于序列化对象，以便于存储或传输
     *
     * @param obj 要转换的对象
     * @return 转换后的byte数组，如果转换失败则返回null
     */
    public static byte[] obj2Bytes(Object obj) {
        byte[] byteArr = null;
        try {
            // 使用mapper将对象转换为byte数组
            byteArr = mapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            // 捕获并处理json格式化异常
            log.error("json格式化异常:{}", e);
        }
        return byteArr;
    }


    /**
     * 将byte数组转换为指定类型的对象
     *
     * @param byteArr  byte数组，通常是从文件、网络等来源获取的二进制数据
     * @param beanType 指定转换后的对象类型，通过Class<T>泛型参数传递
     * @return 返回转换后的对象实例，如果转换过程中发生异常，则返回null
     */
    public static <T> T bytes2Obj(byte[] byteArr, Class<T> beanType) {
        T obj = null;
        try {
            // 使用Jackson库的ObjectMapper实例将byte数组反序列化为指定类型的对象
            obj = mapper.readValue(byteArr, beanType);
        } catch (Exception e) {
            // 捕获并处理转换过程中可能发生的异常，例如数据格式不匹配
            // 为了避免污染调用者代码，这里不打印堆栈跟踪，而是记录错误日志
            log.error("json格式化异常:{}", e);
        }
        // 返回转换后的对象，如果转换失败则返回null
        return obj;
    }
}
