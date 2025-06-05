package fun.timu.oj.judge.service.impl.Problem;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.enmus.BizCodeEnum;
import fun.timu.oj.common.interceptor.LoginInterceptor;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.common.utils.JsonData;
import fun.timu.oj.judge.controller.request.ProblemCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemQueryRequest;
import fun.timu.oj.judge.controller.request.ProblemUpdateRequest;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.manager.ProblemTagRelationManager;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.VO.ProblemTagVO;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.service.Problem.ProblemCoreService;
import fun.timu.oj.judge.service.ProblemTagRelationService;
import fun.timu.oj.judge.service.ProblemTagService;
import fun.timu.oj.judge.utils.ConvertToUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基础题目服务 (ProblemCoreService)
 * 负责题目的基本CRUD操作：
 * 题目的创建、查询、更新、删除
 * 批量操作（状态更新、可见性修改等）
 * 题目状态管理（发布/下线）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemCoreServiceImpl implements ProblemCoreService {
    private final ProblemManager problemManager;
    private final ProblemTagRelationManager problemTagRelationManager;
    private final ProblemTagRelationService problemTagRelationService;
    private final ProblemTagService tagService;
    private final ProblemTagService problemTagService;

    /**
     * 根据题目ID获取题目详细信息
     * 此方法首先验证用户登录状态，然后根据题目ID检索题目信息，
     * 并进行题目状态和权限验证，最后返回题目详细信息或错误信息
     *
     * @param id 题目ID，用于检索特定题目的详细信息
     * @return 返回包含题目信息的JsonData对象，或包含错误信息的JsonData对象
     */
    @Override
    public JsonData getById(Long id) {
        try {
            //  获取当前登录用户信息
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) throw new RuntimeException("用户未登录");

            // 通过problemManager获取题目数据对象
            ProblemDO problemDO = problemManager.getById(id);

            // 检查题目是否存在且未被删除
            if (problemDO == null || problemDO.getIsDeleted() == 1) throw new RuntimeException("题目不存在或已被删除");

            //  检查题目状态， 如果是隐藏的或者禁用的，则需要检查用户权限，如果不是，则直接返回题目数据
            if ((problemDO.getStatus() == 0 || problemDO.getStatus() == 2) && !loginUser.getAuth().equals("ADMIN")) {
                throw new RuntimeException("题目未发布，无法查看");
            }

            // 如果题目是公开的，则直接返回题目数据
            if (problemDO.getVisibility() == 0 && !loginUser.getAccountNo().equals(problemDO.getCreatorId())) {
                // 如果题目是公开的，且当前用户不是题目的创建者，则抛出异常
                throw new RuntimeException("题目不可见");
            }

            // 将题目数据对象转换为视图对象
            ProblemVO problemVO = ConvertToUtils.convertToVO(problemDO);

            // 查询与题目相关联的标签
            List<ProblemTagVO> tagVOList = tagService.getTagListByProblemId(id);
            problemVO.setTags(tagVOList);
            log.info("ProblemService--->获取题目成功: {}", problemVO);
            return JsonData.buildSuccess(problemVO);
        } catch (Exception e) {
            // 记录获取题目时发生的错误
            log.error("ProblemService--->获取题目失败: {}", e.getMessage(), e);
            return JsonData.buildResult(BizCodeEnum.PROBLEM_NOT_EXIST);
        }
    }


    /**
     * 根据条件获取问题列表
     *
     * @param request 包含查询条件的请求对象
     * @return 包含问题列表的分页结果
     */
    @Override
    public JsonData getProblemsWithConditions(ProblemQueryRequest request) {
        try {
            // 1. 从请求中提取查询参数
            int current = Optional.ofNullable(request.getCurrent()).orElse(1);
            int size = Optional.ofNullable(request.getSize()).orElse(20);

            // 2. 调用Manager层方法获取分页结果
            IPage<ProblemDO> problemPage = problemManager.findTagListWithPage(current, size, request.getProblemType(), request.getDifficulty(), request.getStatus(), request.getVisibility(), request.getSupportedLanguages(), request.getHasInput(), request.getMinAcceptanceRate(), request.getMaxAcceptanceRate());

            // 3. 将DO转换为VO
            List<ProblemVO> problemVOList = problemPage.getRecords().stream().map(ConvertToUtils::convertToVO).filter(Objects::nonNull).collect(Collectors.toList());

            // 4. 获取所有题目的标签信息
            List<Long> problemIds = problemVOList.stream().map(ProblemVO::getId).collect(Collectors.toList());
            Map<Long, List<ProblemTagVO>> tagLists = tagService.getTagListByProblemIds(problemIds);
            // 5. 设置题目标签信息
            problemVOList.forEach(problemVO -> problemVO.setTags(tagLists.get(problemVO.getId())));

            // 6. 构建并返回分页结果
            PageResult<ProblemVO> pageResult = new PageResult<>(problemVOList, problemPage.getTotal(), (int) problemPage.getSize(), (int) problemPage.getCurrent(), (int) problemPage.getPages());
            log.info("ProblemService--->按条件查询题目列表成功: 当前页 {}, 每页大小 {}, 总记录数 {}", current, size, problemPage.getTotal());
            return JsonData.buildSuccess(pageResult);
        } catch (Exception e) {
            // 记录异常日志
            log.error("ProblemService--->按条件查询题目列表失败: {}", e.getMessage(), e);
            // 返回空结果
            return JsonData.buildResult(BizCodeEnum.SYSTEM_ERROR);
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

            // TODO 多表联查优化：在ProblemManager中新增findByCreatorIdWithTags()方法
            // TODO 通过LEFT JOIN problem_tag_relation 和 problem_tag 表一次性获取用户题目及其标签信息
            // TODO 或调用ProblemTagRelationManager.findByProblemIds()批量获取标签关联信息
            // 查询并转换问题数据为视图对象列表
            List<ProblemVO> voList = problemManager.findByCreatorId(loginUser.getAccountNo()).stream().map(ConvertToUtils::convertToVO).filter(Objects::nonNull).collect(Collectors.toList());
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

    /**
     * 创建题目
     * <p>
     * 该方法用于处理题目的创建请求，包括验证请求参数、设置题目属性、保存题目以及处理相关标签关联
     *
     * @param request 题目创建请求对象，包含题目所需的各种信息
     * @return 创建成功的题目ID，若创建失败则返回-1
     */
    @Override
    @Transactional
    public Long createProblem(ProblemCreateRequest request) {
        try {
            //  获取当前登录用户
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException("用户未登录");
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                throw new RuntimeException("用户没有创建题目的权限");
            }

            // 参数校验
            if (request == null) {
                throw new RuntimeException("请求参数为空");
            }

            if (isDuplicateTitle(request.getTitle())) {
                throw new RuntimeException("标题已存在" + request.getTitle());
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
                // TODO 多表联查优化：考虑在ProblemTagRelationManager中新增validateAndBatchAddTags()方法
                // TODO 在添加标签关联前先验证标签是否存在，通过JOIN查询ProblemTagManager.findById()
                // TODO 避免无效标签ID的关联操作
                // 关联题目和标签
                Long problemId = problemDO.getId();
                problemTagRelationService.batchAddTagsToProblem(problemId, request.getTagIds());
            }
            Long problemId = problemDO.getId();
            log.info("ProblemService--->创建题目成功，题目ID: {}, 标题: {}", problemId, request.getTitle());
            return problemId;
        } catch (Exception e) {
            log.error("ProblemService--->创建题目失败: {}", e.getMessage(), e);
            return -1L;
        }
    }

    /**
     * 更新题目
     *
     * @param request 更新题目请求
     * @return
     */
    @Override
    @Transactional
    public boolean updateProblem(ProblemUpdateRequest request) {
        try {
            // 参数校验
            if (request == null || request.getId() == null) {
                throw new RuntimeException("请求参数或ID为空");
            }

            //  获取当前登录用户
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException("用户未登录");
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                throw new RuntimeException("用户没有更新题目的权限");
            }

            // 获取当前题目信息，检查是否存在
            ProblemDO existingProblem = problemManager.getById(request.getId());
            if (existingProblem == null || existingProblem.getIsDeleted() == 1) {
                throw new RuntimeException("题目不存在或已被删除");
            }

            // 检查标题重复（如果请求中包含标题且与原标题不同）
            if (request.getTitle() != null && !request.getTitle().equals(existingProblem.getTitle())) {
                if (problemManager.existsByTitle(request.getTitle())) {
                    throw new RuntimeException("标题已存在: " + request.getTitle());
                }
            }

            // 创建ProblemDO对象并设置ID
            ProblemDO problemDO = new ProblemDO();
            problemDO.setId(request.getId());
            BeanUtils.copyProperties(request, problemDO);

            // 处理JSON字段
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

            // 更新题目
            int row = problemManager.updateById(problemDO);

            // 更新标签关联（如果有）
            if (row > 0 && request.getTagIds() != null && !request.getTagIds().isEmpty()) {
                // TODO 多表联查优化：在ProblemTagRelationManager中优化replaceAllTagsForProblem()方法
                // TODO 通过一次查询获取当前标签关联，对比新标签列表，只操作差异部分
                // TODO 可调用ProblemTagRelationManager.findByProblemId()获取现有关联，减少不必要的删除和插入操作
                // 更新题目和标签的关联
                problemTagRelationService.replaceAllTagsForProblem(request.getId(), request.getTagIds());
            }

            log.info("ProblemService--->更新题目成功，题目ID: {}", request.getId());
            return row > 0;
        } catch (Exception e) {
            log.error("ProblemService--->更新题目失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 删除题目
     * <p>
     * 此方法执行题目的软删除操作，将题目标记为已删除状态
     * 软删除可以保持数据完整性，避免真正删除后无法恢复
     *
     * @param id 题目的唯一标识符
     * @return 如果删除成功返回true，否则返回false
     */
    @Override
    @Transactional
    public boolean deleteProblem(Long id) {
        try {
            //  获取当前登录用户
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException("用户未登录");
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                throw new RuntimeException("用户没有删除题目的权限");
            }

            // 尝试删除题目，实际上是将题目标记为已删除状态
            int row = problemManager.deleteById(id);
            if (row <= 0) throw new RuntimeException("删除题目失败");
            log.info("成功删除题目，ID: {}", id);
            return true;
        } catch (Exception e) {
            // 捕获删除题目时的异常，并记录错误日志
            log.error("ProblemService--->删除题目失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量更新题目状态
     *
     * @param problemIds 题目ID列表
     * @param status     状态值
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean batchUpdateStatus(List<Long> problemIds, Integer status) {
        try {
            // 获取当前登录用户，验证权限
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException("用户未登录");
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                throw new RuntimeException("用户没有批量更新题目状态的权限");
            }

            log.info("开始批量更新题目状态，题目ID列表大小: {}, 目标状态: {}", problemIds.size(), status);

            // 调用manager层执行批量更新
            int updatedCount = problemManager.batchUpdateStatus(problemIds, status);

            if (updatedCount > 0) {
                log.info("批量更新题目状态成功，更新记录数: {}", updatedCount);
                return true;
            } else {
                log.warn("批量更新题目状态未生效，可能题目ID不存在");
                return false;
            }
        } catch (Exception e) {
            log.error("批量更新题目状态失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量更新题目状态失败");
        }
    }

    /**
     * 批量更新题目可见性
     *
     * @param problemIds 题目ID列表
     * @param visibility 可见性值
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean batchUpdateVisibility(List<Long> problemIds, Integer visibility) {
        try {
            // 获取当前登录用户，验证权限
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException("用户未登录");
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                throw new RuntimeException("用户没有批量更新题目可见性的权限");
            }

            log.info("ProblemService--->开始批量更新题目可见性，题目ID列表大小: {}, 目标可见性: {}", problemIds.size(), visibility);

            // 调用manager层执行批量更新
            int updatedCount = problemManager.batchUpdateVisibility(problemIds, visibility);

            log.info("ProblemService--->批量更新题目可见性成功，更新记录数: {}", updatedCount);
            return true;
        } catch (Exception e) {
            log.error("ProblemService--->批量更新题目可见性失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量更新题目可见性失败");
        }
    }


    /**
     * 批量更新题目的时间和内存限制
     *
     * @param problemIds  题目ID列表
     * @param timeLimit   时间限制（秒）
     * @param memoryLimit 内存限制（MB）
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean batchUpdateLimits(List<Long> problemIds, Integer timeLimit, Integer memoryLimit) {
        try {
            // 获取当前登录用户，验证权限
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException("用户未登录");
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                throw new RuntimeException("用户没有批量更新题目限制的权限");
            }

            log.info("ProblemService--->开始批量更新题目时间和内存限制，题目ID列表大小: {}, 时间限制: {}秒, 内存限制: {}MB", problemIds.size(), timeLimit, memoryLimit);

            // 调用manager层执行批量更新
            int updatedCount = problemManager.batchUpdateLimits(problemIds, timeLimit, memoryLimit);

            log.info("ProblemService--->批量更新题目时间和内存限制成功，更新记录数: {}", updatedCount);
            return updatedCount > 0;
        } catch (Exception e) {
            log.error("ProblemService--->批量更新题目时间和内存限制失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量更新题目时间和内存限制失败");
        }
    }

    /**
     * 批量软删除题目
     *
     * @param problemIds 题目ID列表
     * @return 成功删除的题目数量
     */
    @Override
    public int batchSoftDelete(List<Long> problemIds) {
        try {
            log.info("ProblemService--->批量软删除题目, 题目数量: {}", problemIds == null ? 0 : problemIds.size());

            // 参数校验
            if (problemIds == null || problemIds.isEmpty()) {
                log.warn("批量软删除题目失败：题目ID列表为空");
                return 0;
            }

            // 调用manager层执行软删除操作
            int deletedCount = problemManager.batchSoftDelete(problemIds);

            log.info("批量软删除题目成功, 成功删除数量: {}", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("ProblemService--->批量软删除题目失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量软删除题目失败", e);
        }
    }

    /**
     * 批量恢复已删除的题目
     *
     * @param problemIds 需要恢复的题目ID列表
     * @return 成功恢复的题目数量
     */
    @Override
    public int batchRestore(List<Long> problemIds) {
        try {
            log.info("ProblemService--->批量恢复已删除题目, 题目数量: {}", problemIds == null ? 0 : problemIds.size());

            // 参数校验
            if (problemIds == null || problemIds.isEmpty()) {
                log.warn("批量恢复已删除题目失败：题目ID列表为空");
                return 0;
            }

            // 调用manager层执行恢复操作
            int restoredCount = problemManager.batchRestore(problemIds);

            log.info("批量恢复已删除题目成功, 成功恢复数量: {}", restoredCount);
            return restoredCount;
        } catch (Exception e) {
            log.error("ProblemService--->批量恢复已删除题目失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量恢复已删除题目失败", e);
        }
    }


    /**
     * 发布题目
     *
     * @param id 题目ID
     */
    @Override
    public boolean publishProblem(Long id) {
        try {
            log.info("ProblemService--->发布题目, ID: {}", id);
            int row = problemManager.batchUpdateVisibility(List.of(id), 1);
            return row > 0;
        } catch (Exception e) {
            log.error("ProblemService--->发布题目: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 下线题目
     *
     * @param id 题目ID
     */
    @Override
    public boolean unpublishProblem(Long id) {
        try {
            log.info("ProblemService--->下线题目, ID: {}", id);
            int row = problemManager.batchUpdateVisibility(List.of(id), 2);
            return row > 0;
        } catch (Exception e) {
            log.error("ProblemService--->下线题目: {}", e.getMessage(), e);
            return false;
        }
    }


    /**
     * 检查给定标题是否为重复标题
     *
     * @param title 待检查的标题字符串
     * @return 返回一个布尔值，如果标题重复则为true，否则为false
     */
    private boolean isDuplicateTitle(String title) {
        // 实现查询逻辑，检查标题是否已存在
        return problemManager.existsByTitle(title);
    }

}
