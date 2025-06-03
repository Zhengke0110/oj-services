package fun.timu.oj.judge.controller.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class BatchProblemRequest {
    /**
     * 题目ID列表
     */
    @NotEmpty(message = "题目ID列表不能为空")
    private List<Long> problemIds;

    /**
     * 统一统计信息请求模型
     * 支持不同范围的统计信息查询
     *
     * @author zhengke
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnifiedStatisticsRequest {

        /**
         * 统计范围 - 必填
         */
        @NotNull(message = "统计范围不能为空")
        private fun.timu.oj.judge.model.enums.StatisticsScope scope;

        /**
         * 可选的时间范围过滤 - 开始时间
         */
        private Date startDate;

        /**
         * 可选的时间范围过滤 - 结束时间
         */
        private Date endDate;

        /**
         * 可选的难度过滤
         */
        private List<String> difficulties;

        /**
         * 可选的题目类型过滤
         */
        private List<String> problemTypes;

        /**
         * 可选的状态过滤
         */
        private List<Integer> statuses;

        /**
         * 可选的创建者ID过滤
         */
        private List<Long> creatorIds;

        /**
         * 可选的特定指标列表（用于自定义统计）
         */
        private List<String> metrics;

        /**
         * 可选的分页参数 - 页码
         */
        private Integer pageNum;

        /**
         * 可选的分页参数 - 页面大小
         */
        private Integer pageSize;

        /**
         * 可选的限制返回数量
         */
        private Integer limit;
    }
}
