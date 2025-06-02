package fun.timu.oj.judge.model.DTO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 趋势分析结果数据传输对象
 * 统一所有时间趋势分析的返回结果
 *
 * @author zhengke
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendAnalysisDTO {

    /**
     * 时间周期标识
     */
    private String timePeriod;

    /**
     * 时间标签（用于显示）
     */
    private String timeLabel;

    /**
     * 趋势数据（包含各种指标的键值对）
     */
    private Map<String, Object> data;

    /**
     * 趋势分析类型
     */
    private String trendType;

    /**
     * 创建时间
     */
    private Date timestamp;

    /**
     * 从 HashMap 转换为 TrendAnalysisDTO
     *
     * @param map 包含趋势数据的 Map
     * @param trendType 趋势类型
     * @return 转换后的 DTO 对象
     */
    public static TrendAnalysisDTO fromMap(Map<String, Object> map, String trendType) {
        if (map == null) {
            return null;
        }

        TrendAnalysisDTO dto = new TrendAnalysisDTO();
        dto.setTimePeriod(getStringValue(map, "time_period"));
        dto.setTimeLabel(getStringValue(map, "time_label"));
        dto.setTrendType(trendType);
        dto.setTimestamp(new Date());

        // 将除了时间相关字段外的所有数据存入data map
        Map<String, Object> data = new HashMap<>(map);
        data.remove("time_period");
        data.remove("time_label");
        dto.setData(data);

        return dto;
    }

    /**
     * 从 Map 中安全获取字符串值
     */
    private static String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
