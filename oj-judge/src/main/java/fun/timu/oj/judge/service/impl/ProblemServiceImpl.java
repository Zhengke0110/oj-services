package fun.timu.oj.judge.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.enmus.DifficultyEnum;
import fun.timu.oj.common.enmus.ProblemStatusEnum;
import fun.timu.oj.common.enmus.ProblemVisibilityEnum;
import fun.timu.oj.common.interceptor.LoginInterceptor;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.controller.request.ProblemCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemQueryRequest;
import fun.timu.oj.judge.controller.request.ProblemUpdateRequest;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.VO.ExampleVO;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * 根据条件获取问题列表
     *
     * @param request 包含查询条件的请求对象
     * @return 包含问题列表的分页结果
     */
    @Override
    public PageResult<ProblemVO> getProblemsWithConditions(ProblemQueryRequest request) {
        try {
            // 1. 从请求中提取查询参数
            int current = Optional.ofNullable(request.getCurrent()).orElse(1);
            int size = Optional.ofNullable(request.getSize()).orElse(20);

            // 2. 调用Manager层方法获取分页结果
            IPage<ProblemDO> problemPage = problemManager.findTagListWithPage(current, size, request.getProblemType(), request.getDifficulty(), request.getStatus(), request.getSupportedLanguages(), request.getHasInput(), request.getMinAcceptanceRate(), request.getMaxAcceptanceRate());

            // 3. 将DO转换为VO
            List<ProblemVO> problemVOList = problemPage.getRecords().stream().map(this::convertToVO).filter(Objects::nonNull).collect(Collectors.toList());

            // 4. 构建并返回分页结果
            PageResult<ProblemVO> pageResult = new PageResult<>(problemVOList, problemPage.getTotal(), (int) problemPage.getSize(), (int) problemPage.getCurrent(), (int) problemPage.getPages());
            log.info("ProblemService--->按条件查询题目列表成功: 当前页 {}, 每页大小 {}, 总记录数 {}", current, size, problemPage.getTotal());
            return pageResult;
        } catch (Exception e) {
            // 记录异常日志
            log.error("ProblemService--->按条件查询题目列表失败: {}", e.getMessage(), e);
            // 返回空结果
            return new PageResult<>();
        }
    }

    /**
     * 获取当前登录用户创建的问题列表
     * <p>
     * 此方法首先从线程局部变量中获取当前登录用户的信息，
     * 如果用户未登录，则抛出运行时异常
     * 接着，通过问题管理器根据用户账号查询问题，并将查询结果转换为问题视图对象（ProblemVO）列表，
     * 过滤掉可能的空对象，并记录日志信息
     * 如果在执行过程中遇到异常，则记录错误日志并返回空列表
     *
     * @return 当前用户创建的问题列表，如果出现异常则返回空列表
     */
    @Override
    public List<ProblemVO> getProblemsWithCurrentUser() {
        try {
            // 从线程局部变量中获取当前登录用户信息
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) throw new RuntimeException("用户未登录");
            // 查询并转换问题数据为视图对象列表
            List<ProblemVO> voList = problemManager.findByCreatorId(loginUser.getAccountNo()).stream().map(this::convertToVO).filter(Objects::nonNull).collect(Collectors.toList());
            // 记录获取问题列表成功的日志信息
            log.info("获取当前用户创建的题目列表成功，用户ID：{}，题目数量：{}", loginUser.getAccountNo(), voList.size());
            return voList;
        } catch (Exception e) {
            // 记录获取问题列表时出现的异常信息
            log.error("获取当前用户创建的题目列表异常：{}", e.getMessage());
            // 返回空列表作为异常情况下的响应
            return new ArrayList<>();
        }
    }

    @Override
    public Long createProblem(ProblemCreateRequest request) {
        try {

            // 参数校验
            if (request == null) {
                throw new RuntimeException("请求参数为空");
            }

            if (isDuplicateTitle(request.getTitle())) {
                throw new RuntimeException("标题已存在" + request.getTitle());
            }

            // 获取当前登录用户信息
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException("用户未登录");
            }

            // 创建ProblemDO对象并设置基本属性
            ProblemDO problemDO = new ProblemDO();
            BeanUtils.copyProperties(request, problemDO);

            // 设置枚举类型的值
            if (request.getDifficulty() != null) {
                problemDO.setDifficulty(request.getDifficulty().getCode());
            }
            if (request.getStatus() != null) {
                problemDO.setStatus(request.getStatus().getCode());
            }
            if (request.getVisibility() != null) {
                problemDO.setVisibility(request.getVisibility().getCode());
            }

            // 设置Boolean类型的hasInput
            problemDO.setHasInput(request.getHasInput() != null && request.getHasInput() ? 1 : 0);

            // 设置创建者ID
            problemDO.setCreatorId(loginUser.getAccountNo());

            // 设置初始统计值
            problemDO.setSubmissionCount(0L);
            problemDO.setAcceptedCount(0L);

            // 设置非删除状态
            problemDO.setIsDeleted(0);

            // 将各种列表/Map转换为JSON字符串
            if (request.getSupportedLanguages() != null) {
                problemDO.setSupportedLanguages(JSON.toJSONString(request.getSupportedLanguages()));
            }

            if (request.getSolutionTemplates() != null) {
                problemDO.setSolutionTemplates(JSON.toJSONString(request.getSolutionTemplates()));
            }

            if (request.getExamples() != null) {
                problemDO.setExamples(JSON.toJSONString(request.getExamples()));
            }

            if (request.getHints() != null) {
                problemDO.setHints(JSON.toJSONString(request.getHints()));
            }

            if (request.getMetadata() != null) {
                problemDO.setMetadata(JSON.toJSONString(request.getMetadata()));
            }

            // 保存题目并获取ID
            int row = problemManager.save(problemDO);

            // 处理标签关联
            if (row > 0 && request.getTagIds() != null && !request.getTagIds().isEmpty()) {
                // TODO: 关联题目和标签，可能需要调用ProblemTagService
                // problemTagService.associateProblemWithTags(problemId, request.getTagIds());
            }
            Long problemId = problemDO.getId();
            log.info("ProblemService--->创建题目成功，题目ID: {}, 标题: {}", problemId, request.getTitle());
            return problemId;
        } catch (Exception e) {
            log.error("ProblemService--->创建题目失败: {}", e.getMessage(), e);
            return -1L;
        }
    }

    @Override
    public boolean updateProblem(ProblemUpdateRequest request) {
        try {
            return true;
        } catch (Exception e) {
            log.error("ProblemTagService--->更新题目失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean deleteProblem(Long id) {
        try {
            return true;
        } catch (Exception e) {
            log.error("ProblemTagService--->删除题目失败: {}", e.getMessage(), e);
            return false;
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

    private boolean isDuplicateTitle(String title) {
        // 实现查询逻辑，检查标题是否已存在
        return problemManager.existsByTitle(title);
    }
}
