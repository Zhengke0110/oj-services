package fun.timu.oj.account.controller;


import com.google.code.kaptcha.Producer;

import fun.timu.oj.account.controller.request.SendCodeRequest;
import fun.timu.oj.account.service.NotifyService;
import fun.timu.oj.common.enmus.BizCodeEnum;
import fun.timu.oj.common.enmus.SendCodeEnum;
import fun.timu.oj.common.utils.CommonUtil;
import fun.timu.oj.common.utils.JsonData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 验证码相关接口
 */
@RestController
@RequestMapping("/api/notify/v1")
public class NotifyController {
    private static Logger logger = LoggerFactory.getLogger(NotifyController.class);

    private final NotifyService notifyService;

    private final Producer captchaProducer;

    private final StringRedisTemplate redisTemplate;

    /**
     * 验证码过期时间
     */
    private static final long CAPTCHA_CODE_EXPIRED = 1000 * 10 * 60;


    public NotifyController(NotifyService notifyService, Producer captchaProducer, StringRedisTemplate redisTemplate) {
        this.notifyService = notifyService;
        this.captchaProducer = captchaProducer;
        this.redisTemplate = redisTemplate;
    }


    /**
     * 生成验证码的唯一键
     * 该方法用于根据用户请求生成一个唯一的验证码键，以确保每个用户的验证码是独立的
     * 它结合了用户的IP地址和用户代理，通过MD5加密生成一个唯一的后缀，然后将其与固定的前缀组合起来
     *
     * @param request 用户的HTTP请求对象，从中获取IP地址和用户代理
     * @return 返回生成的验证码键字符串
     */
    private String getCaptchaKey(HttpServletRequest request) {
        // 获取用户请求的IP地址
        String ip = CommonUtil.getIpAddr(request);
        // 获取用户请求的User-Agent头部信息
        String userAgent = request.getHeader("User-Agent");
        // 将IP地址和用户代理拼接后使用MD5加密，生成唯一的后缀，并与固定的前缀组合成完整的验证码键
        String key = "account-service:captcha:" + CommonUtil.MD5(ip + userAgent);
        // 记录日志信息，输出生成的验证码键
        logger.info("验证码key:{}", key);
        // 返回生成的验证码键
        return key;
    }


    /**
     * 生成验证码
     *
     * @param request  HTTP请求对象，用于获取生成验证码的会话信息
     * @param response HTTP响应对象，用于输出生成的验证码图片
     */
    @GetMapping("captcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        // 生成验证码文本
        String captchaText = captchaProducer.createText();
        // 记录日志，方便调试和追踪
        logger.info("验证码内容:{}", captchaText);

        // 将验证码文本存储到Redis中，并设置过期时间
        // 这里使用了Redis的字符串操作，将验证码与会话关联，以便后续验证
        redisTemplate.opsForValue().set(getCaptchaKey(request), captchaText, CAPTCHA_CODE_EXPIRED, TimeUnit.MILLISECONDS);

        // 根据验证码文本生成验证码图片
        BufferedImage bufferedImage = captchaProducer.createImage(captchaText);

        // 尝试使用响应流输出验证码图片
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            // 将生成的验证码图片以JPEG格式写入响应流
            ImageIO.write(bufferedImage, "jpg", outputStream);
            // 刷新响应流，确保验证码图片被正确输出
            outputStream.flush();
        } catch (IOException e) {
            // 捕获IO异常，并记录错误日志
            logger.error("获取流出错:{}", e.getMessage());
        }
    }

    /**
     * 处理发送验证码请求
     * 该方法用于验证用户提交的验证码，并在验证成功后发送新的验证码
     *
     * @param sendCodeRequest 包含用户提交的验证码和接收新验证码的目标地址
     * @param request         HTTP请求对象，用于获取会话信息
     * @return 返回一个JsonData对象，包含发送结果
     */
    @PostMapping("send_code")
    public JsonData sendCode(@RequestBody SendCodeRequest sendCodeRequest, HttpServletRequest request) {
        //获取验证码的缓存键
        String key = getCaptchaKey(request);

        //从缓存中获取存储的验证码
        String cacheCaptcha = redisTemplate.opsForValue().get(key);

        //获取用户提交的验证码
        String captcha = sendCodeRequest.getCaptcha();

        //比较用户提交的验证码和缓存中的验证码
        if (captcha != null && cacheCaptcha != null && cacheCaptcha.equalsIgnoreCase(captcha)) {
            //验证码匹配，删除缓存中的验证码
            redisTemplate.delete(key);
            //调用服务发送新的验证码，并返回发送结果
            JsonData jsonData = notifyService.sendCode(SendCodeEnum.USER_REGISTER, sendCodeRequest.getTo());
            return jsonData;
        } else {
            //验证码不匹配，返回错误信息
            return JsonData.buildResult(BizCodeEnum.CODE_CAPTCHA_ERROR);
        }
    }

}
