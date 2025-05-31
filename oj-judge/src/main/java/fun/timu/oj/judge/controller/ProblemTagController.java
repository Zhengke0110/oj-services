package fun.timu.oj.judge.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.enmus.ColorEnum;
import fun.timu.oj.common.enmus.TagCategoryEnum;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.common.utils.JsonData;
import fun.timu.oj.judge.controller.request.ProblemTagBatchRequest;
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

    /**
     * 根据使用次数范围查询标签
     *
     * @param minUsageCount 最小使用次数
     * @param maxUsageCount 最大使用次数
     * @param category      标签分类（可选）
     * @return 符合条件的标签列表
     */
    @GetMapping("/usage-range")
    public JsonData findByUsageCountRange(@RequestParam(required = false, defaultValue = "0") Long minUsageCount, @RequestParam(required = false, defaultValue = "100") Long maxUsageCount, @RequestParam(required = false) String category) {
        try {
            // 解析分类参数
            TagCategoryEnum categoryEnum = null;
            if (category != null && !category.isEmpty()) {
                try {
                    categoryEnum = TagCategoryEnum.valueOf(category.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("无效的标签分类");
                }
            }

            log.info("根据使用次数范围查询标签请求: 最小次数={}, 最大次数={}, 分类={}", minUsageCount, maxUsageCount, categoryEnum);
            List<ProblemTagVO> tags = problemTagService.findByUsageCountRange(minUsageCount, maxUsageCount, categoryEnum);
            return JsonData.buildSuccess(tags);
        } catch (Exception e) {
            log.error("根据使用次数范围查询标签失败: {}", e.getMessage(), e);
            return JsonData.buildError("根据使用次数范围查询标签失败");
        }
    }

    /**
     * 查询热门标签
     *
     * @param limit    限制返回的标签数量，默认10个
     * @param category 标签分类
     * @return 热门标签列表
     */
    @GetMapping("/popular")
    public JsonData findPopularTags(@RequestParam(defaultValue = "10") @Positive(message = "标签数量必须为正数") int limit, @RequestParam(required = false) String category) {
        try {
            TagCategoryEnum categoryEnum = null;
            if (category != null && !category.isEmpty()) {
                try {
                    categoryEnum = TagCategoryEnum.valueOf(category.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("无效的标签分类");
                }
            }

            log.info("查询热门标签请求: 限制数量={}, 分类={}", limit, categoryEnum);
            List<ProblemTagVO> tags = problemTagService.findPopularTags(limit, categoryEnum);
            return JsonData.buildSuccess(tags);
        } catch (Exception e) {
            log.error("查询热门标签失败: {}", e.getMessage(), e);
            return JsonData.buildError("查询热门标签失败");
        }
    }

    /**
     * 增加标签使用次数
     *
     * @param tagId 标签ID
     * @return 操作结果
     */
    @PutMapping("/{tagId}/increment-usage")
    public JsonData incrementUsageCount(@PathVariable Long tagId) {
        try {
            log.info("增加标签使用次数请求，tagId: {}", tagId);
            boolean result = problemTagService.incrementUsageCount(tagId);
            if (result) {
                return JsonData.buildSuccess("增加标签使用次数成功");
            } else {
                return JsonData.buildError("增加标签使用次数失败，标签可能不存在");
            }
        } catch (Exception e) {
            log.error("增加标签使用次数失败: {}", e.getMessage(), e);
            return JsonData.buildError("增加标签使用次数失败");
        }
    }

    /**
     * 减少标签使用次数
     *
     * @param tagId 标签ID
     * @return 操作结果
     */
    @PutMapping("/{tagId}/decrement-usage")
    public JsonData decrementUsageCount(@PathVariable Long tagId) {
        try {
            log.info("减少标签使用次数请求，tagId: {}", tagId);
            boolean result = problemTagService.decrementUsageCount(tagId);
            if (result) {
                return JsonData.buildSuccess("减少标签使用次数成功");
            } else {
                return JsonData.buildError("减少标签使用次数失败，标签可能不存在或使用次数已为0");
            }
        } catch (Exception e) {
            log.error("减少标签使用次数失败: {}", e.getMessage(), e);
            return JsonData.buildError("减少标签使用次数失败");
        }
    }


    /**
     * 批量增加标签使用次数
     *
     * @param request 包含tagIds和value的请求
     * @return 操作结果
     */
    @PutMapping("/batch/increment-usage")
    public JsonData batchIncrementUsageCount(@RequestBody ProblemTagBatchRequest request) {
        try {
            List<Long> tagIds = request.getTagIds();
            Integer increment = (request.getValue() != null && request.getValue() > 0) ? request.getValue() : 1;

            log.info("批量增加标签使用次数请求，标签数量: {}, 增加次数: {}", tagIds != null ? tagIds.size() : 0, increment);

            if (tagIds == null || tagIds.isEmpty()) {
                return JsonData.buildError("标签ID列表不能为空");
            }

            if (increment <= 0) {
                return JsonData.buildError("增加次数必须大于0");
            }

            int affectedRows = problemTagService.batchIncrementUsageCount(tagIds, increment);
            return JsonData.buildSuccess("批量增加标签使用次数成功:" + affectedRows);
        } catch (Exception e) {
            log.error("批量增加标签使用次数失败: {}", e.getMessage(), e);
            return JsonData.buildError("批量增加标签使用次数失败");
        }
    }

    /**
     * 批量减少标签使用次数
     *
     * @param request 包含tagIds和value的请求
     * @return 操作结果
     */
    @PutMapping("/batch/decrement-usage")
    public JsonData batchDecrementUsageCount(@RequestBody ProblemTagBatchRequest request) {
        try {
            List<Long> tagIds = request.getTagIds();

            Integer decrement = (request.getValue() != null && request.getValue() > 0) ? request.getValue() : 1;


            log.info("批量减少标签使用次数请求，标签数量: {}, 减少次数: {}", tagIds != null ? tagIds.size() : 0, decrement);

            if (tagIds == null || tagIds.isEmpty()) {
                return JsonData.buildError("标签ID列表不能为空");
            }

            if (decrement <= 0) {
                return JsonData.buildError("减少次数必须大于0");
            }

            int affectedRows = problemTagService.batchDecrementUsageCount(tagIds, decrement);
            return JsonData.buildSuccess("批量减少标签使用次数成功" + affectedRows);
        } catch (Exception e) {
            log.error("批量减少标签使用次数失败: {}", e.getMessage(), e);
            return JsonData.buildError("批量减少标签使用次数失败");
        }
    }

    /**
     * 批量更新标签状态
     *
     * @param request 包含tagIds和status的请求
     * @param request 请求体
     * @return 响应体
     */
    @PutMapping("/batch/update-status")
    public JsonData batchUpdateStatus(@RequestBody ProblemTagBatchRequest request) {
        try {

            List<Long> tagIds = request.getTagIds();
            Integer status = request.getStatus() != null ? request.getStatus().getCode() : 0;
            log.info("批量更新标签状态请求，标签数量: {}, 新状态: {}", tagIds != null ? tagIds.size() : 0, status);

            if (tagIds == null || tagIds.isEmpty()) {
                return JsonData.buildError("标签ID列表不能为空");
            }

            if (status == null || (status != 0 && status != 1)) {
                return JsonData.buildError("状态值无效，必须为0或1");
            }

            int affectedRows = problemTagService.batchUpdateStatus(tagIds, status);
            return JsonData.buildSuccess(affectedRows);
        } catch (Exception e) {
            log.error("批量更新标签状态失败: {}", e.getMessage(), e);
            return JsonData.buildError("批量更新标签状态失败");
        }
    }

    /**
     * 批量更新标签使用次数
     *
     * @param request
     * @return
     */
    @PutMapping("/batch/usage")
    public JsonData batchUpdateUsageCount(@RequestBody ProblemTagBatchRequest request) {
        try {
            List<Long> tagIds = request.getTagIds();
            String type = request.getType() != null ? request.getType().getValue() : "increment";
            Integer value = request.getValue() != null ? request.getValue() : 1;

            log.info("批量{}标签使用次数请求，标签数量: {}, 变更次数: {}", type.equals("increment") ? "增加" : "减少", tagIds != null ? tagIds.size() : 0, value);

            if (tagIds == null || tagIds.isEmpty()) {
                return JsonData.buildError("标签ID列表不能为空");
            }

            if (value <= 0) {
                return JsonData.buildError("更新次数必须大于0");
            }

            if (!type.equals("increment") && !type.equals("decrement")) {
                return JsonData.buildError("无效的操作类型，必须是increment或decrement");
            }

            int affectedRows = problemTagService.batchUpdateUsageCount(tagIds, value, type);
            return JsonData.buildSuccess(affectedRows);
        } catch (Exception e) {
            log.error("批量{}标签使用次数失败: {}", request.getType().equals("increment") ? "增加" : "减少", e.getMessage(), e);
            return JsonData.buildError("批量更新标签使用次数失败");
        }
    }
}
