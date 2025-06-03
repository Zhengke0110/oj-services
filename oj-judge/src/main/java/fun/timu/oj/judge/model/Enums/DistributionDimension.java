package fun.timu.oj.judge.model.Enums;

/**
 * 分布分析维度枚举
 *
 * @author zhengke
 * @since 2025-06-03
 */
public enum DistributionDimension {

    /**
     * 按难度分布
     */
    DIFFICULTY("difficulty", "按难度分布"),

    /**
     * 按题目类型分布
     */
    TYPE("problem_type", "按题目类型分布"),

    /**
     * 按支持语言分布
     */
    LANGUAGE("supported_languages", "按支持语言分布"),

    /**
     * 按状态分布
     */
    STATUS("status", "按状态分布"),

    /**
     * 按时间限制分布
     */
    TIME_LIMIT("time_limit", "按时间限制分布"),

    /**
     * 按内存限制分布
     */
    MEMORY_LIMIT("memory_limit", "按内存限制分布"),

    /**
     * 按可见性分布
     */
    VISIBILITY("visibility", "按可见性分布"),

    /**
     * 按创建者分布
     */
    CREATOR("creator_id", "按创建者分布"),
    /**
     * 难度-类型分布矩阵
     */
    DIFFICULTY_TYPE_MATRIX("difficulty_type_matrix", "难度-类型分布矩阵"),

    /**
     * 按通过率分布
     */
    ACCEPTANCE_RATE("acceptance_rate", "按通过率分布"),

    /**
     * 按提交量分布
     */
    SUBMISSION_COUNT("submission_count", "按提交量分布");

    private final String field;
    private final String description;

    DistributionDimension(String field, String description) {
        this.field = field;
        this.description = description;
    }

    public String getField() {
        return field;
    }

    public String getDescription() {
        return description;
    }
}
