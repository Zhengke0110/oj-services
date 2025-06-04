package fun.timu.oj.judge.model.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题目类型枚举：ALGORITHM(算法)，PRACTICE(编程练习)，DEBUG(代码调试)
 */
@Getter
@AllArgsConstructor
public enum ProblemTypeEnum {
    ALGORITHM(0, "算法"),
    PRACTICE(1, "编程练习"),
    DEBUG(2, "代码调试");

    private final Integer code;
    private final String description;

    /**
     * 根据code获取对应的枚举值
     *
     * @param code 类型编码
     * @return 对应的ProblemTypeEnum，如果未找到则返回null
     */
    public static ProblemTypeEnum getByCode(Integer code) {
        if (code == null) return null;
        for (ProblemTypeEnum type : ProblemTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据code获取对应的描述
     *
     * @param code 类型编码
     * @return 描述字符串，如果未找到则返回null
     */
    public static String getDescriptionByCode(Integer code) {
        ProblemTypeEnum type = getByCode(code);
        return type != null ? type.getDescription() : null;
    }
}
