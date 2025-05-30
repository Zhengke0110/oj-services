package fun.timu.oj.judge.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目标签关联表
 *
 * @TableName problem_tag_relation
 */
@TableName(value = "problem_tag_relation")
@Data
public class ProblemTagRelationDO implements Serializable {
    /**
     * 关联ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目ID
     */
    private Long problemId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 是否已删除(软删除)
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}