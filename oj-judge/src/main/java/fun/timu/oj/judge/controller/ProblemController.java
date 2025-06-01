package fun.timu.oj.judge.controller;

import fun.timu.oj.common.enmus.BizCodeEnum;
import fun.timu.oj.common.exception.BizException;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.common.utils.JsonData;
import fun.timu.oj.judge.controller.request.ProblemCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemQueryRequest;
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
}
