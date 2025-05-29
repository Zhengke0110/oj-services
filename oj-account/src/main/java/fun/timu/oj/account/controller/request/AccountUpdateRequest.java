package fun.timu.oj.account.controller.request;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class AccountUpdateRequest {
    /**
     * 昵称
     */
    @Size(max = 20, message = "昵称长度不能超过20个字符")
    private String nickname;

    /**
     * 头像URL
     */
    private String headImg;
}
