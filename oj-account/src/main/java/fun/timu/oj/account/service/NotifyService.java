package fun.timu.oj.account.service;

import fun.timu.oj.common.enmus.SendCodeEnum;
import fun.timu.oj.common.utils.JsonData;

public interface NotifyService {
    /**
     * 发送验证码
     *
     * @param sendCodeEnum
     * @param to
     * @return
     */
    JsonData sendCode(SendCodeEnum sendCodeEnum, String to);

    /**
     * 校验验证码
     *
     * @param sendCodeEnum
     * @param to
     * @param code
     * @return
     */
    boolean checkCode(SendCodeEnum sendCodeEnum, String to, String code);

}
