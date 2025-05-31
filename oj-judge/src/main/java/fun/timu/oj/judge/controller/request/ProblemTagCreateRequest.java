package fun.timu.oj.judge.controller.request;

import fun.timu.oj.common.enmus.TagCategoryEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 问题标签创建请求
 */
@Data
public class ProblemTagCreateRequest extends BaseTagRequest {
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50字符")
    private String tagName;

    @Size(max = 50, message = "英文标签名称长度不能超过50字符")
    private String tagNameEn;

    private TagCategoryEnum category;

    @Size(max = 500, message = "标签描述长度不能超过500字符")
    private String description;

    private Boolean isEnabled = true;

    @Override
    protected String getDefaultColor() {
        return "#007bff";
    }
}