package fun.timu.oj.judge.service.impl.Problem;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import fun.timu.oj.common.enmus.ProblemDifficultyEnum;
import fun.timu.oj.common.enmus.ProblemStatusEnum;
import fun.timu.oj.common.enmus.ProblemVisibilityEnum;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.VO.ExampleVO;
import fun.timu.oj.judge.model.VO.ProblemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProblemUtils {
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
        // 只复制基本字段
        BeanUtils.copyProperties(problemDO, problemVO);

        // 设置难度和状态的标签
        problemVO.setDifficultyLabel(ProblemDifficultyEnum.getDescriptionByCode(problemDO.getDifficulty()));
        problemVO.setStatusLabel(ProblemStatusEnum.getDescriptionByCode(problemDO.getStatus()));

        // 计算通过率
        if (problemDO.getSubmissionCount() > 0) {
            double acceptanceRate = (double) problemDO.getAcceptedCount() / problemDO.getSubmissionCount();
            problemVO.setAcceptanceRate(Math.round(acceptanceRate * 10000) / 10000.0);
        } else {
            problemVO.setAcceptanceRate(0.0);
        }

        return problemVO;
    }

}
