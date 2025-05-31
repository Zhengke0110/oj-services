package fun.timu.oj.judge.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.common.enmus.TagCategoryEnum;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.controller.request.ProblemTagCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemTagUpdateRequest;
import fun.timu.oj.judge.manager.ProblemTagManager;
import fun.timu.oj.judge.model.DO.ProblemTagDO;
import fun.timu.oj.judge.model.DTO.CategoryAggregateStatisticsDTO;
import fun.timu.oj.judge.model.DTO.TagUsageStatisticsDTO;
import fun.timu.oj.judge.model.VO.CategoryAggregateStatisticsVO;
import fun.timu.oj.judge.model.VO.ProblemTagVO;
import fun.timu.oj.judge.model.VO.TagUsageStatisticsVO;
import fun.timu.oj.judge.service.ProblemTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProblemTagServiceImpl implements ProblemTagService {
    private final ProblemTagManager problemTagManager;

    public ProblemTagServiceImpl(ProblemTagManager problemTagManager) {
        this.problemTagManager = problemTagManager;
    }

    /**
     * 创建问题标签
     * <p>
     * 此方法用于将一个新标签添加到数据库中它首先将请求对象的属性复制到一个标签数据对象中，
     * 然后设置必要的元数据，如创建时间、更新时间和删除标志最后，调用问题标签管理器保存到数据库
     * 如果标签创建失败，将抛出运行时异常；创建成功，则记录日志并返回标签ID
     *
     * @param request 包含要创建的标签信息的请求对象
     * @return 创建的标签的ID
     * @throws RuntimeException 如果标签创建失败
     */
    @Override
    @Transactional
    public Long createTag(ProblemTagCreateRequest request) {
        try {
            // 创建一个新的问题标签数据对象，并从请求对象中复制属性
            ProblemTagDO tagDO = new ProblemTagDO();
            BeanUtils.copyProperties(request, tagDO);

            // 设置标签的创建时间和更新时间为当前日期
            tagDO.setCreatedAt(new Date());
            tagDO.setUpdatedAt(new Date());

            // 设置标签为未删除状态，并初始化使用计数为0
            tagDO.setIsDeleted(0);
            tagDO.setUsageCount(0L);

            // 尝试保存标签到数据库
            int row = problemTagManager.save(tagDO);

            // 如果保存失败，抛出运行时异常
            if (row <= 0) {
                throw new RuntimeException("创建标签失败");
            }

            // 记录成功创建标签的日志，并返回标签ID
            log.info("成功创建标签，ID: {}", tagDO.getId());
            return tagDO.getId();
        } catch (RuntimeException e) {
            log.error("ProblemTagService--->创建标签失败: {}", e.getMessage(), e);
            return -1L;
        }
    }


    /**
     * 更新问题标签
     * <p>
     * 此方法用于根据提供的问题标签更新请求来更新数据库中的标签信息
     * 它首先检查标签是否存在，然后根据请求中的信息更新标签的属性
     * 更新包括标签的名称、颜色、状态和类别
     *
     * @param request 包含要更新的标签信息的请求对象
     * @return 如果更新成功，则返回true；否则返回false
     */
    @Override
    @Transactional
    public boolean updateTag(ProblemTagUpdateRequest request) {
        try {
            // 根据ID查找现有的标签
            ProblemTagDO existingTag = problemTagManager.findById(request.getId());
            if (existingTag == null || existingTag.getIsDeleted() == 1) {
                // 如果标签不存在或已被删除，则抛出异常
                throw new RuntimeException("标签不存在");
            }

            // 创建一个用于更新的标签对象，并设置更新时间
            ProblemTagDO updateTag = new ProblemTagDO();
            BeanUtils.copyProperties(request, updateTag);
            updateTag.setUpdatedAt(new Date());

            // 设置标签的状态
            if (request.getIsEnabled()) updateTag.setStatus(1);
            else updateTag.setStatus(0);

            // 设置标签的颜色，如果提供了颜色信息
            if (request.getColor() != null) updateTag.setTagColor(request.getColor());

            // 设置标签的类别，如果提供了类别信息
            if (request.getCategory() != null) updateTag.setCategory(request.getCategory().toString());

            // 执行更新操作
            int row = problemTagManager.updateById(updateTag);
            if (row <= 0) {
                // 如果更新失败，则抛出异常
                throw new RuntimeException("更新标签失败");
            }
            // 记录日志
            log.info("成功更新标签，ID: {}", request.getId());
            return true;
        } catch (RuntimeException e) {
            // 记录错误日志
            log.error("ProblemTagService--->更新标签失败: {}", e.getMessage(), e);
            return false;
        }
    }


    /**
     * 删除指定ID的标签
     *
     * @param id 需要删除的标签的ID
     * @return 如果删除成功返回true，否则返回false
     */
    @Override
    @Transactional
    public boolean deleteTag(Long id) {
        try {
            // 尝试删除标签，如果删除失败则抛出运行时异常
            int row = problemTagManager.deleteById(id);
            if (row <= 0) throw new RuntimeException("删除标签失败");
            return true;
        } catch (RuntimeException e) {
            // 捕获删除标签时的异常，并记录错误日志
            log.error("ProblemTagService--->删除标签失败: {}", e.getMessage(), e);
            return false;
        }
    }


    /**
     * 根据标签ID获取标签详细信息
     * <p>
     * 此方法首先尝试通过problemTagManager findById方法从数据库中查找与给定ID关联的标签对象
     * 如果找到的标签对象为空或已被标记为删除，则抛出运行时异常
     * 否则，将找到的标签对象转换为视图对象(VO)并返回
     *
     * @param id 标签的唯一标识符
     * @return 如果找到且未删除的标签存在，则返回标签的视图对象，否则返回null
     */
    @Override
    public ProblemTagVO getTagById(Long id) {
        try {
            // 尝试通过ID查找问题标签实体
            ProblemTagDO tagDO = problemTagManager.findById(id);
            // 检查标签实体是否存在且未被删除
            if (tagDO == null || tagDO.getIsDeleted() == 1) {
                throw new RuntimeException("标签不存在或已被删除");
            }
            // 将找到的标签实体转换为视图对象并返回
            return convertToVO(tagDO);
        } catch (Exception e) {
            // 记录获取标签失败的错误信息
            log.error("ProblemTagService--->获取标签失败: {}", e.getMessage(), e);
            return null;
        }
    }


    /**
     * 根据多个条件分页查询问题标签列表
     *
     * @param current   当前页码
     * @param size      每页记录数
     * @param tagName   标签名称，用于模糊查询
     * @param isEnabled 标签启用状态，true表示启用，false表示禁用，null表示不作为筛选条件
     * @param tagColor  标签颜色
     * @return 返回包含标签信息的分页结果
     */
    @Override
    public PageResult<ProblemTagVO> listTags(int current, int size, String tagName, Boolean isEnabled, String tagColor) {
        try {
            // 转换状态参数：true->1, false->0, null->null
            Integer status = null;
            if (isEnabled != null) status = isEnabled ? 1 : 0;

            // 调用manager层的分页查询方法，添加tagColor参数
            IPage<ProblemTagDO> tagPage = problemTagManager.findTagListWithPage(current, size, tagName, null, status, tagColor);

            // 转换DO对象为VO对象
            List<ProblemTagVO> voList = tagPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

            // 创建自定义分页结果
            PageResult<ProblemTagVO> result = new PageResult<>(voList, tagPage.getTotal(), tagPage.getSize(), tagPage.getCurrent(), tagPage.getPages());

            // 记录日志信息
            log.info("ProblemTagService--->成功查询标签列表，当前页: {}, 每页大小: {}, 总数: {}", current, size, tagPage.getTotal());
            return result;
        } catch (Exception e) {
            // 记录异常信息
            log.error("ProblemTagService--->查询标签列表失败: {}", e.getMessage(), e);
            // 返回空的分页结果
            return new PageResult<>(List.of(), 0, size, current, 0);
        }
    }


    /**
     * 获取所有启用的问题标签
     * <p>
     * 此方法通过调用问题标签管理器的findAllActive方法来获取所有启用的ProblemTagDO对象，
     * 然后将这些对象转换为ProblemTagVO对象，以便于在更高层次的业务逻辑中使用
     * 如果在查询过程中发生异常，将记录错误日志并返回null
     *
     * @return 包含所有启用问题标签的ProblemTagVO对象列表，如果查询失败则返回null
     */
    @Override
    public List<ProblemTagVO> getAllEnabledTags() {
        try {
            // 查询所有启用的ProblemTagDO对象
            List<ProblemTagDO> tags = problemTagManager.findAllActive();
            // 将查询到的ProblemTagDO对象转换为ProblemTagVO对象列表
            List<ProblemTagVO> list = tags.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            // 记录查询成功的日志信息
            log.info("ProblemTagService--->查询启用的Tags成功，数量: {}", list.size());
            return list;
        } catch (Exception e) {
            // 记录查询失败的错误日志
            log.error("ProblemTagService--->查询启用的Tags失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取指定标签类别的使用统计信息
     *
     * @param category 标签类别枚举，用于指定要统计的标签类别
     * @return 返回一个包含标签使用统计信息的列表，如果发生异常则返回null
     * <p>
     * 此方法首先调用 manager 层的相应方法获取标签使用统计的原始数据（DTO 类型），
     * 然后将这些数据转换为适合前端展示的 VO 类型列表
     * 如果在处理过程中遇到任何异常，都会记录错误日志并返回 null
     */
    @Override
    public List<TagUsageStatisticsVO> getTagUsageStatistics(TagCategoryEnum category) {
        try {
            // 调用 manager 层获取统计数据，注意这里返回的是 TagUsageStatisticsDTO 类型
            List<TagUsageStatisticsDTO> statisticsDTOs = problemTagManager.getTagUsageStatistics(category.toString());

            // 将 DTO 转换为 VO 对象
            return statisticsDTOs.stream()
                    .map(this::convertStatisticsToVO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // 记录异常信息，便于问题追踪和定位
            log.error("ProblemTagService--->获取标签使用统计信息: {}", e.getMessage(), e);
            // 异常情况下返回 null，上层调用者需要处理可能的 null 值
            return Collections.emptyList();
        }
    }

    /**
     * 获取所有分类的标签使用聚合统计信息
     * <p>
     * 此方法通过调用 manager 层获取各分类标签的聚合统计数据，然后转换为前端可用的 VO 对象
     * 统计信息包括每个分类的标签总数、使用次数、活跃标签数等指标
     *
     * @return 分类聚合统计信息的 VO 对象列表
     */
    @Override
    public List<CategoryAggregateStatisticsVO> getCategoryAggregateStatistics() {
        try {
            // 调用 manager 层获取原始聚合统计数据
            List<CategoryAggregateStatisticsDTO> statisticsDTOs = problemTagManager.getCategoryAggregateStatistics();

            // 将 DTO 转换为 VO 对象
            return statisticsDTOs.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // 记录异常信息，便于问题追踪和定位
            log.error("ProblemTagService--->获取分类聚合统计信息失败: {}", e.getMessage(), e);
            // 异常情况下返回空列表，避免空指针异常
            return Collections.emptyList();
        }
    }


    /**
     * 根据使用次数范围查询标签
     * <p>
     * 此方法查询使用次数在指定范围内的问题标签
     *
     * @param minUsageCount 最小使用次数
     * @param maxUsageCount 最大使用次数
     * @param category 标签分类（可选）
     * @return 符合条件的标签列表
     */
    @Override
    public List<ProblemTagVO> findByUsageCountRange(Long minUsageCount, Long maxUsageCount, TagCategoryEnum category) {
        try {
            // 转换分类参数
            String categoryStr = category != null ? category.toString() : null;

            // 调用manager层方法获取标签DO对象
            List<ProblemTagDO> tagDOs = problemTagManager.findByUsageCountRange(minUsageCount, maxUsageCount, categoryStr);

            // 将DO对象转换为VO对象
            List<ProblemTagVO> voList = tagDOs.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 记录日志
            log.info("ProblemTagService--->查询使用次数范围[{}-{}]、分类[{}]的标签成功，数量: {}",
                    minUsageCount, maxUsageCount, category, voList.size());
            return voList;
        } catch (Exception e) {
            // 记录错误日志
            log.error("ProblemTagService--->查询使用次数范围[{}-{}]、分类[{}]的标签失败: {}",
                    minUsageCount, maxUsageCount, category, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询热门标签
     * <p>
     * 此方法根据标签使用次数查询指定分类的热门标签
     *
     * @param limit    限制返回的标签数量
     * @param category 标签分类
     * @return 热门标签列表
     */
    @Override
    public List<ProblemTagVO> findPopularTags(int limit, TagCategoryEnum category) {
        try {
            // 转换分类参数
            String categoryStr = category != null ? category.toString() : null;

            // 调用manager层方法获取热门标签
            List<ProblemTagDO> tagDOs = problemTagManager.findPopularTags(limit, categoryStr);

            // 将DO对象转换为VO对象
            List<ProblemTagVO> voList = tagDOs.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 记录日志
            log.info("ProblemTagService--->查询热门标签成功，分类: {}, 限制数量: {}, 实际数量: {}",
                    category, limit, voList.size());
            return voList;
        } catch (Exception e) {
            // 记录错误日志
            log.error("ProblemTagService--->查询热门标签失败，分类: {}, 限制数量: {}, 错误: {}",
                    category, limit, e.getMessage(), e);
            return Collections.emptyList();
        }
    }


    /**
     * 将标签使用统计DTO转换为VO
     *
     * @param dto 标签使用统计DTO
     * @return 标签使用统计VO
     */
    private TagUsageStatisticsVO convertStatisticsToVO(TagUsageStatisticsDTO dto) {
        TagUsageStatisticsVO vo = new TagUsageStatisticsVO();
        BeanUtils.copyProperties(dto, vo);
        return vo;
    }

    /**
     * 将 CategoryAggregateStatisticsDTO 转换为 CategoryAggregateStatisticsVO
     *
     * @param dto CategoryAggregateStatisticsDTO 对象
     * @return 转换后的 CategoryAggregateStatisticsVO 对象
     */
    private CategoryAggregateStatisticsVO convertToVO(CategoryAggregateStatisticsDTO dto) {
        if (dto == null) {
            return null;
        }
        // 使用 BeanUtils 进行属性拷贝
        CategoryAggregateStatisticsVO vo = new CategoryAggregateStatisticsVO();
        BeanUtils.copyProperties(dto, vo);

        // 设置额外的字段
        vo.setCategoryDisplayName(getCategoryDisplayName(dto.getCategory()));
        if (dto.getTotalTags() != null && dto.getTotalTags() > 0) {
            vo.setActiveRate(dto.getActiveTags() * 1.0 / dto.getTotalTags());
            vo.setAverageUsage(dto.getStoredUsageCount() * 1.0 / dto.getTotalTags());
        } else {
            vo.setActiveRate(0.0);
            vo.setAverageUsage(0.0);
        }
        return vo;
    }

    /**
     * 根据分类枚举值获取分类显示名称
     *
     * @param category 分类枚举值
     * @return 分类显示名称
     */
    private String getCategoryDisplayName(String category) {
        try {
            return TagCategoryEnum.valueOf(category).name();
        } catch (IllegalArgumentException e) {
            return "ALGORITHM";
        }
    }


    /**
     * 将问题标签数据对象转换为视图对象
     *
     * @param tagDO 问题标签数据对象，包含标签的相关数据
     * @return 返回一个视图对象，包含与数据对象相同的信息
     */
    private ProblemTagVO convertToVO(ProblemTagDO tagDO) {
        // 创建一个视图对象实例
        ProblemTagVO tagVO = new ProblemTagVO();
        // 将数据对象的属性值复制到视图对象中
        BeanUtils.copyProperties(tagDO, tagVO);
        // 返回填充好的视图对象
        return tagVO;
    }

}
