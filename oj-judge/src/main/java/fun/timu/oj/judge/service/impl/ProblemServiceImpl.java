package fun.timu.oj.judge.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import fun.timu.oj.common.enmus.DifficultyEnum;
import fun.timu.oj.common.enmus.ProblemStatusEnum;
import fun.timu.oj.common.enmus.ProblemVisibilityEnum;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.VO.ExampleVO;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 题目服务实现类
 *
 * @author zhengke
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemManager problemManager;

    /**
     * 根据题目ID获取题目详细信息
     * 此方法首先调用problemManager的getById方法获取题目数据对象（ProblemDO），
     * 如果题目不存在或已被删除，则抛出运行时异常；
     * 否则，将ProblemDO转换为ProblemVO并返回
     *
     * @param id 题目ID，用于获取特定题目信息
     * @return ProblemVO 题目视图对象，包含题目详细信息如果题目不存在或已被删除，返回null
     */
    @Override
    public ProblemVO getById(Long id) {
        try {
            // 通过problemManager获取题目数据对象
            ProblemDO problemDO = problemManager.getById(id);
            // 检查题目是否存在且未被删除
            if (problemDO == null || problemDO.getIsDeleted() == 1) throw new RuntimeException("题目不存在或已被删除");
            // 将题目数据对象转换为视图对象
            ProblemVO problemVO = convertToVO(problemDO);
            // TODO 调用ProblemTagService获取题目标签
            return problemVO;
        } catch (Exception e) {
            // 记录获取题目时发生的错误
            log.error("ProblemService--->获取题目失败: {}", e.getMessage(), e);
            return null;
        }
    }


    private ProblemVO convertToVO(ProblemDO problemDO) {
        if (problemDO == null) return null;

        ProblemVO problemVO = new ProblemVO();
        BeanUtils.copyProperties(problemDO, problemVO);

        // 设置难度级别描述
        problemVO.setDifficultyLabel(DifficultyEnum.getDescriptionByCode(problemDO.getDifficulty()));

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


    private <T> List<T> parseJsonToList(Object jsonField, String fieldName, Class<T> clazz) {
        if (jsonField != null && jsonField instanceof String && !((String) jsonField).trim().isEmpty()) {
            String str = (String) jsonField;
            try {
                return JSON.parseObject(str, new TypeReference<List<T>>(clazz) {
                });
            } catch (Exception e) {
                log.warn("Failed to parse {} JSON: {}", fieldName, str, e);
            }
        }
        return Collections.emptyList();
    }


    private double calculateAcceptanceRate(Long submissionCount, Long acceptedCount) {
        if (submissionCount == null || acceptedCount == null || submissionCount <= 0) {
            return 0.0;
        }
        double rate = ((double) acceptedCount) / submissionCount;
        return Math.round(rate * 100) / 100.0; // 保留两位小数
    }

    /**
     * 将 Object 类型的 JSON 字符串字段解析为 Map<String, String>
     *
     * @param jsonField JSON 字段对象
     * @param fieldName 字段名（用于日志记录）
     * @return 解析后的 Map，失败时返回空 Map
     */
    private Map<String, String> parseTemplateMap(Object jsonField, String fieldName) {
        if (jsonField instanceof String) {
            String jsonStr = (String) jsonField;
            if (jsonStr != null && !jsonStr.trim().isEmpty()) {
                try {
                    return JSON.parseObject(jsonStr, new TypeReference<Map<String, String>>() {
                    });
                } catch (Exception e) {
                    log.warn("Failed to parse {} JSON: {}", fieldName, jsonStr, e);
                }
            }
        }
        return Collections.emptyMap();
    }

}
