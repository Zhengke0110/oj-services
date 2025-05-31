package fun.timu.oj.judge.controller.request;

import fun.timu.oj.common.enmus.TagCategoryEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * 问题标签更新请求
 */
@Data
public class ProblemTagUpdateRequest {

    @NotNull(message = "ID不能为空")
    @Positive(message = "标签ID必须为正数")
    private Long id;

    @Size(max = 50, message = "标签名称长度不能超过50字符")
    private String tagName;

    @Size(max = 50, message = "英文标签名称长度不能超过50字符")
    private String tagNameEn;

    @Size(max = 20, message = "标签颜色长度不能超过20字符")
    private String color;

    @NotNull(message = "标签类别不能为空")
    private TagCategoryEnum category;

    @Size(max = 500, message = "标签描述长度不能超过500字符")
    private String description;

    private Boolean isEnabled;
}
