package fun.timu.oj.judge.model.VO;

import lombok.Data;

import java.util.Date;

/**
 * 题目标签视图对象
 *
 * @author zhengke
 */
@Data
public class ProblemTagVO {

    /**
     * 标签ID
     */
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
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
