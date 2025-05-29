package fun.timu.oj.common.utils;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckUtil {

    /**
     * 邮箱正则
     */
    private static final Pattern MAIL_PATTERN = Pattern.compile("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");

    /**
     * 手机号正则
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");

    /**
     * 验证给定的字符串是否符合电子邮件地址的格式
     * 该方法主要用于检查输入的字符串是否为空或是否符合预定义的电子邮件格式
     *
     * @param email 待验证的字符串
     * @return 如果字符串不为空且符合电子邮件地址格式，则返回true；否则返回false
     */
    public static boolean isEmail(String email) {
        // 检查邮箱字符串是否为空或为空字符串
        if (null == email || "".equals(email)) {
            return false;
        }
        // 使用正则表达式匹配邮箱格式
        Matcher m = MAIL_PATTERN.matcher(email);
        // 返回匹配结果
        return m.matches();
    }

    /**
     * 检查字符串是否为有效的电话号码
     * 该方法使用正则表达式来验证电话号码的格式
     *
     * @param phone 待验证的电话号码字符串
     * @return 如果字符串是有效的电话号码，则返回true；否则返回false
     */
    public static boolean isPhone(String phone) {
        // 检查电话号码是否为空或空字符串
        if (null == phone || "".equals(phone)) {
            return false;
        }
        // 使用预定义的正则表达式编译的模式来创建Matcher对象
        Matcher m = PHONE_PATTERN.matcher(phone);
        // 检查电话号码是否与预定义的模式匹配
        boolean result = m.matches();
        // 返回匹配结果
        return result;
    }
}
