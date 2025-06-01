package fun.timu.oj.judge.controller;

import fun.timu.oj.common.enmus.BizCodeEnum;
import fun.timu.oj.common.exception.BizException;
import fun.timu.oj.common.utils.JsonData;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.service.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;

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

}
