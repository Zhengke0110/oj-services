package fun.timu.oj.judge.controller.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 自定义报告请求对象
 *
 * @author zhengke
 */
@Data
public class CustomReportRequest {

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /**
     * 指标列表
     */
    @NotEmpty(message = "指标列表不能为空")
    private List<String> metrics;
}
