package fun.timu.oj.judge.controller;

import fun.timu.oj.common.enmus.BizCodeEnum;
import fun.timu.oj.common.enmus.ProgrammingLanguage;
import fun.timu.oj.common.exception.BizException;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.common.utils.JsonData;
import fun.timu.oj.judge.controller.request.*;
import fun.timu.oj.judge.model.Enums.RankingType;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.model.VTO.PopularProblemCategoryVTO;
import fun.timu.oj.judge.model.VTO.ProblemDetailStatisticsVTO;
import fun.timu.oj.judge.model.VTO.ProblemStatisticsVTO;
import fun.timu.oj.judge.model.criteria.RecommendationCriteria;
import fun.timu.oj.judge.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 题目控制器
 * 提供获取题目详情的接口
 *
 * @author zhengke
 */
@Slf4j
@RestController
@RequestMapping("/api/judge/problem")
@RequiredArgsConstructor
public class ProblemController {
    private final ProblemService problemService;

    /**
     * 获取题目详情
     *
     * @param id 题目ID
     * @return 题目详情
     */
    @GetMapping("/{id}")
    public JsonData getProblem(@PathVariable @Positive(message = "题目ID必须为正数") Long id) {
        log.info("ProblemController--->获取题目详情请求, ID: {}", id);
        try {
            ProblemVO problemVO = problemService.getById(id);
            if (problemVO == null) throw new RuntimeException("题目不存在");
            return JsonData.buildSuccess(problemVO);
        } catch (Exception e) {
            log.error("ProblemController--->获取题目详情异常, id: {}, error: {}", id, e.getMessage(), e);
            throw new BizException(BizCodeEnum.PROBLEM_NOT_EXIST);
        }
    }

