package fun.timu.oj.judge.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.enmus.ProblemDifficultyEnum;
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
import fun.timu.oj.judge.model.DTO.PopularProblemCategoryDTO;
import fun.timu.oj.judge.model.DTO.ProblemDetailStatisticsDTO;
import fun.timu.oj.judge.model.DTO.ProblemStatisticsDTO;
import fun.timu.oj.judge.model.Enums.DistributionDimension;
import fun.timu.oj.judge.model.Enums.RankingType;
import fun.timu.oj.judge.model.VO.ExampleVO;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.model.VTO.PopularProblemCategoryVTO;
import fun.timu.oj.judge.model.VTO.ProblemDetailStatisticsVTO;
import fun.timu.oj.judge.model.VTO.ProblemStatisticsVTO;
import fun.timu.oj.judge.model.criteria.DistributionCriteria;
import fun.timu.oj.judge.model.criteria.RankingCriteria;
import fun.timu.oj.judge.model.criteria.RecommendationCriteria;
import fun.timu.oj.judge.model.criteria.TrendCriteria;
import fun.timu.oj.judge.service.ProblemService;
import fun.timu.oj.judge.service.ProblemTagRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ProblemTagRelationService problemTagRelationService;

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
            ProblemVO problemVO = convertToVO(problemDO);

            // 获取题目标签
            List<Long> tagIds = problemTagRelationService.getTagIdsByProblemId(id);
