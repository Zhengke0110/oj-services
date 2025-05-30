package fun.timu.oj.judge.model.VO;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 题目视图对象
 * @author zhengke
 */
@Data
public class ProblemVO {

    /**
     * 题目ID
     */
    private Long id;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 英文标题
     */
    private String titleEn;

    /**
     * 题目描述
     */
    private String description;

    /**
     * 英文描述
     */
    private String descriptionEn;

    /**
     * 题目类型：ALGORITHM(算法)，PRACTICE(编程练习)，DEBUG(代码调试)
     */
    private String problemType;

    /**
     * 难度级别：0-简单，1-中等，2-困难
     */
    private Integer difficulty;

    /**
     * 难度级别描述
     */
    private String difficultyLabel;

    /**
     * 默认时间限制(ms)
     */
    private Integer timeLimit;

    /**
     * 默认内存限制(字节)
     */
    private Long memoryLimit;

    /**
     * 支持的编程语言列表
     */
    private List<String> supportedLanguages;

    /**
     * 解题代码模板(多语言)
     */
    private Object solutionTemplates;

    /**
     * 输入格式说明
     */
    private String inputDescription;

    /**
     * 输出格式说明
     */
    private String outputDescription;

    /**
     * 是否需要输入数据
     */
    private Boolean hasInput;

    /**
     * 输入格式：TEXT(文本)，JSON(JSON格式)，ARGS(命令行参数)
     */
    private String inputFormat;

    /**
     * 示例输入输出
     */
    private List<Object> examples;

    /**
     * 状态：0-禁用，1-启用，2-草稿
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusLabel;

    /**
     * 可见性：0-私有，1-公开，2-仅注册用户
     */
    private Integer visibility;

    /**
     * 可见性描述
     */
    private String visibilityLabel;

    /**
     * 提交次数
     */
    private Long submissionCount;

    /**
     * 通过次数
     */
    private Long acceptedCount;

    /**
     * 通过率
     */
    private Double acceptanceRate;

    /**
     * 创建者账户号
     */
    private Long creatorId;

    /**
     * 提示信息(分步提示)
     */
    private List<String> hints;

    /**
     * 约束条件说明
     */
    private String constraints;

    /**
     * 备注信息
     */
    private String notes;

    /**
     * 元数据
     */
    private Object metadata;

    /**
     * 关联的标签列表
     */
    private List<ProblemTagVO> tags;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
