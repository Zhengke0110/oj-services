package fun.timu.oj.judge.controller.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * 问题标签列表查询请求
 */
@Data
public class ProblemTagListRequest extends BaseTagRequest {
    @Min(value = 1, message = "当前页码不能小于1")
    private Integer current = 1;

    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 20;

    @Size(max = 50, message = "标签名称长度不能超过50字符")
    private String tagName;

    private Boolean isEnabled;

    @Override
    protected String getDefaultColor() {
        return null; // 列表查询时，找不到颜色返回null
    }
}