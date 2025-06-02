package fun.timu.oj.judge.model.VTO;

import fun.timu.oj.judge.model.DTO.PopularProblemCategoryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 最受欢迎的题目类型和难度组合统计数据（面向视图对象）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularProblemCategoryVTO {
    // 题目类型
    private String problemType;
    // 难度级别
    private Integer difficulty;
    // 总题目数量
    private Integer totalCount;
    // 通过率
    private Double acceptanceRate;
    // 热度指标 - 使用字符串表示大致热度，而非具体数值
    private String popularity;

    /**
     * 将DTO转换为VTO，屏蔽敏感信息
     *
     * @param dto 数据传输对象
     * @return 视图传输对象
     */
    public static PopularProblemCategoryVTO fromDTO(PopularProblemCategoryDTO dto) {
        if (dto == null) {
            return null;
        }

        // 计算热度等级，替代具体的提交次数
        String popularity = calculatePopularity(dto.getTotalSubmissions());

        return PopularProblemCategoryVTO.builder()
                .problemType(dto.getProblemType())
                .difficulty(dto.getDifficulty())
                .totalCount(dto.getTotalCount())
                .acceptanceRate(dto.getAcceptanceRate())
                .popularity(popularity)
                .build();
    }

    /**
     * 将DTO列表转换为VTO列表
     *
     * @param dtoList DTO对象列表
     * @return VTO对象列表
     */
    public static List<PopularProblemCategoryVTO> fromDTOList(List<PopularProblemCategoryDTO> dtoList) {
        if (dtoList == null) {
            return new ArrayList<>();
        }

        List<PopularProblemCategoryVTO> vtoList = new ArrayList<>(dtoList.size());
        for (PopularProblemCategoryDTO dto : dtoList) {
            PopularProblemCategoryVTO vto = fromDTO(dto);
            if (vto != null) {
                vtoList.add(vto);
            }
        }
        return vtoList;
    }

    /**
     * 根据提交次数计算热度等级，用描述性文本替代具体数值
     *
     * @param totalSubmissions 总提交次数
     * @return 热度等级描述
     */
    private static String calculatePopularity(Long totalSubmissions) {
        if (totalSubmissions == null) {
            return "未知";
        }

        if (totalSubmissions >= 10000) {
            return "非常热门";
        } else if (totalSubmissions >= 5000) {
            return "热门";
        } else if (totalSubmissions >= 1000) {
            return "较热门";
        } else if (totalSubmissions >= 100) {
            return "一般";
        } else {
            return "冷门";
        }
    }
}