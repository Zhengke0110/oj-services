package fun.timu.oj.judge.controller;

import fun.timu.oj.common.utils.JsonData;
import fun.timu.oj.judge.controller.request.ProblemTagCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemTagUpdateRequest;
import fun.timu.oj.judge.service.ProblemTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 问题标签控制器
 * 处理问题标签的创建请求
 */
@Slf4j
@RestController
@RequestMapping("/api/judge/tag")
public class ProblemTagController {
    private final ProblemTagService problemTagService;

    public ProblemTagController(ProblemTagService problemTagService) {
        this.problemTagService = problemTagService;
    }


    /**
     * 创建标签
     * 接收标签创建请求并返回创建结果
     *
     * @param request 标签创建请求对象
     * @return JsonData 包含创建结果的响应
     */
    @PostMapping
    public JsonData createTag(@Valid @RequestBody ProblemTagCreateRequest request) {
        try {
            log.info("创建标签请求: {}", request);
            Long tagID = problemTagService.createTag(request);
            if (tagID < 0) throw new RuntimeException("创建标签失败");
            return JsonData.buildSuccess(tagID);
        } catch (Exception e) {
            log.error("创建标签失败: {}", e.getMessage(), e);
            return JsonData.buildError("创建标签失败");
        }
    }

    /**
     * 更新标签
     * 接收标签更新请求并返回更新结果
     *
     * @param request 标签更新请求对象
     * @return JsonData 包含更新结果的响应
     */
    @PutMapping
    public JsonData updateTag(@Valid @RequestBody ProblemTagUpdateRequest request) {
        try {
            log.info("更新标签请求: {}", request);
            boolean result = problemTagService.updateTag(request);
            if (!result) throw new RuntimeException("更新标签失败");
            return JsonData.buildSuccess();
        } catch (RuntimeException e) {
            log.error("更新标签失败: {}", e.getMessage(), e);
            return JsonData.buildError("更新标签失败");
        }
    }
}