//            problemVO.setTags(tagIds);

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
            IPage<ProblemDO> problemPage = problemManager.findTagListWithPage(current, size, request.getProblemType(), request.getDifficulty(), request.getStatus(), request.getVisibility(), request.getSupportedLanguages(), request.getHasInput(), request.getMinAcceptanceRate(), request.getMaxAcceptanceRate());

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
     * 更新题目提交统计
     * <p>
     * 此方法用于更新题目的提交统计信息，包括总提交次数和通过次数
     * 当用户提交一个题目的解答时，无论解答是否正确，总提交次数都会增加
     * 只有当解答被接受（即正确）时，通过次数才会增加
     *
     * @param problemId  题目ID
     * @param isAccepted 提交是否被接受
     * @return 更新是否成功
     */
    @Override
    public boolean updateSubmissionStats(Long problemId, boolean isAccepted) {
        try {
            // 获取当前登录用户信息
            LoginUser loginUser = LoginInterceptor.threadLocal.get();

            if (loginUser == null) {
                throw new RuntimeException("用户未登录");
            }
            log.info("更新题目[{}]提交统计，提交结果: {}", problemId, isAccepted ? "通过" : "未通过");

            // 调用manager层方法更新统计数据
            int result = problemManager.updateSubmissionStats(problemId, loginUser, isAccepted);

            if (result > 0) {
                // 更新成功
                return true;
            } else if (result == 0) {
                log.warn("更新题目[{}]提交统计失败，可能无记录被更新", problemId);
                return false;
            } else {
                log.warn("更新题目[{}]提交统计失败，题目可能不存在", problemId);
                return false;
            }
        } catch (Exception e) {
            log.error("ProblemService--->更新题目提交统计失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取热门题目列表
     *
     * @param problemType 题目类型
     * @param difficulty  题目难度
     * @param limit       返回数量限制，默认为10
     * @return 热门题目列表
     */
    @Override
    public List<ProblemVO> selectHotProblems(String problemType, Integer difficulty, Integer limit) {
        try {
            // 调用manager层获取热门题目数据
            List<ProblemDO> problemDOList = problemManager.selectHotProblems(problemType, difficulty, limit);

            // 将DO列表转换为VO列表
            List<ProblemVO> problemVOList = problemDOList.stream().map(this::convertToVO).filter(Objects::nonNull).collect(Collectors.toList());

            log.info("ProblemService--->获取热门题目列表成功，类型: {}, 难度: {}, 数量: {}", problemType, difficulty, problemVOList.size());
            return problemVOList;
        } catch (Exception e) {
            log.error("ProblemService--->获取热门题目列表失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据指定条件选择推荐的问题
     *
     * @param minAcceptanceRate 最小接受率，用于筛选问题
     * @param maxAcceptanceRate 最大接受率，用于筛选问题
     * @param difficulty        难度级别，用于筛选问题
     * @param limit             最多返回的问题数量
     * @param minAcceptanceRate 最小通过率
     * @param maxAcceptanceRate 最大通过率
     * @param difficulty        难度级别，用于筛选问题
     * @param limit             最多返回的问题数量
     * @return 返回一个ProblemVO对象列表，包含根据条件筛选出的问题
     * <p>
     * 该方法首先调用problemManager的selectRecommendedProblems方法来获取符合条件的问题列表，
     * 然后将这些问题从ProblemDO对象转换为ProblemVO对象，以便于后续处理或传输
     * 如果在处理过程中遇到异常，将记录错误日志并抛出运行时异常
     * /**
     * 查询推荐题目（旧接口，推荐使用新的统一接口）
     * 为了保持向后兼容性，此方法将调用转换为新的统一接口
     * @return 返回一个ProblemVO对象列表，包含根据条件筛选出的问题
     * <p>
     * 该方法首先调用problemManager的selectRecommendedProblems方法来获取符合条件的问题列表，
     * 然后将这些问题从ProblemDO对象转换为ProblemVO对象，以便于后续处理或传输
     * 如果在处理过程中遇到异常，将记录错误日志并抛出运行时异常
     * @deprecated 此方法已弃用，请使用 {@link #getRecommendedProblems(RecommendationCriteria)} 替代
     */
    @Override
    @Deprecated
    public List<ProblemVO> selectRecommendedProblems(Double minAcceptanceRate, Double maxAcceptanceRate, Integer difficulty, Integer limit) {
        // 调用新的统一接口
        RecommendationCriteria criteria = RecommendationCriteria.forAcceptanceRate(minAcceptanceRate, maxAcceptanceRate, difficulty, limit);
        return getRecommendedProblems(criteria);
    }

    /**
     * 获取推荐题目（统一接口）
     * 支持多种推荐算法：通过率、相似性、热度、算法数据
     *
     * @param criteria 推荐条件，包含推荐类型和相关参数
     * @return 推荐题目列表
     * @since 2.0
     */
    @Override
    public List<ProblemVO> getRecommendedProblems(RecommendationCriteria criteria) {
        try {
            log.info("ProblemService--->获取推荐题目开始, 推荐类型:{}, 条件:{}", criteria.getType(), criteria);

            List<ProblemDO> problemDOList = problemManager.getRecommendedProblems(criteria);
            List<ProblemVO> result = problemDOList.stream().map(this::convertToVO).collect(Collectors.toList());

            log.info("ProblemService--->获取推荐题目成功, 推荐类型:{}, 返回数量:{}", criteria.getType(), result.size());
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取推荐题目失败, 推荐类型:{}, 错误: {}", criteria.getType(), e.getMessage(), e);
            throw new RuntimeException("获取推荐题目失败", e);
        }
    }

    /**
     * 获取带评分的推荐题目（统一接口）
     * 支持多种推荐算法，返回题目及其推荐评分
     *
     * @param criteria 推荐条件，包含推荐类型和相关参数
     * @return 带评分的推荐题目列表
     * @since 2.0
     */
    @Override
    public List<Map<String, Object>> getRecommendedProblemsWithScore(RecommendationCriteria criteria) {
        try {
            log.info("ProblemService--->获取带评分推荐题目开始, 推荐类型:{}, 条件:{}", criteria.getType(), criteria);

            List<Map<String, Object>> recommendedProblemsWithScore = problemManager.getRecommendedProblemsWithScore(criteria);

            log.info("ProblemService--->获取带评分推荐题目成功, 推荐类型:{}, 返回数量:{}", criteria.getType(), recommendedProblemsWithScore.size());
            return recommendedProblemsWithScore;
        } catch (Exception e) {
            log.error("ProblemService--->获取带评分推荐题目失败, 推荐类型:{}, 错误: {}", criteria.getType(), e.getMessage(), e);
            throw new RuntimeException("获取带评分推荐题目失败", e);
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
     * 获取题目统计信息
     * 该方法返回按题目类型和难度分组的统计数据，包括总题目数量、活跃题目数量、
     * 总提交次数、总通过次数以及平均通过率等信息
     *
     * @return 统计信息列表
     */
    @Override
    public List<ProblemStatisticsVTO> getProblemStatistics() {
        try {
            log.info("ProblemService--->获取题目统计信息");
            List<ProblemStatisticsDTO> problemStatistics = problemManager.getProblemStatistics();
            List<ProblemStatisticsVTO> problemStatisticsVTOList = problemStatistics.stream().map(problemStatisticsDTO -> {
                ProblemStatisticsVTO problemStatisticsVTO = ProblemStatisticsVTO.fromDTO(problemStatisticsDTO);
                return problemStatisticsVTO;
            }).collect(Collectors.toList());
            log.info("ProblemService--->获取题目统计信息成功，数量: {}", problemStatisticsVTOList.size());
            return problemStatisticsVTOList;
        } catch (Exception e) {
            log.error("ProblemService--->获取题目统计信息异常: {}", e.getMessage(), e);
            throw new RuntimeException("获取题目统计信息失败", e);
        }
    }


    /**
     * 根据创建者ID统计题目数量
     *
     * @param creatorId 创建者ID
     * @return 创建者创建的题目数量
     */
    @Override
    public Long countByCreator(Long creatorId) {
        try {
            // 参数校验
            if (creatorId == null || creatorId <= 0) {
                throw new RuntimeException("创建者ID无效");
            }

            // 调用manager层查询题目数量
            Long count = problemManager.countByCreator(creatorId);
            log.info("查询创建者题目数量成功，创建者ID: {}, 题目数量: {}", creatorId, count);
            return count;
        } catch (Exception e) {
            log.error("查询创建者题目数量失败, 创建者ID: {}, 错误: {}", creatorId, e.getMessage(), e);
            throw new RuntimeException("查询创建者题目数量失败", e);
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
     * 获取指定题目的通过率
     *
     * @param problemId 题目ID
     * @return 题目的通过率，如果题目不存在或从未被提交过，则返回0.0
     */
    @Override
    public Double getAcceptanceRate(Long problemId) {
        try {
            log.info("ProblemService--->获取题目通过率, 题目ID: {}", problemId);

            // 参数校验
            if (problemId == null || problemId <= 0) {
                log.warn("获取题目通过率失败：题目ID无效");
                return 0.0;
            }

            // 调用manager层获取通过率
            Double acceptanceRate = problemManager.getAcceptanceRate(problemId);

            log.info("获取题目通过率成功, 题目ID: {}, 通过率: {}", problemId, acceptanceRate);
            return acceptanceRate;
        } catch (Exception e) {
            log.error("ProblemService--->获取题目通过率失败: {}", e.getMessage(), e);
            return 0.0;
        }
    }


    /**
     * 获取题目详细统计信息
     *
     * @return 包含各种维度统计数据的HashMap，包括题目总数、难度分布、类型分布、提交情况等
     */
    @Override
    public ProblemDetailStatisticsVTO getProblemDetailStatistics() {
        try {
            log.info("ProblemService--->获取题目详细统计信息");
            ProblemDetailStatisticsDTO problemDetailStatistics = problemManager.getProblemDetailStatistics();
            ProblemDetailStatisticsVTO problemDetailStatisticsVTO = ProblemDetailStatisticsVTO.fromDTO(problemDetailStatistics);
            log.info("ProblemService--->获取题目详细统计信息成功, 统计信息: {}", problemDetailStatisticsVTO.toString());
            return problemDetailStatisticsVTO;
        } catch (Exception e) {
            log.error("ProblemService--->获取题目详细统计信息失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取最受欢迎的题目类型和难度组合
     *
     * @param limit 返回结果数量限制
     * @return 包含题目类型、难度及其统计信息的列表
     */
    @Override
    public List<PopularProblemCategoryVTO> getPopularProblemCategories(Integer limit) {
        try {
            log.info("ProblemService--->获取最受欢迎的题目类型和难度组合, limit: {}", limit);
            List<PopularProblemCategoryDTO> popularProblemCategories = problemManager.getPopularProblemCategories(limit);
            List<PopularProblemCategoryVTO> popularProblemCategoriesVTO = PopularProblemCategoryVTO.fromDTOList(popularProblemCategories);
            log.info("ProblemService--->获取最受欢迎的题目类型和难度组合成功, 获取到的组合数量: {}", popularProblemCategoriesVTO.size());
            return popularProblemCategoriesVTO;
        } catch (Exception e) {
            log.error("ProblemService--->获取最受欢迎的题目类型和难度组合失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据创建时间范围查询题目
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 分页题目列表
     */
    @Override
    public PageResult<ProblemVO> selectByDateRange(Date startDate, Date endDate, int pageNum, int pageSize) {
        try {
            // 记录日志
            log.info("按时间范围查询题目, 开始时间: {}, 结束时间: {}, 页码: {}, 每页大小: {}", startDate, endDate, pageNum, pageSize);

            // 默认查询状态为1（激活状态）的题目
            Integer status = 1;

            // 调用Manager层方法获取分页数据
            IPage<ProblemDO> problemPage = problemManager.selectByDateRange(startDate, endDate, status, pageNum, pageSize);

            if (problemPage == null || problemPage.getRecords().isEmpty()) {
                throw new RuntimeException("没有找到符合要求的题目");
            }

            // 转换DO对象为VO对象
            List<ProblemVO> problemVOList = problemPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

            PageResult<ProblemVO> pageResult = new PageResult<>(problemVOList, problemPage.getTotal(), (int) problemPage.getSize(), (int) problemPage.getCurrent(), (int) problemPage.getPages());
            log.info("成功查询时间范围内的题目列表，当前页: {}, 每页大小: {}, 总数: {}", pageNum, pageSize, problemPage.getTotal());
            return pageResult;
        } catch (Exception e) {
            log.error("按时间范围查询题目失败: {}", e.getMessage(), e);
            return new PageResult<>();
        }
    }

    /**
     * 查询相似题目（基于标签和难度）
     *
     * @param problemId   题目ID
     * @param difficulty  难度限制
     * @param problemType 题目类型限制
     * @param limit       返回数量限制
     * @return 相似题目列表
     */
    @Override
    public List<ProblemVO> findSimilarProblems(Long problemId, Integer difficulty, String problemType, Integer limit) {
        try {
            log.info("ProblemService--->查询相似题目, 题目ID: {}, 难度: {}, 题目类型: {}, 限制数量: {}", problemId, difficulty, problemType, limit);

            // 参数校验
            if (problemId == null || problemId <= 0) {
                throw new RuntimeException("题目ID无效");
            }

            // 调用manager层查询相似题目
            List<ProblemDO> problemDOList = problemManager.findSimilarProblems(problemId, difficulty, problemType, limit);

            // 将DO列表转换为VO列表
            List<ProblemVO> problemVOList = problemDOList.stream().map(this::convertToVO).filter(Objects::nonNull).collect(Collectors.toList());

            log.info("查询相似题目成功, 题目ID: {}, 获取到 {} 个相似题目", problemId, problemVOList.size());
            return problemVOList;
        } catch (Exception e) {
            log.error("ProblemService--->查询相似题目失败: {}", e.getMessage(), e);
            return new ArrayList<>();
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
     * 批量重置题目统计数据（将提交次数和通过次数重置为0）
     *
     * @param problemIds 题目ID列表
     * @return 成功重置的题目数量
     */
    @Override
    @Transactional
    public int batchResetStatistics(List<Long> problemIds) {
        try {
            // 获取当前登录用户，验证权限
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException("用户未登录");
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                throw new RuntimeException("用户没有重置题目统计数据的权限");
            }

            log.info("ProblemService--->批量重置题目统计数据, 题目数量: {}", problemIds == null ? 0 : problemIds.size());

            // 参数校验
            if (problemIds == null || problemIds.isEmpty()) {
                throw new RuntimeException("参数错误, 题目ID列表不能为空");
            }

            // 调用manager层执行重置操作
            int resetCount = problemManager.batchResetStats(problemIds);

            log.info("批量重置题目统计数据成功, 成功重置数量: {}", resetCount);
            return resetCount;
        } catch (Exception e) {
            log.error("ProblemService--->批量重置题目统计数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量重置题目统计数据失败", e);
        }
    }

    /**
     * 查询长时间未更新的题目
     *
     * @param days     超过多少天未更新视为长时间未更新
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<ProblemVO> selectStaleProblems(int days, int pageNum, int pageSize) {
        try {
            // 计算目标日期：当前日期减去指定的天数
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            Date lastUpdateBefore = calendar.getTime();

            // 调用管理层方法查询数据
            IPage<ProblemDO> problemPage = problemManager.selectStaleProblems(lastUpdateBefore, pageNum, pageSize);


            // 转换DO对象为VO对象
            List<ProblemVO> problemVOList = problemPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

            // 构建并返回分页结果
            PageResult<ProblemVO> pageResult = new PageResult<>(problemVOList, problemPage.getTotal(), (int) problemPage.getSize(), (int) problemPage.getCurrent(), (int) problemPage.getPages());
            log.info("ProblemService--->成功查询长时间未更新题目，当前页: {}, 每页大小: {}, 总数: {}", pageNum, pageSize, problemPage.getTotal());
            return pageResult;
        } catch (Exception e) {
            log.error("ProblemService--->查询长时间未更新题目失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询长时间未更新题目失败", e);
        }
    }

    /**
     * 查询零提交的题目（即 submission_count = 0 的题目）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<ProblemVO> selectProblemsWithoutSubmissions(int pageNum, int pageSize) {
        try {
            log.info("ProblemService--->查询零提交的题目, 页码: {}, 每页大小: {}", pageNum, pageSize);

            // 调用manager层查询零提交题目
            IPage<ProblemDO> problemPage = problemManager.selectProblemsWithoutSubmissions(pageNum, pageSize);

            // 将DO列表转换为VO列表
            List<ProblemVO> problemVOList = problemPage.getRecords().stream().map(this::convertToVO).filter(Objects::nonNull).collect(Collectors.toList());

            // 构建分页结果
            PageResult<ProblemVO> pageResult = new PageResult<>(problemVOList, problemPage.getTotal(), (int) problemPage.getSize(), (int) problemPage.getCurrent(), (int) problemPage.getPages());

            log.info("查询零提交题目成功，总数: {}", pageResult.getTotal());
            return pageResult;
        } catch (Exception e) {
            log.error("ProblemService--->查询零提交题目失败: {}", e.getMessage(), e);
            return new PageResult<>();
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

    // ==================== 分布统计类方法实现 ====================


    /**
     * 按难度获取统计信息
     *
     * @return 各难度级别的题目统计信息
     */
    @Override
    public List<Map<String, Object>> getStatisticsByDifficulty() {
        try {
            log.info("ProblemService--->按难度获取统计信息开始");

            List<Map<String, Object>> result = problemManager.getStatisticsByDifficulty();

            log.info("ProblemService--->按难度获取统计信息成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->按难度获取统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("按难度获取统计信息失败", e);
        }
    }

    /**
     * 按题目类型获取统计信息
     *
     * @return 各题目类型的统计信息
     */
    @Override
    public List<Map<String, Object>> getStatisticsByType() {
        try {
            log.info("ProblemService--->按题目类型获取统计信息开始");

            List<Map<String, Object>> result = problemManager.getStatisticsByType();

            log.info("ProblemService--->按题目类型获取统计信息成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->按题目类型获取统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("按题目类型获取统计信息失败", e);
        }
    }

    /**
     * 按编程语言获取统计信息
     *
     * @return 各编程语言的统计信息
     */
    @Override
    public List<Map<String, Object>> getStatisticsByLanguage() {
        try {
            log.info("ProblemService--->按编程语言获取统计信息开始");

            List<Map<String, Object>> result = problemManager.getStatisticsByLanguage();

            log.info("ProblemService--->按编程语言获取统计信息成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->按编程语言获取统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("按编程语言获取统计信息失败", e);
        }
    }

    /**
     * 按状态获取统计信息
     *
     * @return 各状态的统计信息
     */
    @Override
    public List<Map<String, Object>> getStatisticsByStatus() {
        try {
            log.info("ProblemService--->按状态获取统计信息");
            List<Map<String, Object>> result = problemManager.getStatisticsByStatus();
            log.info("ProblemService--->按状态获取统计信息成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->按状态获取统计信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("按状态获取统计信息失败", e);
        }
    }

    // ==================== 趋势分析类方法实现 ====================


    /**
     * 获取题目创建趋势
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 时间粒度
     * @return 创建趋势数据
     */
    @Override
    public List<Map<String, Object>> getProblemCreationTrend(Date startDate, Date endDate, String granularity) {
        try {
            log.info("ProblemService--->获取题目创建趋势, 开始日期: {}, 结束日期: {}, 粒度: {}", startDate, endDate, granularity);
            List<Map<String, Object>> result = problemManager.getProblemCreationTrend(startDate, endDate, granularity);
            log.info("ProblemService--->获取题目创建趋势成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取题目创建趋势失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取题目创建趋势失败", e);
        }
    }


    /**
     * 获取提交趋势分析
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 时间粒度
     * @return 提交趋势数据
     */
    @Override
    public List<Map<String, Object>> getSubmissionTrendAnalysis(Date startDate, Date endDate, String granularity) {
        try {
            log.info("ProblemService--->获取提交趋势分析, 开始日期: {}, 结束日期: {}, 粒度: {}", startDate, endDate, granularity);
            List<Map<String, Object>> result = problemManager.getSubmissionTrendAnalysis(startDate, endDate, granularity);
            log.info("ProblemService--->获取提交趋势分析成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取提交趋势分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取提交趋势分析失败", e);
        }
    }


    // ==================== 排名类方法实现 ====================

    /**
     * 获取热门题目排行榜
     *
     * @param limit     限制数量
     * @param timeRange 时间范围
     * @return 热门题目排行榜
     */
    @Override
    public List<Map<String, Object>> getPopularProblemsRanking(Integer limit, Integer timeRange) {
        try {
            log.info("ProblemService--->获取热门题目排行榜, 限制数量: {}, 时间范围: {}", limit, timeRange);
            List<Map<String, Object>> result = problemManager.getPopularProblemsRanking(limit, timeRange);
            log.info("ProblemService--->获取热门题目排行榜成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取热门题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取热门题目排行榜失败", e);
        }
    }

    /**
     * 获取最难题目排行榜
     *
     * @param limit          限制数量
     * @return 最难题目排行榜
     */
    @Override
    public List<Map<String, Object>> getHardestProblemsRanking(Integer limit) {
        try {
            log.info("ProblemService--->获取最难题目排行榜, 限制数量: {}", limit);
            List<Map<String, Object>> result = problemManager.getHardestProblemsRanking(limit);
            log.info("ProblemService--->获取最难题目排行榜成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取最难题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取最难题目排行榜失败", e);
        }
    }


    /**
     * 获取最简单题目排行榜
     *
     * @param limit          限制数量
     * @return 最简单题目排行榜
     */
    @Override
    public List<Map<String, Object>> getEasiestProblemsRanking(Integer limit) {
        try {
            log.info("ProblemService--->获取最简单题目排行榜, 限制数量: {}", limit );
            List<Map<String, Object>> result = problemManager.getEasiestProblemsRanking(limit);
            log.info("ProblemService--->获取最简单题目排行榜成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取最简单题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取最简单题目排行榜失败", e);
        }
    }


    /**
     * 获取提交最多题目排行榜
     *
     * @param limit     限制数量
     * @param timeRange 时间范围
     * @return 提交最多题目排行榜
     */
    @Override
    public List<Map<String, Object>> getMostSubmittedProblemsRanking(Integer limit, Integer timeRange) {
        try {
            log.info("ProblemService--->获取提交最多题目排行榜, 限制数量: {}, 时间范围: {}", limit, timeRange);

            List<Map<String, Object>> result = problemManager.getMaxSubmissionProblemsRanking(limit, timeRange);

            log.info("ProblemService--->获取提交最多题目排行榜成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取提交最多题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取提交最多题目排行榜失败", e);
        }
    }

    /**
     * 获取提交零提交题目排行榜
     *
     * @param limit     限制数量
     * @param timeRange 时间范围
     * @return 提交最多题目排行榜
     */
    @Override
    public List<Map<String, Object>> getZeroSubmittedProblemsRanking(Integer limit, Integer timeRange) {
        try {
            log.info("ProblemService--->获取提交最多题目排行榜, 限制数量: {}, 时间范围: {}", limit, timeRange);

            List<Map<String, Object>> result = problemManager.getZeroSubmissionProblemsRanking(limit, timeRange);

            log.info("ProblemService--->获取提交最多题目排行榜成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取提交最多题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取提交最多题目排行榜失败", e);
        }
    }



    /**
     * 获取最近热门题目排行榜
     *
     * @param limit 限制数量
     * @param days  最近天数
     * @return 最近热门题目排行榜
     */
    @Override
    public List<Map<String, Object>> getRecentPopularProblemsRanking(Integer limit, Integer days) {
        try {
            log.info("ProblemService--->获取最近热门题目排行榜, 限制数量: {}, 最近天数: {}", limit, days);

            // 使用RankingCriteria
            RankingCriteria criteria = RankingCriteria.builder()
                    .type(RankingType.POPULARITY)
                    .limit(limit != null ? limit : 10)
                    .dayRange(days)
                    .build();

            List<Map<String, Object>> result = problemManager.getProblemRanking(criteria);

            log.info("ProblemService--->获取最近热门题目排行榜成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取最近热门题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取最近热门题目排行榜失败", e);
        }
    }

    /**
     * 统一的排行榜接口
     *
     * @param type 排行榜类型
     * @return 排行榜数据列表
     */
    @Override
    public List<Map<String, Object>> getProblemRanking(RankingType type, Integer limit) {
        try {
            log.info("ProblemService--->获取题目排行榜, 类型: {}", type);

            // 构建排行榜条件
            RankingCriteria criteria = RankingCriteria.fromRankingType(type, limit, null, null, null);

            // 调用管理器方法获取排行榜数据
            List<Map<String, Object>> result = problemManager.getProblemRanking(criteria);

            log.info("ProblemService--->获取题目排行榜成功, 数量: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取题目排行榜失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取题目排行榜失败: " + e.getMessage(), e);
        }
    }

    // ==================== 统一统计接口实现 ====================


    /**
     * 获取题目排名
     *
     * @param rankingType 排名类型
     * @param criteria    排名条件
     * @return 题目排名结果
     */
    @Override
    public List<Map<String, Object>> getProblemRanking(String rankingType, Map<String, Object> criteria) {
        try {
            log.info("ProblemService--->获取题目排名开始, 排名类型: {}, 条件: {}", rankingType, criteria);

            // 创建RankingCriteria对象并设置参数
            RankingCriteria rankingCriteria = new RankingCriteria();
            rankingCriteria.setType(RankingType.valueOf(rankingType));
            // 将Map中的参数设置到RankingCriteria对象中
            if (criteria != null) {
                if (criteria.containsKey("limit")) {
                    rankingCriteria.setLimit((Integer) criteria.get("limit"));
                }
                if (criteria.containsKey("timeRange")) {
                    rankingCriteria.setTimeRange((Integer) criteria.get("timeRange"));
                }
                if (criteria.containsKey("minSubmissions")) {
                    rankingCriteria.setMinSubmissions((Integer) criteria.get("minSubmissions"));
                }
            }

            List<Map<String, Object>> result = problemManager.getProblemRanking(rankingCriteria);

            log.info("ProblemService--->获取题目排名成功, 排名类型: {}, 返回数量: {}", rankingType, result.size());
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取题目排名失败, 排名类型: {}, 错误: {}", rankingType, e.getMessage(), e);
            throw new RuntimeException("获取题目排名失败", e);
        }
    }

    // ==================== 报告类方法实现 ====================

    /**
     * 获取月度报告
     *
     * @param year  年份
     * @param month 月份
     * @return 月度报告数据
     */
    @Override
    public Map<String, Object> getMonthlyReport(int year, int month) {
        try {
            log.info("ProblemService--->获取月度报告开始, 年份: {}, 月份: {}", year, month);

            Map<String, Object> result = problemManager.getMonthlyReport(year, month);

            log.info("ProblemService--->获取月度报告成功, 年份: {}, 月份: {}", year, month);
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取月度报告失败, 年份: {}, 月份: {}, 错误: {}", year, month, e.getMessage(), e);
            throw new RuntimeException("获取月度报告失败", e);
        }
    }

    /**
     * 获取年度报告
     *
     * @param year 年份
     * @return 年度报告数据
     */
    @Override
    public Map<String, Object> getAnnualReport(int year) {
        try {
            log.info("ProblemService--->获取年度报告开始, 年份: {}", year);

            Map<String, Object> result = problemManager.getAnnualReport(year);

            log.info("ProblemService--->获取年度报告成功, 年份: {}", year);
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取年度报告失败, 年份: {}, 错误: {}", year, e.getMessage(), e);
            throw new RuntimeException("获取年度报告失败", e);
        }
    }

    /**
     * 获取自定义报表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param metrics   指标列表
     * @return 自定义报表数据
     */
    @Override
    public Map<String, Object> getCustomReport(Date startDate, Date endDate, List<String> metrics) {
        try {
            log.info("ProblemService--->获取自定义报表, 开始日期: {}, 结束日期: {}, 指标: {}", startDate, endDate, metrics);
            Map<String, Object> result = problemManager.getCustomRangeReport(startDate, endDate, metrics);
            log.info("ProblemService--->获取自定义报表成功");
            return result;
        } catch (Exception e) {
            log.error("ProblemService--->获取自定义报表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取自定义报表失败", e);
        }
    }

    /**
     * 将ProblemDO转换为基本信息的ProblemVO
     * 只包含题目的基本字段，不包含详细内容
     *
     * @param problemDO 题目DO对象
     * @return 基本信息的题目VO对象
     */
    private ProblemVO convertToBasicVO(ProblemDO problemDO) {
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

    /**
     * 将ProblemDO对象转换为ProblemVO对象
     * 此方法主要用于数据传输对象（DO）到视图对象（VO）的属性复制和处理，以用于展示层
     * 它不仅复制基本属性，还处理一些属性的特殊逻辑，如难度标签、语言列表、示例代码等
     *
     * @param problemDO ProblemDO对象，包含问题的基本信息和配置如果为null，则返回null
     * @return ProblemVO对象，包含转换后的数据以及一些额外处理过的属性如果输入为null，则返回null
     */
    private ProblemVO convertToVO(ProblemDO problemDO) {
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
    private <T> List<T> parseJsonToList(Object jsonField, String fieldName, Class<T> clazz) {
        // 检查输入是否为非空字符串，这是进行JSON解析的前提条件
        if (jsonField != null && jsonField instanceof String && !((String) jsonField).trim().isEmpty()) {
            String str = (String) jsonField;
            try {
                // 尝试将JSON字符串解析为指定类型的列表
                return JSON.parseObject(str, new TypeReference<List<T>>(clazz) {
                });
            } catch (Exception e) {
                // 捕获解析异常，并记录警告日志
                log.warn("Failed to parse {} JSON: {}", fieldName, str, e);
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
    private double calculateAcceptanceRate(Long submissionCount, Long acceptedCount) {
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
     * 检查给定标题是否为重复标题
     *
     * @param title 待检查的标题字符串
     * @return 返回一个布尔值，如果标题重复则为true，否则为false
     */
    private boolean isDuplicateTitle(String title) {
        // 实现查询逻辑，检查标题是否已存在
        return problemManager.existsByTitle(title);
    }


    /**
     * 根据题目ID列表获取题目基本信息
     *
     * @param problemIds 题目ID列表
     * @return 包含题目基本信息的列表
     */
    @Override
    public List<ProblemVO> selectBasicInfoByIds(List<Long> problemIds) {
        try {
            log.info("ProblemService--->批量获取题目基本信息, 题目数量: {}", problemIds == null ? 0 : problemIds.size());

            // 参数校验
            if (problemIds == null || problemIds.isEmpty()) {
                log.warn("批量获取题目基本信息失败：题目ID列表为空");
                return new ArrayList<>();
            }

            // 调用manager层获取题目基本信息
            List<ProblemDO> problemDOList = problemManager.selectBasicInfoByIds(problemIds);

            // 将DO列表转换为VO列表
            List<ProblemVO> problemVOList = problemDOList.stream().map(this::convertToBasicVO).filter(Objects::nonNull).collect(Collectors.toList());

            log.info("批量获取题目基本信息成功, 获取到的题目数量: {}", problemVOList.size());
            return problemVOList;
        } catch (Exception e) {
            log.error("ProblemService--->批量获取题目基本信息失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
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


    /**
     * 查询最近创建的题目
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param limit    限制返回的题目总数（可为null，表示无上限）
     * @return 最近创建的题目列表
     */
    @Override
    public List<ProblemVO> selectRecentProblems(int pageNum, int pageSize, Integer limit) {
        try {
            // 调用manager层查询最近创建的题目
            IPage<ProblemDO> problemPage = problemManager.selectRecentProblems(pageNum, pageSize, limit);

            // 将DO列表转换为VO列表
            List<ProblemVO> problemVOList = problemPage.getRecords().stream().map(this::convertToVO).filter(Objects::nonNull).collect(Collectors.toList());

            log.info("ProblemService--->获取最近创建的题目成功，页码: {}, 每页数量: {}, 限制数量: {}, 实际获取数量: {}", pageNum, pageSize, limit, problemVOList.size());

            return problemVOList;
        } catch (Exception e) {
            log.error("ProblemService--->获取最近创建的题目失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }


    /**
     * 根据支持的编程语言查询题目
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param language 编程语言
     * @return 分页题目列表结果
     */
    @Override
    public PageResult<ProblemVO> selectByLanguage(int pageNum, int pageSize, String language) {
        try {
            log.info("ProblemService--->根据支持的编程语言查询题目, 页码: {}, 每页数量: {}, 语言: {}", pageNum, pageSize, language);

            // 调用manager层获取分页数据
            IPage<ProblemDO> problemPage = problemManager.selectByLanguage(pageNum, pageSize, language);

            // 将DO列表转换为VO列表
            List<ProblemVO> problemVOList = problemPage.getRecords().stream().map(this::convertToVO).filter(Objects::nonNull).collect(Collectors.toList());

            // 构建分页结果
            PageResult<ProblemVO> pageResult = new PageResult<>(problemVOList, problemPage.getTotal(), (int) problemPage.getSize(), (int) problemPage.getCurrent(), (int) problemPage.getPages());

            log.info("根据编程语言查询题目成功, 语言: {}, 总数: {}", language, pageResult.getTotal());
            return pageResult;
        } catch (Exception e) {
            log.error("ProblemService--->根据支持的编程语言查询题目失败: {}", e.getMessage(), e);
            return new PageResult<>();
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
}















