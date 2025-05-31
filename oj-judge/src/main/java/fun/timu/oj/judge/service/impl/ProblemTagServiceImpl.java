package fun.timu.oj.judge.service.impl;

import fun.timu.oj.judge.controller.request.ProblemTagCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemTagUpdateRequest;
import fun.timu.oj.judge.manager.ProblemTagManager;
import fun.timu.oj.judge.model.DO.ProblemTagDO;
import fun.timu.oj.judge.service.ProblemTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

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


    @Override
    @Transactional
    public boolean updateTag(ProblemTagUpdateRequest request) {
        try {
            ProblemTagDO existingTag = problemTagManager.findById(request.getId());
            if (existingTag == null || existingTag.getIsDeleted() == 1) {
                throw new RuntimeException("标签不存在");
            }

            ProblemTagDO updateTag = new ProblemTagDO();
            BeanUtils.copyProperties(request, updateTag);
            updateTag.setUpdatedAt(new Date());

            int row = problemTagManager.updateById(updateTag);
            if (row <= 0) {
                throw new RuntimeException("更新标签失败");
            }
            log.info("成功更新标签，ID: {}", request.getId());
            return true;
        } catch (RuntimeException e) {
            log.error("ProblemTagService--->更新标签失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
