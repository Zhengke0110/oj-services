package fun.timu.oj.judge.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.common.utils.JsonData;
import fun.timu.oj.judge.controller.request.ProblemTagCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemTagUpdateRequest;
import fun.timu.oj.judge.model.VO.ProblemTagVO;
import fun.timu.oj.judge.service.ProblemTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.HashMap;
import java.util.List;
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


    /**
     * 删除标签
     * 接收标签删除请求并返回删除结果
     *
     * @param id 标签ID
     * @return JsonData 包含删除结果的响应
     */
    @DeleteMapping("/{id}")
    public JsonData deleteTag(@PathVariable @Positive Long id) {
        try {
            log.info("删除标签请求, ID: {}", id);
            boolean result = problemTagService.deleteTag(id);
            if (!result) throw new RuntimeException("删除标签失败");
            return JsonData.buildSuccess();
        } catch (Exception e) {
            log.error("删除标签失败: {}", e.getMessage(), e);
            return JsonData.buildError("删除标签失败");
        }
    }

    /**
     * 根据ID获取标签详情
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public JsonData getTag(@PathVariable @Positive Long id) {
        try {
            log.info("获取标签请求, ID: {}", id);
            ProblemTagVO tagVO = problemTagService.getTagById(id);
            if (tagVO == null) {
                throw new RuntimeException("标签不存在或已被删除");
            }
            return JsonData.buildSuccess(tagVO);
        } catch (RuntimeException e) {
            log.error("根据ID获取标签失败: {}", e.getMessage(), e);
            return JsonData.buildError("根据ID获取标签失败");
        }
    }

    /**
     * 获取标签列表
     *
     * @param current
     * @param size
     * @param tagName
     * @param isEnabled
     * @return
     */
    @GetMapping("/lists")
    public JsonData listTags(@RequestParam(defaultValue = "1") int current, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String tagName, @RequestParam(required = false) Boolean isEnabled) {
        try {
            PageResult<ProblemTagVO> lists = problemTagService.listTags(current, size, tagName, isEnabled);
            return JsonData.buildSuccess(lists);
        } catch (Exception e) {
            log.error("获取标签列表失败: {}", e.getMessage(), e);
            return JsonData.buildError("获取标签列表失败");
        }
    }

    /**
     * 获取所有启用的标签
     *
     * @return
     */
    @GetMapping("/enabled")
    public JsonData getAllEnabledTags() {
        try {
            List<ProblemTagVO> list = problemTagService.getAllEnabledTags();
            return JsonData.buildSuccess(list);
        } catch (Exception e) {
            log.error("获取启用的标签列表失败: {}", e.getMessage(), e);
            return JsonData.buildError("获取启用的标签列表失败");
        }
    }
}
