package fun.timu.oj.judge.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目标签表
 *
 * @TableName problem_tag
 */
@TableName(value = "problem_tag")
@Data
public class ProblemTagDO implements Serializable {
    /**
     * 标签ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 英文标签名称
     */
    private String tagNameEn;

    /**
     * 标签颜色(十六进制)
     */
    private String tagColor;

    /**
     * 标签分类：ALGORITHM(算法)，DATA_STRUCTURE(数据结构)，TOPIC(主题)，DIFFICULTY(难度)
     */
    private String category;

    /**
     * 使用次数
     */
    private Long usageCount;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 标签描述
     */
    private String description;

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