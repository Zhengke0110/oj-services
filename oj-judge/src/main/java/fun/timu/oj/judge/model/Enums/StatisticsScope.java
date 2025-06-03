package fun.timu.oj.judge.model.Enums;

/**
 * 统计信息范围枚举
 * 用于统一管理不同粒度的统计信息接口
 * 
 * @author zhengke
 */
public enum StatisticsScope {
    /**
     * 基础统计 - 按题目类型和难度分组的基本统计数据
     */
    BASIC("basic", "基础统计信息"),
    
    /**
     * 详细统计 - 包含各种维度的详细统计数据
     */
    DETAILED("detailed", "详细统计信息"),
    
    /**
     * 总体统计 - 平台整体的统计信息（增强版）
     */
    OVERALL("overall", "总体统计信息"),
    
    /**
     * 仪表盘统计 - 用于数据大屏的统计信息
     */
    DASHBOARD("dashboard", "仪表盘统计信息");

    private final String code;
    private final String description;

    StatisticsScope(String code, String description) {
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
     * 根据代码获取统计范围枚举
     */
    public static StatisticsScope fromCode(String code) {
        for (StatisticsScope scope : values()) {
            if (scope.getCode().equals(code)) {
                return scope;
            }
        }
        throw new IllegalArgumentException("未知的统计范围代码: " + code);
    }
}
