package fun.timu.oj.judge.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.enmus.ColorEnum;
import fun.timu.oj.common.enmus.TagCategoryEnum;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.common.utils.JsonData;
import fun.timu.oj.judge.controller.request.ProblemTagCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemTagListRequest;
import fun.timu.oj.judge.controller.request.ProblemTagUpdateRequest;
import fun.timu.oj.judge.model.VO.CategoryAggregateStatisticsVO;
import fun.timu.oj.judge.model.VO.ProblemTagVO;
import fun.timu.oj.judge.model.VO.TagUsageStatisticsVO;
import fun.timu.oj.judge.service.ProblemTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
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
            if (request.getColor() == null) throw new RuntimeException("无效的标签颜色");
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
            if (request.getColor() == null) throw new RuntimeException("无效的标签颜色");
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
     * @param request 标签列表查询请求
     * @return 标签列表结果
     */
    @GetMapping("/lists")
    public JsonData listTags(@Valid @RequestBody ProblemTagListRequest request) {
        try {
            log.info("查询标签列表请求: {}", request);
            PageResult<ProblemTagVO> lists = problemTagService.listTags(request.getCurrent(), request.getSize(), request.getTagName(), request.getIsEnabled(), request.getColor());
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

    /**
     * 获取所有标签颜色
     *
     * @return
     */
    @GetMapping("/colors")
    public JsonData getAllColors() {
        try {
            List<Map<String, String>> colorList = new ArrayList<>();
            for (ColorEnum color : ColorEnum.values()) {
                Map<String, String> colorMap = new HashMap<>();
                colorMap.put("colorName", color.getColorName());
                colorMap.put("colorValue", color.getColorCode());
                colorList.add(colorMap);
            }
            return JsonData.buildSuccess(colorList);
        } catch (Exception e) {
            log.error("获取颜色列表失败: {}", e.getMessage(), e);
            return JsonData.buildError("获取颜色列表失败");
        }
    }

    /**
     * 获取指定分类的标签使用统计
     *
     * @param category 标签分类
     * @return 标签使用统计信息
     */
    @GetMapping("/statistics/{category}")
    public JsonData getTagUsageStatistics(@PathVariable String category) {
        try {
            log.info("获取标签使用统计请求, 分类: {}", category);
            TagCategoryEnum categoryEnum;
            try {
                categoryEnum = TagCategoryEnum.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("无效的标签分类");
            }
            List<TagUsageStatisticsVO> statistics = problemTagService.getTagUsageStatistics(categoryEnum);
            return JsonData.buildSuccess(statistics);
        } catch (Exception e) {
            log.error("获取标签使用统计失败: {}", e.getMessage(), e);
            return JsonData.buildError("获取标签使用统计失败");
        }
    }

    /**
     * 获取所有分类的标签使用聚合统计信息
     *
     * @return 分类聚合统计信息
     */
    @GetMapping("/statistics/category/aggregate")
    public JsonData getCategoryAggregateStatistics() {
        try {
            log.info("获取分类聚合统计请求");
            List<CategoryAggregateStatisticsVO> statistics = problemTagService.getCategoryAggregateStatistics();
            return JsonData.buildSuccess(statistics);
        } catch (Exception e) {
            log.error("获取分类聚合统计失败: {}", e.getMessage(), e);
            return JsonData.buildError("获取分类聚合统计失败");
        }
    }
}
