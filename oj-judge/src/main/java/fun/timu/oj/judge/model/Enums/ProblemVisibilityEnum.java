package fun.timu.oj.judge.model.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题目可见性枚举：0-私有，1-公开，2-下线
 */
@Getter
@AllArgsConstructor
public enum ProblemVisibilityEnum {
    PRIVATE(0, "私有"),
    PUBLIC(1, "公开"),
    OFFLINE(2, "下线");

    private final Integer code;
    private final String description;

    /**
     * 根据code获取对应的枚举值
     *
     * @param code 可见性编码
     * @return 对应的ProblemVisibilityEnum，如果未找到则返回null
     */
    public static ProblemVisibilityEnum getByCode(Integer code) {
        if (code == null) return null;
        for (ProblemVisibilityEnum visibility : ProblemVisibilityEnum.values()) {
            if (visibility.getCode().equals(code)) {
                return visibility;
            }
        }
        return null;
    }

    /**
     * 根据code获取对应的描述
     *
     * @param code 可见性编码
     * @return 描述字符串，如果未找到则返回null
     */
    public static String getDescriptionByCode(Integer code) {
        ProblemVisibilityEnum visibility = getByCode(code);
        return visibility != null ? visibility.getDescription() : null;
    }
}
