package fun.timu.oj.judge.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.controller.request.ProblemTagCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemTagUpdateRequest;
import fun.timu.oj.judge.manager.ProblemTagManager;
import fun.timu.oj.judge.model.DO.ProblemTagDO;
import fun.timu.oj.judge.model.VO.ProblemTagVO;
import fun.timu.oj.judge.service.ProblemTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Override
    public PageResult<ProblemTagVO> listTags(int current, int size, String tagName, Boolean isEnabled) {
        try {
            // 转换状态参数：true->1, false->0, null->null
            Integer status = null;
            if (isEnabled != null) {
                status = isEnabled ? 1 : 0;
            }

            // 调用manager层的分页查询方法
            IPage<ProblemTagDO> tagPage = problemTagManager.findTagListWithPage(current, size, tagName, null, status);

            // 转换DO对象为VO对象
            List<ProblemTagVO> voList = tagPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

            // 创建自定义分页结果
            PageResult<ProblemTagVO> result = new PageResult<>(voList, tagPage.getTotal(), tagPage.getSize(), tagPage.getCurrent(), tagPage.getPages());

            log.info("成功查询标签列表，当前页: {}, 每页大小: {}, 总数: {}", current, size, tagPage.getTotal());
            return result;

        } catch (Exception e) {
            log.error("ProblemTagService--->查询标签列表失败: {}", e.getMessage(), e);
            // 返回空的分页结果
            return new PageResult<>(List.of(), 0, size, current, 0);
        }
    }


    private ProblemTagVO convertToVO(ProblemTagDO tagDO) {
        ProblemTagVO tagVO = new ProblemTagVO();
        BeanUtils.copyProperties(tagDO, tagVO);
        return tagVO;
    }
}
