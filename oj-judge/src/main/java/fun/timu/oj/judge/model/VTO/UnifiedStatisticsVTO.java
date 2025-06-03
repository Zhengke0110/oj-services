package fun.timu.oj.judge.model.VTO;

import fun.timu.oj.judge.model.enums.StatisticsScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 统一统计信息响应模型
 * 包含元数据和实际统计数据
 *
 * @author zhengke
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnifiedStatisticsVTO {

    /**
     * 统计范围
     */
    private StatisticsScope scope;

    /**
     * 统计数据生成时间
     */
    private LocalDateTime timestamp;

    /**
     * 数据版本（用于缓存管理）
     */
    private String version;

    /**
     * 实际的统计数据
     */
    private Object data;

    /**
     * 元数据信息
     */
    private StatisticsMetadata metadata;

    /**
     * 统计数据的元数据信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsMetadata {
        /**
         * 数据总条数
         */
        private Long totalCount;

        /**
         * 分页信息 - 当前页
         */
        private Integer currentPage;

        /**
         * 分页信息 - 页面大小
         */
        private Integer pageSize;

        /**
         * 分页信息 - 总页数
         */
        private Integer totalPages;

        /**
         * 查询执行时间（毫秒）
         */
        private Long executionTime;

        /**
         * 数据来源
         */
        private String dataSource;

        /**
         * 附加信息
         */
        private Map<String, Object> additionalInfo;
    }
}
