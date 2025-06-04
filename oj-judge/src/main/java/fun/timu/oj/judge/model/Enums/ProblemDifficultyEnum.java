package fun.timu.oj.judge.model.Enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 难度级别枚举：0-简单，1-中等，2-困难
 */
@Getter
@AllArgsConstructor
public enum ProblemDifficultyEnum {
    EASY(0, "简单"), MEDIUM(1, "中等"), HARD(2, "困难");

    private final Integer code;
    private final String description;

    /**
     * 根据code获取对应的枚举值
     *
     * @param code 难度编码
     * @return 对应的DifficultyEnum，如果未找到则返回null
     */
    public static ProblemDifficultyEnum getByCode(Integer code) {
        if (code == null) return null;
        for (ProblemDifficultyEnum difficulty : ProblemDifficultyEnum.values()) {
            if (difficulty.getCode().equals(code)) {
                return difficulty;
            }
        }
        return null;
    }

    /**
     * 根据code获取对应的描述
     *
     * @param code 难度编码
     * @return 描述字符串，如果未找到则返回null
     */
    public static String getDescriptionByCode(Integer code) {
        ProblemDifficultyEnum difficulty = getByCode(code);
        return difficulty != null ? difficulty.getDescription() : null;
    }
}
