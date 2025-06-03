package fun.timu.oj.judge.model.Enums;

/**
 * 排行榜类型枚举
 *
 * @author zhengke
 */
public enum RankingType {

    /**
     * 热门题目排行（按提交量）
     */
    POPULARITY("popularity", "热门题目排行"),

    /**
     * 最难题目排行（按通过率）
     */
    HARDEST("hardest", "最难题目排行"),

    /**
     * 最易题目排行（按通过率）
     */
    EASIEST("easiest", "最易题目排行"),

    /**
     * 高质量题目排行（综合评分）
     */
    QUALITY("quality", "高质量题目排行"),

    /**
     * 最新题目排行
     */
    NEWEST("newest", "最新题目排行"),

    /**
     * 提交最多题目排行
     */
    MAX_SUBMISSION("max_submission", "最多提交题目排行"),

    /**
     * 零提交题目排行
     */
    ZERO_SUBMISSION("zero_submission", "零提交题目排行"),

    /**
     * 创建者贡献排行
     */
    CREATOR_CONTRIBUTION("creator_contribution", "创建者贡献排行");

    private final String code;
    private final String description;

    RankingType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     */
    public static RankingType fromCode(String code) {
        for (RankingType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ranking type code: " + code);
    }
}
