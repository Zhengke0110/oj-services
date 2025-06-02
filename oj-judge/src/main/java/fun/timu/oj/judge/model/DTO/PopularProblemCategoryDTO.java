package fun.timu.oj.judge.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 最受欢迎的题目类型和难度组合统计数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularProblemCategoryDTO {
    // 题目类型
    private String problemType;
    // 难度级别
    private Integer difficulty;
    // 总题目数量
    private Integer totalCount;
    // 总提交次数
    private Long totalSubmissions;
    // 总通过次数
    private Long totalAccepted;
    // 通过率
    private Double acceptanceRate;

    /**
     * 将 Map 转换为 PopularProblemCategoryDTO 对象
     *
     * @param map 包含统计数据的 Map
     * @return 转换后的 PopularProblemCategoryDTO 对象
     */
    public static PopularProblemCategoryDTO fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        return PopularProblemCategoryDTO.builder().problemType((String) map.get("problem_type")).difficulty(convertToInteger(map.get("difficulty"))).totalCount(convertToInteger(map.get("total_count"))).totalSubmissions(convertToLong(map.get("total_submissions"))).totalAccepted(convertToLong(map.get("total_accepted"))).acceptanceRate(convertToDouble(map.get("acceptance_rate"))).build();
    }

    /**
     * 将 Map 列表转换为 PopularProblemCategoryDTO 对象列表
     *
     * @param mapList 包含统计数据的 Map 列表
     * @return 转换后的 PopularProblemCategoryDTO 对象列表
     */
    public static List<PopularProblemCategoryDTO> fromMapList(List<HashMap<String, Object>> mapList) {
        if (mapList == null) {
            return new ArrayList<>();
        }

        List<PopularProblemCategoryDTO> result = new ArrayList<>(mapList.size());
        for (Map<String, Object> map : mapList) {
            PopularProblemCategoryDTO dto = fromMap(map);
            if (dto != null) {
                result.add(dto);
            }
        }
        return result;
    }

    /**
     * 将对象转换为 Integer 类型
     *
     * @param value 需要转换的对象
     * @return 转换后的 Integer 值，转换失败则返回0
     */
    private static Integer convertToInteger(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 将对象转换为 Long 类型
     *
     * @param value 需要转换的对象
     * @return 转换后的 Long 值，转换失败则返回0L
     */
    private static Long convertToLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 将对象转换为 Double 类型
     *
     * @param value 需要转换的对象
     * @return 转换后的 Double 值，转换失败则返回0.0
     */
    private static Double convertToDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}