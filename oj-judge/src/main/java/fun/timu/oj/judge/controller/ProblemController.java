package fun.timu.oj.judge.controller;

import fun.timu.oj.common.enmus.BizCodeEnum;
import fun.timu.oj.common.enmus.ProgrammingLanguage;
import fun.timu.oj.common.exception.BizException;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.common.utils.JsonData;
import fun.timu.oj.judge.controller.request.*;
import fun.timu.oj.judge.model.DTO.ProblemDetailStatisticsDTO;
import fun.timu.oj.judge.model.DTO.ProblemStatisticsDTO;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
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
     * 获取推荐题目列表
     *
     * @param request 包含查询参数的请求对象
     * @return 推荐题目列表
     */
    @GetMapping("/recommended")
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
            List<ProblemStatisticsDTO> statistics = problemService.getProblemStatistics();
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
            ProblemDetailStatisticsDTO statistics = problemService.getProblemDetailStatistics();
            return JsonData.buildSuccess(statistics);
        } catch (Exception e) {
            log.error("ProblemController--->获取题目详细统计信息异常: {}", e.getMessage(), e);
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
    }
}