    /**
     * 分页查询题目列表
     *
     * @param request 查询请求对象，包含分页和筛选条件
     * @return 分页题目列表
     */
    @GetMapping("/list")
    public JsonData listProblems(@Valid @RequestBody ProblemQueryRequest request) {
        log.info("ProblemController--->分页查询题目列表请求: {}", request);
        try {
            PageResult<ProblemVO> result = problemService.getProblemsWithConditions(request);
            if (result == null) throw new RuntimeException("查询失败");
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("ProblemController--->分页查询题目列表异常: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取当前登录用户创建的题目列表
     * TODO: 考虑使用分页
     *
     * @return 当前用户创建的题目列表
     */
    @GetMapping("/my")
    public JsonData getMyProblems() {
        try {
            log.info("ProblemController--->获取当前用户创建的题目列表");
            // 调用服务层方法获取当前用户创建的题目列表
            List<ProblemVO> problemList = problemService.getProblemsWithCurrentUser();
            return JsonData.buildSuccess(problemList);
        } catch (Exception e) {
            log.error("ProblemController--->获取当前用户创建的题目列表失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.PROBLEM_NOT_EXIST);
        }
    }

    /**
     * 创建新题目
     *
     * @param request 创建题目的请求对象
     * @return 创建结果，包含新题目的ID
     */
    @PostMapping
    public JsonData createProblem(@Valid @RequestBody ProblemCreateRequest request) {
        try {
            log.info("创建问题请求: {}", request);
            Long problemId = problemService.createProblem(request);
            if (problemId < 0) throw new RuntimeException("创建问题失败");

            Map<String, Object> result = new HashMap<>();
            result.put("problemId", problemId);
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("ProblemController--->创建问题失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.PROBLEM_CREATE_FAILED);
        }
    }

    /**
     * 更新题目
     * 接收题目更新请求并返回更新结果
     *
     * @param request 题目更新请求对象
     * @return JsonData 包含更新结果的响应
     */
    @PutMapping
    public JsonData updateProblem(@Valid @RequestBody ProblemUpdateRequest request) {
        try {
            log.info("更新题目请求: {}", request);
            // 参数校验
            if (request.getId() == null) {
                throw new RuntimeException("题目ID不能为空");
            }

            boolean result = problemService.updateProblem(request);
            if (!result) {
                throw new RuntimeException("更新题目失败");
            }
            return JsonData.buildSuccess();
        } catch (Exception e) {
            log.error("ProblemController--->更新题目失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.PROBLEM_UPDATE_FAILED);
        }
    }

    /**
     * 删除题目
     * 接收题目删除请求并返回删除结果
     *
     * @param id 题目ID
     * @return JsonData 包含删除结果的响应
     */
    @DeleteMapping("/{id}")
    public JsonData deleteProblem(@PathVariable @Positive Long id) {
        try {
            log.info("删除题目请求, ID: {}", id);
            boolean result = problemService.deleteProblem(id);
            if (!result) throw new RuntimeException("删除题目失败");
            return JsonData.buildSuccess();
        } catch (Exception e) {
            log.error("ProblemController--->删除题目失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.PROBLEM_DELETE_FAILED);
        }
    }

    /**
     * 更新题目提交统计
     * TODO: 此接口后续需要弃用, 集成到判题逻辑中处理
     *
     * @param id         题目ID
     * @param isAccepted 是否通过
     * @return 更新结果
     */
    @PutMapping("/{id}/submission-stats")
    public JsonData updateSubmissionStats(@PathVariable @Positive Long id, @RequestParam(required = true) Boolean isAccepted) {
        try {
            log.info("更新题目提交统计请求, ID: {}, 是否通过: {}", id, isAccepted);
            boolean result = problemService.updateSubmissionStats(id, isAccepted);
            if (!result) {
                throw new RuntimeException("更新题目提交统计失败，题目可能不存在");
            }
            return JsonData.buildSuccess();
        } catch (Exception e) {
            log.error("ProblemController--->更新题目提交统计失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.PROBLEM_UPDATE_FAILED);
        }
    }

    /**
     * 获取热门题目列表
     * TODO 后续需要添加分页
     *
     * @param request 包含查询参数的请求对象
     * @return 热门题目列表
     */
    @PostMapping("/hot")
    public JsonData getHotProblems(@Valid @RequestBody HotProblemRequest request) {
        try {
            log.info("ProblemController--->获取热门题目列表请求, 类型: {}, 难度: {}, 数量限制: {}", request.getProblemType(), request.getDifficulty(), request.getLimit());

            List<ProblemVO> problemList = problemService.selectHotProblems(request.getProblemType(), request.getDifficulty(), request.getLimit());

            return JsonData.buildSuccess(problemList);
        } catch (Exception e) {
            log.error("ProblemController--->获取热门题目列表异常: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }


    /**
     * 获取统一推荐题目（新接口）
     * 支持多种推荐算法：通过率、相似性、热度、算法数据
     *
     * @param request 统一推荐请求参数
     * @return 推荐题目列表或带评分的推荐题目列表
     * @since 2.0
     */
    @PostMapping("/recommendations")
    public JsonData getUnifiedRecommendations(@Valid @RequestBody UnifiedRecommendationRequest request) {
        try {
            log.info("ProblemController--->获取统一推荐题目请求, 推荐类型: {}, 参数: {}", request.getType(), request);

            // 构建推荐条件
            RecommendationCriteria criteria = buildRecommendationCriteria(request);

            if (request.getIncludeScore()) {
                // 返回带评分的推荐题目
                List<Map<String, Object>> recommendedProblemsWithScore = problemService.getRecommendedProblemsWithScore(criteria);
                return JsonData.buildSuccess(recommendedProblemsWithScore);
            } else {
                // 返回普通推荐题目
                List<ProblemVO> problemList = problemService.getRecommendedProblems(criteria);
                return JsonData.buildSuccess(problemList);
            }
        } catch (Exception e) {
            log.error("ProblemController--->获取统一推荐题目异常: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR, "获取推荐题目失败");
        }
    }

    /**
     * 获取推荐题目列表（旧接口，推荐使用新的统一接口）
     *
     * @param request 包含查询参数的请求对象
     * @return 推荐题目列表
     * @deprecated 此方法已弃用，请使用 {@link #getUnifiedRecommendations(UnifiedRecommendationRequest)} 替代
     */
    @GetMapping("/recommended")
    @Deprecated
    public JsonData getRecommendedProblems(@Valid @RequestBody RecommendedProblemRequest request) {
        try {
            log.info("ProblemController--->获取推荐题目列表请求, minAcceptanceRate: {}, maxAcceptanceRate: {}, difficulty: {}, limit: {}", request.getMinAcceptanceRate(), request.getMaxAcceptanceRate(), request.getDifficulty(), request.getLimit());

            List<ProblemVO> problemList = problemService.selectRecommendedProblems(request.getMinAcceptanceRate(), request.getMaxAcceptanceRate(), request.getDifficulty(), request.getLimit());

            return JsonData.buildSuccess(problemList);
        } catch (Exception e) {
            log.error("ProblemController--->获取推荐题目列表异常: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取题目统计信息
     *
     * @return 按题目类型和难度分组的统计数据，包括总题目数量、活跃题目数量、提交数和通过率等信息
     */
    @GetMapping("/statistics")
    public JsonData getProblemStatistics() {
        try {
            log.info("ProblemController--->获取题目统计信息");
            List<ProblemStatisticsVTO> statistics = problemService.getProblemStatistics();
            return JsonData.buildSuccess(statistics);
        } catch (Exception e) {
            log.error("ProblemController--->获取题目统计信息异常: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 批量更新题目状态
     *
     * @param request 包含题目ID列表和目标状态的请求对象
     * @return 更新结果
     */
    @PutMapping("/batch-status")
    public JsonData batchUpdateStatus(@Valid @RequestBody BatchUpdateStatusRequest request) {
        try {
            log.info("ProblemController--->批量更新题目状态请求, 题目数量: {}, 目标状态: {}", request.getProblemIds().size(), request.getStatus());

            boolean result = problemService.batchUpdateStatus(request.getProblemIds(), request.getStatus());

            if (!result) {
                throw new RuntimeException("批量更新题目状态失败");
            }

            return JsonData.buildSuccess();
        } catch (Exception e) {
            log.error("ProblemController--->批量更新题目状态失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.PROBLEM_UPDATE_FAILED);
        }
    }

    /**
     * 根据创建者ID统计题目数量
     *
     * @param creatorId 创建者ID
     * @return 包含题目数量的响应
     */
    @GetMapping("/count/creator/{creatorId}")
    public JsonData countByCreator(@PathVariable @Positive(message = "创建者ID必须为正数") Long creatorId) {
        try {
            log.info("ProblemController--->根据创建者ID统计题目数量, 创建者ID: {}", creatorId);
            Long count = problemService.countByCreator(creatorId);

            Map<String, Object> result = new HashMap<>();
            result.put("count", count);

            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("ProblemController--->根据创建者ID统计题目数量失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 查询最近创建的题目
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param limit    限制返回的题目总数（可为null，表示无上限）
     * @return 最近创建的题目列表
     */
    @GetMapping("/recent")
    public JsonData getRecentProblems(@RequestParam(defaultValue = "1") @Positive int pageNum, @RequestParam(defaultValue = "10") @Positive int pageSize, @RequestParam(required = false) Integer limit) {
        try {
            log.info("ProblemController--->查询最近创建的题目, 页码: {}, 每页数量: {}, 限制数量: {}", pageNum, pageSize, limit);

            List<ProblemVO> problemList = problemService.selectRecentProblems(pageNum, pageSize, limit);

            return JsonData.buildSuccess(problemList);
        } catch (Exception e) {
            log.error("ProblemController--->查询最近创建的题目失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
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
    @GetMapping("/language")
    public JsonData getProblemsByLanguage(@RequestParam(defaultValue = "1") @Positive int pageNum, @RequestParam(defaultValue = "10") @Positive int pageSize, @RequestParam(required = true) ProgrammingLanguage language) {
        try {
            log.info("ProblemController--->根据支持的编程语言查询题目, 页码: {}, 每页数量: {}, 语言: {}", pageNum, pageSize, language);

            PageResult<ProblemVO> pageResult = problemService.selectByLanguage(pageNum, pageSize, language.name());

            return JsonData.buildSuccess(pageResult);
        } catch (Exception e) {
            log.error("ProblemController--->根据支持的编程语言查询题目失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 批量软删除题目
     *
     * @return 通用响应对象，包含成功删除的题目数量
     */
    @PostMapping("/batch-delete")
    public JsonData batchSoftDelete(@Valid @RequestBody BatchProblemRequest request) {
        log.info("批量软删除题目，题目ID列表: {}", request.getProblemIds());
        try {
            int deletedCount = problemService.batchSoftDelete(request.getProblemIds());
            return JsonData.buildSuccess("批量软删除题目成功: " + deletedCount + " 道题目已被软删除");
        } catch (Exception e) {
            log.error("ProblemController--->批量软删除题目失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 批量恢复已删除的题目
     *
     * @return 通用响应对象，包含成功恢复的题目数量
     */
    @PostMapping("/batch-restore")
    public JsonData batchRestore(@Valid @RequestBody BatchProblemRequest request) {
        log.info("批量恢复已删除题目，题目ID列表: {}", request.getProblemIds());
        try {
            int restoredCount = problemService.batchRestore(request.getProblemIds());
            return JsonData.buildSuccess("批量恢复删除题目成功: " + restoredCount + " 道题目已被恢复");
        } catch (Exception e) {
            log.error("ProblemController--->批量恢复已删除题目失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取指定题目的通过率
     * TODO 需要优化返回值 不应该是Map
     *
     * @param problemId 题目ID
     * @return 通用响应对象，包含题目的通过率
     */
    @GetMapping("/acceptance-rate/{problemId}")
    public JsonData getAcceptanceRate(@PathVariable Long problemId) {
        log.info("获取题目通过率，题目ID: {}", problemId);
        try {
            Double acceptanceRate = problemService.getAcceptanceRate(problemId);
            Map<String, Object> result = new HashMap<>();
            result.put("acceptanceRate", acceptanceRate);
            result.put("problemId", problemId);
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("ProblemController--->获取题目通过率失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 批量获取题目基本信息
     *
     * @return 通用响应对象，包含题目基本信息列表
     */
    @PostMapping("/basic-info")
    public JsonData getBasicInfoByIds(@Valid @RequestBody BatchProblemRequest request) {
        log.info("批量获取题目基本信息，题目ID列表: {}", request.getProblemIds());
        try {
            List<ProblemVO> problemVOList = problemService.selectBasicInfoByIds(request.getProblemIds());
            return JsonData.buildSuccess(problemVOList);
        } catch (Exception e) {
            log.error("ProblemController--->批量获取题目基本信息失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取题目详细统计信息
     *
     * @return 包含各种维度统计数据的响应
     */
    @GetMapping("/detail-statistics")
    public JsonData getProblemDetailStatistics() {
        try {
            log.info("ProblemController--->获取题目详细统计信息");
            ProblemDetailStatisticsVTO statistics = problemService.getProblemDetailStatistics();
            return JsonData.buildSuccess(statistics);
        } catch (Exception e) {
            log.error("ProblemController--->获取题目详细统计信息异常: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取最受欢迎的题目类型和难度组合
     * <p>
     * 聚合：数据展示用
     *
     * @param limit 返回结果数量限制
     * @return 包含题目类型、难度及其统计信息的列表
     */
    @GetMapping("/popular-categories")
    public JsonData getPopularProblemCategories(@RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            log.info("ProblemController--->获取最受欢迎的题目类型和难度组合, limit: {}", limit);
            List<PopularProblemCategoryVTO> result = problemService.getPopularProblemCategories(limit);
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("ProblemController--->获取最受欢迎的题目类型和难度组合失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 根据创建时间范围查询题目
     *
     * @param startDate 开始日期，格式为yyyy-MM-dd
     * @param endDate   结束日期，格式为yyyy-MM-dd
     * @param pageNum   页码，默认为1
     * @param pageSize  每页大小，默认为10
     * @return 分页题目列表结果
     */
    @GetMapping("/date-range")
    public JsonData selectByDateRange(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, @RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize) {
        try {
            // 调用Service层方法获取时间范围内的题目
            PageResult<ProblemVO> result = problemService.selectByDateRange(startDate, endDate, pageNum, pageSize);
            log.info("ProblemController--->成功查询时间范围内的题目，开始时间：{}，结束时间：{}，页码：{}，每页数量：{}", startDate, endDate, pageNum, pageSize);
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("ProblemController--->根据创建时间范围查询题目失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 查询相似题目（基于难度）
     * TODO 后续需要修改 考虑使用单独的Mapper接口 提供支持标签查询的功能（由题目关联获取）
     *
     * @param request 包含problemId、difficulty、problemType和limit的请求对象
     * @return 相似题目列表
     */
    @PostMapping("/similar")
    public JsonData findSimilarProblems(@Valid @RequestBody SimilarProblemRequest request) {
        // 参数校验
        if (request.getProblemId() == null || request.getProblemId() <= 0) {
            return JsonData.buildError("题目ID不能为空或无效");
        }

        List<ProblemVO> problems = problemService.findSimilarProblems(request.getProblemId(), request.getDifficulty(), request.getProblemType(), request.getLimit());

        return JsonData.buildSuccess(problems);
    }

    /**
     * 批量更新题目可见性
     *
     * @param request 包含题目ID列表和目标可见性的请求对象
     * @return 更新结果
     */
    @PutMapping("/batch-visibility")
    public JsonData batchUpdateVisibility(@Valid @RequestBody BatchUpdateVisibilityRequest request) {
        try {
            log.info("ProblemController--->批量更新题目可见性请求, 题目数量: {}, 目标可见性: {}", request.getProblemIds().size(), request.getVisibility());

            boolean result = problemService.batchUpdateVisibility(request.getProblemIds(), request.getVisibility());

            if (!result) {
                throw new RuntimeException("批量更新题目可见性失败");
            }
            return JsonData.buildSuccess();
        } catch (Exception e) {
            log.error("ProblemController--->批量更新题目可见性失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.PROBLEM_UPDATE_FAILED);
        }
    }

    /**
     * 批量更新题目的时间和内存限制
     *
     * @param request 包含题目ID列表、时间限制和内存限制的请求对象
     * @return 更新结果
     */
    @PutMapping("/batch-limits")
    public JsonData batchUpdateLimits(@Valid @RequestBody BatchUpdateLimitsRequest request) {
        try {
            log.info("ProblemController--->批量更新题目时间和内存限制请求, 题目数量: {}, 时间限制: {}, 内存限制: {}", request.getProblemIds().size(), request.getTimeLimit(), request.getMemoryLimit());

            // 参数校验 - 至少要有一个不为null
            if (request.getTimeLimit() == null && request.getMemoryLimit() == null) {
                return JsonData.buildError("时间限制和内存限制至少需要指定一个");
            }

            boolean result = problemService.batchUpdateLimits(request.getProblemIds(), request.getTimeLimit(), request.getMemoryLimit());

            if (!result) {
                throw new RuntimeException("批量更新题目时间和内存限制失败");
            }
            return JsonData.buildSuccess();
        } catch (Exception e) {
            log.error("ProblemController--->批量更新题目时间和内存限制失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.PROBLEM_UPDATE_FAILED);
        }
    }

    /**
     * 批量重置题目统计数据（将提交次数和通过次数重置为0）
     *
     * @param request 包含题目ID列表的请求对象
     * @return 重置结果
     */
    @PostMapping("/batch-reset-statistics")
    public JsonData batchResetStatistics(@Valid @RequestBody BatchProblemRequest request) {
        try {
            log.info("ProblemController--->批量重置题目统计数据请求, 题目数量: {}", request.getProblemIds().size());

            // 参数校验
            if (request.getProblemIds() == null || request.getProblemIds().isEmpty()) {
                return JsonData.buildError("题目ID列表不能为空");
            }

            // 调用服务层方法执行重置操作
            int resetCount = problemService.batchResetStatistics(request.getProblemIds());

            Map<String, Object> result = new HashMap<>();
            result.put("resetCount", resetCount);

            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("ProblemController--->批量重置题目统计数据失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 查询长时间未更新的题目
     *
     * @param days     超过多少天未更新视为长时间未更新
     * @param pageNum  页码，默认为1
     * @param pageSize 每页大小，默认为10
     * @return 分页结果，包含符合条件的题目列表
     */
    @GetMapping("/stale")
    public JsonData getStaleProblems(@RequestParam(defaultValue = "30") @Positive(message = "天数必须为正数") int days, @RequestParam(defaultValue = "1") @Positive(message = "页码必须为正数") int pageNum, @RequestParam(defaultValue = "10") @Positive(message = "每页大小必须为正数") int pageSize) {
        try {
            log.info("ProblemController--->查询长时间未更新的题目, 超过{}天, 页码: {}, 每页大小: {}", days, pageNum, pageSize);

            PageResult<ProblemVO> result = problemService.selectStaleProblems(days, pageNum, pageSize);
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("ProblemController--->查询长时间未更新的题目失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 查询零提交的题目（即 submission_count = 0 的题目）
     *
     * @param pageNum  页码，默认为1
     * @param pageSize 每页大小，默认为10
     * @return 分页结果，包含零提交的题目列表
     */
    @GetMapping("/no-submissions")
    public JsonData getProblemsWithoutSubmissions(@RequestParam(defaultValue = "1") @Positive(message = "页码必须为正数") int pageNum, @RequestParam(defaultValue = "10") @Positive(message = "每页大小必须为正数") int pageSize) {
        try {
            log.info("ProblemController--->查询零提交的题目, 页码: {}, 每页大小: {}", pageNum, pageSize);

            PageResult<ProblemVO> result = problemService.selectProblemsWithoutSubmissions(pageNum, pageSize);
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("ProblemController--->查询零提交的题目失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 发布题目
     *
     * @param id 题目ID
     * @return 操作结果
     */
    @PostMapping("/{id}/publish")
    public JsonData publishProblem(@PathVariable Long id) {
        try {
            log.info("ProblemController--->发布题目, 题目ID: {}", id);
            if (id == null || id <= 0) {
                return JsonData.buildError("无效的题目ID");
            }

            boolean result = problemService.publishProblem(id);
            if (result) {
                return JsonData.buildSuccess();
            } else {
                return JsonData.buildError("发布题目失败");
            }
        } catch (Exception e) {
            log.error("ProblemController--->发布题目失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.PROBLEM_UPDATE_FAILED);
        }
    }

    /**
     * 下线题目
     *
     * @param id 题目ID
     * @return 操作结果
     */
    @PostMapping("/{id}/unpublish")
    public JsonData unpublishProblem(@PathVariable Long id) {
        try {
            log.info("ProblemController--->下线题目, 题目ID: {}", id);
            if (id == null || id <= 0) {
                return JsonData.buildError("无效的题目ID");
            }

            boolean result = problemService.unpublishProblem(id);
            if (result) {
                return JsonData.buildSuccess();
            } else {
                return JsonData.buildError("下线题目失败");
            }
        } catch (Exception e) {
            log.error("ProblemController--->下线题目失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.PROBLEM_UPDATE_FAILED);
        }
    }

    /**
     * 构建推荐条件对象
     *
     * @param request 统一推荐请求
     * @return 推荐条件对象
     */
    private RecommendationCriteria buildRecommendationCriteria(UnifiedRecommendationRequest request) {
        RecommendationCriteria criteria = new RecommendationCriteria();
        criteria.setType(request.getType());
        criteria.setMinAcceptanceRate(request.getMinAcceptanceRate());
        criteria.setMaxAcceptanceRate(request.getMaxAcceptanceRate());
        criteria.setDifficulty(request.getDifficulty());
        criteria.setProblemType(request.getProblemType());
        criteria.setBaseProblemId(request.getBaseProblemId());
        criteria.setTimeRange(request.getTimeRange());
        criteria.setLimit(request.getLimit());
        return criteria;
    }

    // ==================== 分布统计类接口 ====================

    /**
     * 按难度获取统计信息
     *
     * @return 各难度级别的题目统计信息
     */
    @GetMapping("/statistics/difficulty")
    public JsonData getStatisticsByDifficulty() {
        try {
            log.info("ProblemController--->按难度获取统计信息");
            List<Map<String, Object>> statistics = problemService.getStatisticsByDifficulty();
            return JsonData.buildSuccess(statistics);
        } catch (Exception e) {
            log.error("ProblemController--->按难度获取统计信息失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 按题目类型获取统计信息
     *
     * @return 各题目类型的统计信息
     */
    @GetMapping("/statistics/type")
    public JsonData getStatisticsByType() {
        try {
            log.info("ProblemController--->按题目类型获取统计信息");
            List<Map<String, Object>> statistics = problemService.getStatisticsByType();
            return JsonData.buildSuccess(statistics);
        } catch (Exception e) {
            log.error("ProblemController--->按题目类型获取统计信息失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 按编程语言获取统计信息
     *
     * @return 各编程语言的统计信息
     */
    @GetMapping("/statistics/language")
    public JsonData getStatisticsByLanguage() {
        try {
            log.info("ProblemController--->按编程语言获取统计信息");
            List<Map<String, Object>> statistics = problemService.getStatisticsByLanguage();
            return JsonData.buildSuccess(statistics);
        } catch (Exception e) {
            log.error("ProblemController--->按编程语言获取统计信息失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 按状态获取统计信息
     *
     * @return 各状态的统计信息
     */
    @GetMapping("/statistics/status")
    public JsonData getStatisticsByStatus() {
        try {
            log.info("ProblemController--->按状态获取统计信息");
            List<Map<String, Object>> statistics = problemService.getStatisticsByStatus();
            return JsonData.buildSuccess(statistics);
        } catch (Exception e) {
            log.error("ProblemController--->按状态获取统计信息失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    // ==================== 趋势分析类接口 ====================

    /**
     * 获取题目创建趋势
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 时间粒度（day/week/month）
     * @return 题目创建趋势数据
     */
    @GetMapping("/trend/creation")
    public JsonData getProblemCreationTrend(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, @RequestParam(defaultValue = "day") String granularity) {
        try {
            log.info("ProblemController--->获取题目创建趋势, 开始日期: {}, 结束日期: {}, 时间粒度: {}", startDate, endDate, granularity);
            List<Map<String, Object>> trendData = problemService.getProblemCreationTrend(startDate, endDate, granularity);
            return JsonData.buildSuccess(trendData);
        } catch (Exception e) {
            log.error("ProblemController--->获取题目创建趋势失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取提交趋势分析
     *
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @param granularity 时间粒度（day/week/month）
     * @return 提交趋势数据
     */
    @GetMapping("/trend/submission")
    public JsonData getSubmissionTrendAnalysis(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, @RequestParam(defaultValue = "day") String granularity) {
        try {
            log.info("ProblemController--->获取提交趋势分析, 开始日期: {}, 结束日期: {}, 时间粒度: {}", startDate, endDate, granularity);
            List<Map<String, Object>> trendData = problemService.getSubmissionTrendAnalysis(startDate, endDate, granularity);
            return JsonData.buildSuccess(trendData);
        } catch (Exception e) {
            log.error("ProblemController--->获取提交趋势分析失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    // ==================== 排名类接口 ====================

    /**
     * 获取热门题目排行榜
     *
     * @param limit     限制数量，默认10
     * @param timeRange 时间范围（天数），默认30天
     * @return 热门题目排行榜
     */
    @GetMapping("/ranking/popular")
    public JsonData getPopularProblemsRanking(@RequestParam(defaultValue = "10") Integer limit, @RequestParam(defaultValue = "30") Integer timeRange) {
        try {
            log.info("ProblemController--->获取热门题目排行榜, 限制数量: {}, 时间范围: {}天", limit, timeRange);
            List<Map<String, Object>> ranking = problemService.getPopularProblemsRanking(limit, timeRange);
            return JsonData.buildSuccess(ranking);
        } catch (Exception e) {
            log.error("ProblemController--->获取热门题目排行榜失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取最难题目排行榜
     *
     * @param limit 限制数量，默认10
     * @return 最难题目排行榜
     */
    @GetMapping("/ranking/hardest")
    public JsonData getHardestProblemsRanking(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            log.info("ProblemController--->获取最难题目排行榜, 限制数量: {}", limit);
            List<Map<String, Object>> ranking = problemService.getHardestProblemsRanking(limit);
            return JsonData.buildSuccess(ranking);
        } catch (Exception e) {
            log.error("ProblemController--->获取最难题目排行榜失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取最容易题目排行榜
     *
     * @param limit 限制数量，默认10
     * @return 最容易题目排行榜
     */
    @GetMapping("/ranking/easiest")
    public JsonData getEasiestProblemsRanking(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            log.info("ProblemController--->获取最容易题目排行榜, 限制数量: {}", limit);
            List<Map<String, Object>> ranking = problemService.getEasiestProblemsRanking(limit);
            return JsonData.buildSuccess(ranking);
        } catch (Exception e) {
            log.error("ProblemController--->获取最容易题目排行榜失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取最常提交题目排行榜
     *
     * @param limit     限制数量，默认10
     * @param timeRange 时间范围（天数），默认30天
     * @return 最常提交题目排行榜
     */
    @GetMapping("/ranking/most-submitted")
    public JsonData getMostSubmittedProblemsRanking(@RequestParam(defaultValue = "10") Integer limit, @RequestParam(defaultValue = "30") Integer timeRange) {
        try {
            log.info("ProblemController--->获取最常提交题目排行榜, 限制数量: {}, 时间范围: {}天", limit, timeRange);
            List<Map<String, Object>> ranking = problemService.getMostSubmittedProblemsRanking(limit, timeRange);
            return JsonData.buildSuccess(ranking);
        } catch (Exception e) {
            log.error("ProblemController--->获取最常提交题目排行榜失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取零提交题目排行榜
     *
     * @param limit     限制数量，默认10
     * @param timeRange 时间范围（天数），默认30天
     * @return 零提交题目排行榜
     */
    @GetMapping("/ranking/zero-submitted")
    public JsonData getZeroSubmittedProblemsRanking(@RequestParam(defaultValue = "10") Integer limit, @RequestParam(defaultValue = "30") Integer timeRange) {
        try {
            log.info("ProblemController--->获取零提交题目排行榜, 限制数量: {}, 时间范围: {}天", limit, timeRange);
            List<Map<String, Object>> ranking = problemService.getZeroSubmittedProblemsRanking(limit, timeRange);
            return JsonData.buildSuccess(ranking);
        } catch (Exception e) {
            log.error("ProblemController--->获取零提交题目排行榜失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取最近热门题目排行榜
     *
     * @param limit 限制数量，默认10
     * @param days  最近天数，默认7天
     * @return 最近热门题目排行榜
     */
    @GetMapping("/ranking/recent-popular")
    public JsonData getRecentPopularProblemsRanking(@RequestParam(defaultValue = "10") Integer limit, @RequestParam(defaultValue = "7") Integer days) {
        try {
            log.info("ProblemController--->获取最近热门题目排行榜, 限制数量: {}, 最近天数: {}", limit, days);
            List<Map<String, Object>> ranking = problemService.getRecentPopularProblemsRanking(limit, days);
            return JsonData.buildSuccess(ranking);
        } catch (Exception e) {
            log.error("ProblemController--->获取最近热门题目排行榜失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 统一的排行榜接口（使用枚举类型）
     *
     * @param type  排行榜类型
     * @param limit 限制数量，默认10
     * @return 排行榜数据列表
     */
    @GetMapping("/ranking/unified")
    public JsonData getProblemRankingByType(@RequestParam RankingType type, @RequestParam(defaultValue = "10") Integer limit) {
        try {
            log.info("ProblemController--->获取统一排行榜, 类型: {}, 限制数量: {}", type, limit);
            List<Map<String, Object>> ranking = problemService.getProblemRanking(type, limit);
            return JsonData.buildSuccess(ranking);
        } catch (Exception e) {
            log.error("ProblemController--->获取统一排行榜失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 通用的排行榜接口（使用字符串类型和条件参数）
     *
     * @param request 包含排行榜类型和条件的请求对象
     * @return 题目排名结果
     */
    @PostMapping("/ranking/generic")
    public JsonData getGenericProblemRanking(@Valid @RequestBody ProblemRankingRequest request) {
        try {
            log.info("ProblemController--->获取通用排行榜, 类型: {}, 条件: {}", request.getRankingType(), request.getCriteria());
            List<Map<String, Object>> ranking = problemService.getProblemRanking(request.getRankingType(), request.getCriteria());
            return JsonData.buildSuccess(ranking);
        } catch (Exception e) {
            log.error("ProblemController--->获取通用排行榜失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    // ==================== 报告类接口 ====================

    /**
     * 获取月度报告
     *
     * @param year  年份
     * @param month 月份
     * @return 月度报告数据
     */
    @GetMapping("/report/monthly")
    public JsonData getMonthlyReport(@RequestParam int year, @RequestParam int month) {
        try {
            log.info("ProblemController--->获取月度报告, 年份: {}, 月份: {}", year, month);

            // 参数校验
            if (year < 2000 || year > 3000) {
                return JsonData.buildError("年份参数无效");
            }
            if (month < 1 || month > 12) {
                return JsonData.buildError("月份参数无效");
            }

            Map<String, Object> report = problemService.getMonthlyReport(year, month);
            return JsonData.buildSuccess(report);
        } catch (Exception e) {
            log.error("ProblemController--->获取月度报告失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取年度报告
     *
     * @param year 年份
     * @return 年度报告数据
     */
    @GetMapping("/report/annual")
    public JsonData getAnnualReport(@RequestParam int year) {
        try {
            log.info("ProblemController--->获取年度报告, 年份: {}", year);

            // 参数校验
            if (year < 2000 || year > 3000) {
                return JsonData.buildError("年份参数无效");
            }

            Map<String, Object> report = problemService.getAnnualReport(year);
            return JsonData.buildSuccess(report);
        } catch (Exception e) {
            log.error("ProblemController--->获取年度报告失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 获取自定义报告
     *
     * @param request 包含开始日期、结束日期和指标列表的请求对象
     * @return 自定义报告数据
     */
    @PostMapping("/report/custom")
    public JsonData getCustomReport(@Valid @RequestBody CustomReportRequest request) {
        try {
            log.info("ProblemController--->获取自定义报告, 开始日期: {}, 结束日期: {}, 指标: {}", request.getStartDate(), request.getEndDate(), request.getMetrics());

            Map<String, Object> report = problemService.getCustomReport(request.getStartDate(), request.getEndDate(), request.getMetrics());
            return JsonData.buildSuccess(report);
        } catch (Exception e) {
            log.error("ProblemController--->获取自定义报告失败: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }

}
