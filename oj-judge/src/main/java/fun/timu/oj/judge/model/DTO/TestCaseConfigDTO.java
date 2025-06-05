package fun.timu.oj.judge.model.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 测试用例配置检查结果DTO
 */
@Data
@Builder
public class TestCaseConfigDTO {
    
    /**
     * 配置是否合理
     */
    private Boolean reasonable;
    
    /**
     * 建议列表
     */
    private List<String> suggestions;
    
    /**
     * 警告列表
     */
    private List<String> warnings;
    
    /**
     * 权重分布
     */
    private Map<Integer, Long> weightDistribution;
    
    /**
     * 特殊限制用例数量
     */
    private Integer specialLimitCasesCount;
    
    /**
     * 评分（0-100）
     */
    private Integer score;
    
    /**
     * 等级评价
     */
    private String grade;
    
    /**
     * 错误消息
     */
    private String message;
}
