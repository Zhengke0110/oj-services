package fun.timu.oj.common.enmus;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题目状态枚举：0-禁用，1-启用，2-草稿
 */
@Getter
@AllArgsConstructor
public enum ProblemStatusEnum {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用"),
    DRAFT(2, "草稿");

    private final Integer code;
    private final String description;

    /**
     * 根据code获取对应的枚举值
     *
     * @param code 状态编码
     * @return 对应的ProblemStatusEnum，如果未找到则返回null
     */
    public static ProblemStatusEnum getByCode(Integer code) {
        if (code == null) return null;
        for (ProblemStatusEnum status : ProblemStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据code获取对应的描述
     *
     * @param code 状态编码
     * @return 描述字符串，如果未找到则返回null
     */
    public static String getDescriptionByCode(Integer code) {
        ProblemStatusEnum status = getByCode(code);
        return status != null ? status.getDescription() : null;
    }
}
