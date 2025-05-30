package fun.timu.oj.judge.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 问题信息表(优化版)
 *
 * @TableName problem
 */
@TableName(value = "problem")
@Data
public class ProblemDO implements Serializable {
    /**
     * 题目ID
     */
    @TableId(type = IdType.AUTO)
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
     * 默认时间限制(ms)
     */
    private Integer timeLimit;

    /**
     * 默认内存限制(字节，默认256MB)
     */
    private Long memoryLimit;

    /**
     * 支持的编程语言列表
     */
    private Object supportedLanguages;

    /**
     * 解题代码模板(多语言)，格式：{"JAVA":"...", "PYTHON":"..."}
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
    private Integer hasInput;

    /**
     * 输入格式：TEXT(文本)，JSON(JSON格式)，ARGS(命令行参数)
     */
    private String inputFormat;

    /**
     * 示例输入输出，格式：[{"input":"...", "output":"...", "explanation":"..."}]
     */
    private Object examples;

    /**
     * 状态：0-禁用，1-启用，2-草稿
     */
    private Integer status;

    /**
     * 可见性：0-私有，1-公开，2-仅注册用户
     */
    private Integer visibility;

    /**
     * 提交次数
     */
    private Long submissionCount;

    /**
     * 通过次数
     */
    private Long acceptedCount;

    /**
     * 创建者账户号(对应account.account_no)
     */
    private Long creatorId;

    /**
     * 提示信息(分步提示)
     */
    private Object hints;

    /**
     * 约束条件说明
     */
    private String constraints;

    /**
     * 备注信息
     */
    private String notes;

    /**
     * 元数据(JSON格式)
     */
    private Object metadata;

    /**
     * 是否已删除(软删除)
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}