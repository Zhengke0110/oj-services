package fun.timu.oj.judge.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.DO.ProblemTagDO;
import fun.timu.oj.judge.model.DTO.CategoryAggregateStatisticsDTO;
import fun.timu.oj.judge.model.Enums.ProblemDifficultyEnum;
import fun.timu.oj.judge.model.Enums.ProblemStatusEnum;
import fun.timu.oj.judge.model.Enums.ProblemVisibilityEnum;
import fun.timu.oj.judge.model.Enums.TagCategoryEnum;
import fun.timu.oj.judge.model.VO.ExampleVO;
import fun.timu.oj.judge.model.VO.ProblemTagVO;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.model.VTO.CategoryAggregateStatisticsVTO;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConvertToUtils {
    public static ProblemVO convertToVO(ProblemDO problemDO) {
        if (problemDO == null) return null;

        ProblemVO problemVO = new ProblemVO();
        BeanUtils.copyProperties(problemDO, problemVO);

        // 设置难度级别描述
        problemVO.setDifficultyLabel(ProblemDifficultyEnum.getDescriptionByCode(problemDO.getDifficulty()));

        // 设置语言支持列表
        problemVO.setSupportedLanguages(parseJsonToList(problemDO.getSupportedLanguages(), "supportedLanguages", String.class));

        // 设置是否需要输入数据
        problemVO.setHasInput(problemDO.getHasInput() == 1);

        // 设置示例输入输出
        problemVO.setExamples(parseJsonToList(problemDO.getExamples(), "examples", ExampleVO.class));

        // 设置题目状态标签
        problemVO.setStatusLabel(ProblemStatusEnum.getDescriptionByCode(problemDO.getStatus()));

        // 设置题目可见性标签
        problemVO.setVisibilityLabel(ProblemVisibilityEnum.getDescriptionByCode(problemDO.getVisibility()));

        // 设置通过率
        problemVO.setAcceptanceRate(calculateAcceptanceRate(problemDO.getSubmissionCount(), problemDO.getAcceptedCount()));

        // 设置提示信息
        problemVO.setHints(parseJsonToList(problemDO.getHints(), "hints", String.class));

        // 设置解题代码模板
        problemVO.setSolutionTemplates(parseTemplateMap(problemDO.getSolutionTemplates(), "solutionTemplates"));

        return problemVO;
    }

    /**
     * 将问题标签数据对象转换为视图对象
     *
     * @param tagDO 问题标签数据对象，包含标签的相关数据
     * @return 返回一个视图对象，包含与数据对象相同的信息
     */
    public static ProblemTagVO convertToVO(ProblemTagDO tagDO) {
        // 创建一个视图对象实例
        ProblemTagVO tagVO = new ProblemTagVO();
        // 将数据对象的属性值复制到视图对象中
        BeanUtils.copyProperties(tagDO, tagVO);
        // 返回填充好的视图对象
        return tagVO;
    }

    /**
     * 将 CategoryAggregateStatisticsDTO 转换为 CategoryAggregateStatisticsVO
     *
     * @param dto CategoryAggregateStatisticsDTO 对象
     * @return 转换后的 CategoryAggregateStatisticsVO 对象
     */
    public static CategoryAggregateStatisticsVTO convertToVO(CategoryAggregateStatisticsDTO dto) {
        if (dto == null) {
            return null;
        }
        // 使用 BeanUtils 进行属性拷贝
        CategoryAggregateStatisticsVTO vo = new CategoryAggregateStatisticsVTO();
        BeanUtils.copyProperties(dto, vo);

        // 设置额外的字段
        vo.setCategoryDisplayName(getCategoryDisplayName(dto.getCategory()));
        if (dto.getTotalTags() != null && dto.getTotalTags() > 0) {
            vo.setActiveRate(dto.getActiveTags() * 1.0 / dto.getTotalTags());
            vo.setAverageUsage(dto.getStoredUsageCount() * 1.0 / dto.getTotalTags());
        } else {
            vo.setActiveRate(0.0);
            vo.setAverageUsage(0.0);
        }
        return vo;
    }

    /**
     * 将给定的JSON字符串解析为指定类型的列表
     * 如果输入无效或解析失败，则返回一个空列表
     *
     * @param jsonField 待解析的JSON字符串，可以是任何类型，但函数会检查它是否是字符串
     * @param fieldName 字段名称，用于日志中标识是哪个字段解析失败
     * @param clazz     列表中元素的目标类型
     * @param <T>       泛型方法参数，表示列表中元素的类型
     * @return 解析后的列表，如果输入无效或解析失败，则返回一个空列表
     */
    private static <T> List<T> parseJsonToList(Object jsonField, String fieldName, Class<T> clazz) {
        // 检查输入是否为非空字符串，这是进行JSON解析的前提条件
        if (jsonField != null && jsonField instanceof String && !((String) jsonField).trim().isEmpty()) {
            String str = (String) jsonField;
            try {
                // 尝试将JSON字符串解析为指定类型的列表
                return JSON.parseObject(str, new TypeReference<List<T>>(clazz) {
                });
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse" + fieldName + "JSON:" + str, e);
            }
        }
        // 如果输入无效或解析失败，返回一个空列表
        return Collections.emptyList();
    }


    /**
     * 计算通过率
     * <p>
     * 此方法用于计算给定提交次数和通过次数下的通过率
     * 它首先检查提交次数和通过次数是否为有效值，然后计算通过率，
     * 并将结果四舍五入到两位小数
     *
     * @param submissionCount 提交的总数，应为非负数
     * @param acceptedCount   通过的总数，应为非负数
     * @return 通过率，如果输入无效则返回0.0
     */
    private static double calculateAcceptanceRate(Long submissionCount, Long acceptedCount) {
        // 检查输入值的有效性，如果无效则返回0.0
        if (submissionCount == null || acceptedCount == null || submissionCount <= 0) {
            return 0.0;
        }
        // 计算原始通过率
        double rate = ((double) acceptedCount) / submissionCount;
        // 将通过率四舍五入到两位小数并返回
        return Math.round(rate * 100) / 100.0;
    }

    /**
     * 将 Object 类型的 JSON 字符串字段解析为 Map<String, String>
     *
     * @param jsonField JSON 字段对象
     * @param fieldName 字段名（用于日志记录）
     * @return 解析后的 Map，失败时返回空 Map
     */
    private static Map<String, String> parseTemplateMap(Object jsonField, String fieldName) {
        if (jsonField instanceof String) {
            String jsonStr = (String) jsonField;
            if (jsonStr != null && !jsonStr.trim().isEmpty()) {
                try {
                    return JSON.parseObject(jsonStr, new TypeReference<Map<String, String>>() {
                    });
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse" + fieldName + "JSON:" + jsonStr, e);
                }
            }
        }
        return Collections.emptyMap();
    }

    /**
     * 将ProblemDO转换为基本信息的ProblemVO
     * 只包含题目的基本字段，不包含详细内容
     *
     * @param problemDO 题目DO对象
     * @return 基本信息的题目VO对象
     */
    public static ProblemVO convertToBasicVO(ProblemDO problemDO) {
        if (problemDO == null) return null;

        ProblemVO problemVO = new ProblemVO();
        // 修正参数顺序：从源对象复制到目标对象
        BeanUtils.copyProperties(problemDO, problemVO);

        // 设置难度标签，增加空值判断
        Integer difficulty = problemDO.getDifficulty();
        problemVO.setDifficultyLabel(
                ProblemDifficultyEnum.getDescriptionByCode(difficulty != null ? difficulty : 0)
        );

        // 设置状态标签，增加空值判断
        Integer status = problemDO.getStatus();
        problemVO.setStatusLabel(
                ProblemStatusEnum.getDescriptionByCode(status != null ? status : 0)
        );

        // 计算通过率，增加更多空值检查
        Long submissionCount = problemDO.getSubmissionCount();
        Long acceptedCount = problemDO.getAcceptedCount();
        if (submissionCount != null && submissionCount > 0 && acceptedCount != null) {
            double acceptanceRate = (double) acceptedCount / submissionCount;
            problemVO.setAcceptanceRate(Math.round(acceptanceRate * 10000) / 10000.0);
        } else {
            problemVO.setAcceptanceRate(0.0);
        }

        return problemVO;
    }


    /**
     * 根据分类枚举值获取分类显示名称
     *
     * @param category 分类枚举值
     * @return 分类显示名称
     */
    private static String getCategoryDisplayName(String category) {
        try {
            return TagCategoryEnum.valueOf(category).name();
        } catch (IllegalArgumentException e) {
            return TagCategoryEnum.ALGORITHM.toString();
        }
    }


}
