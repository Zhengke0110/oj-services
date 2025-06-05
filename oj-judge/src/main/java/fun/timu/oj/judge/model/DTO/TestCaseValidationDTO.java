package fun.timu.oj.judge.model.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 测试用例完整性验证结果DTO
 */
@Data
@Builder
public class TestCaseValidationDTO {
    
    /**
     * 是否验证通过
     */
    private Boolean valid;
    
    /**
     * 总测试用例数
     */
    private Integer totalCount;
    
    /**
     * 启用的测试用例数
     */
    private Integer enabledCount;
    
    /**
     * 示例测试用例数
     */
    private Integer exampleCount;
    
    /**
     * 总权重
     */
    private Integer totalWeight;
    
    /**
     * 问题列表
     */
    private List<String> issues;
    
    /**
     * 重复的测试用例
     */
    private List<Object> duplicateTestCases;
    
    /**
     * 验证消息
     */
    private String message;
}
