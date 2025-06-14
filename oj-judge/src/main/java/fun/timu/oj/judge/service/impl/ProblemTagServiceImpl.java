package fun.timu.oj.judge.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.enmus.BizCodeEnum;
import fun.timu.oj.common.utils.JsonData;
import fun.timu.oj.judge.manager.ProblemTagRelationManager;
import fun.timu.oj.judge.model.Enums.TagCategoryEnum;
import fun.timu.oj.common.interceptor.LoginInterceptor;
import fun.timu.oj.common.model.LoginUser;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.controller.request.ProblemTagCreateRequest;
import fun.timu.oj.judge.controller.request.ProblemTagUpdateRequest;
import fun.timu.oj.judge.manager.ProblemTagManager;
import fun.timu.oj.judge.model.DO.ProblemTagDO;
import fun.timu.oj.judge.model.DTO.CategoryAggregateStatisticsDTO;
import fun.timu.oj.judge.model.DTO.TagUsageStatisticsDTO;
import fun.timu.oj.judge.model.VTO.CategoryAggregateStatisticsVTO;
import fun.timu.oj.judge.model.VO.ProblemTagVO;
import fun.timu.oj.judge.model.VTO.TagUsageStatisticsVTO;
import fun.timu.oj.judge.service.ProblemTagService;
import fun.timu.oj.judge.utils.ConvertToUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class ProblemTagServiceImpl implements ProblemTagService {
    private final ProblemTagManager problemTagManager;
    private final ProblemTagRelationManager relationManager;


    // ================== 基础CRUD操作 ==================

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
    public JsonData createTag(ProblemTagCreateRequest request) {
        try {
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                log.error("ProblemTagService--->createTag: 用户未登录");
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                log.error("ProblemTagService--->createTag: 用户没有创建标签的权限");
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_PERMISSION_DENIED);
            }

            // TODO: 调用Notification服务发送标签创建通知给管理员
            // TODO: 调用Cache服务清除相关标签缓存
            // TODO: 调用Statistics服务更新标签统计信息
            // 创建一个新的问题标签数据对象，并从请求对象中复制属性
            ProblemTagDO tagDO = new ProblemTagDO();
            BeanUtils.copyProperties(request, tagDO);

            // 设置标签为未删除状态，并初始化使用计数为0
            tagDO.setIsDeleted(0);
            tagDO.setUsageCount(0L);

            // 尝试保存标签到数据库
            int row = problemTagManager.save(tagDO);
            // 如果保存失败，抛出运行时异常
            if (row <= 0) {
                log.error("ProblemTagService--->createTag: 创建标签失败");
                throw new RuntimeException("创建标签失败");
            }
            return JsonData.buildSuccess(tagDO.getId());
        } catch (RuntimeException e) {
            log.error("ProblemTagService--->创建标签失败: {}", e.getMessage(), e);
            return JsonData.buildResult(BizCodeEnum.TAG_OPERATION_FAILED);
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
    public JsonData updateTag(ProblemTagUpdateRequest request) {
        try {
            // TODO: 调用Notification服务发送标签更新通知给相关用户
            // TODO: 调用Cache服务更新标签相关缓存
            // TODO: 调用Statistics服务更新标签统计信息
            // TODO: 调用ProblemTagRelation服务检查标签是否被题目使用，影响更新策略

            //  获取当前登录用户
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                log.error("ProblemTagService--->updateTag: 用户未登录");
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                log.error("ProblemTagService--->updateTag: 用户没有更新标签的权限");
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_PERMISSION_DENIED);
            }

            // 根据ID查找现有的标签
            ProblemTagDO existingTag = problemTagManager.findById(request.getId());
            if (existingTag == null || existingTag.getIsDeleted() == 1) {
                log.error("ProblemTagService--->updateTag: 标签不存在");
                throw new RuntimeException("标签不存在");
            }

            // 创建一个用于更新的标签对象，并设置更新时间
            ProblemTagDO updateTag = new ProblemTagDO();
            BeanUtils.copyProperties(request, updateTag);

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
                log.error("ProblemTagService--->updateTag: 更新标签失败");
                throw new RuntimeException("更新标签失败");
            }
            // 记录日志
            log.info("成功更新标签，ID: {}", request.getId());
            return JsonData.buildSuccess();
        } catch (RuntimeException e) {
            log.error("ProblemTagService--->updateTag: 更新标签失败: {}", e.getMessage(), e);
            return JsonData.buildResult(BizCodeEnum.TAG_OPERATION_FAILED);
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
    public JsonData deleteTag(Long id) {
        try {
            // TODO: 调用ProblemTagRelation服务检查标签是否被题目使用，禁止删除被使用的标签
            // TODO: 调用Notification服务发送标签删除通知给相关用户
            // TODO: 调用Cache服务清除标签相关缓存
            // TODO: 调用Statistics服务更新标签统计信息
            //  获取当前登录用户
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                log.error("ProblemTagService--->deleteTag: 用户未登录");
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                log.error("ProblemTagService--->deleteTag: 用户没有删除标签的权限");
                throw new RuntimeException("用户没有删除标签的权限");
            }
            // 尝试删除标签，如果删除失败则抛出运行时异常
            int row = problemTagManager.deleteById(id);
            if (row <= 0) {
                log.error("ProblemTagService--->deleteTag: 删除标签失败");
                throw new RuntimeException("删除标签失败");
            }
            return JsonData.buildSuccess();
        } catch (RuntimeException e) {
            // 捕获删除标签时的异常，并记录错误日志
            log.error("ProblemTagService--->删除标签失败: {}", e.getMessage(), e);
            return JsonData.buildResult(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }


    // ================== 查询操作 ==================

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
    public JsonData getTagById(Long id) {
        try {
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                log.error("ProblemTagService--->getTagById: 用户未登录");
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
            }

            // TODO: 调用Cache服务从缓存中获取标签信息，提升查询性能
            // 尝试通过ID查找问题标签实体
            ProblemTagDO tagDO = problemTagManager.findById(id);
            // 检查标签实体是否存在且未被删除
            if (tagDO == null || tagDO.getIsDeleted() == 1) {
                throw new RuntimeException("标签不存在或已被删除");
            }
            // 将找到的标签实体转换为视图对象并返回
            return JsonData.buildSuccess(ConvertToUtils.convertToVO(tagDO));
        } catch (Exception e) {
            // 记录获取标签失败的错误信息
            log.error("ProblemTagService--->获取标签失败: {}", e.getMessage(), e);
            return JsonData.buildResult(BizCodeEnum.TAG_NOT_EXIST);
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
    public JsonData listTags(int current, int size, String tagName, Boolean isEnabled, String tagColor) {
        try {
            // TODO: 调用Cache服务缓存分页查询结果，提升查询性能
            // 转换状态参数：true->1, false->0, null->null
            Integer status = null;
            if (isEnabled != null) status = isEnabled ? 1 : 0;

            // 调用manager层的分页查询方法，添加tagColor参数
            IPage<ProblemTagDO> tagPage = problemTagManager.findTagListWithPage(current, size, tagName, null, status, tagColor);

            // 转换DO对象为VO对象
            List<ProblemTagVO> voList = tagPage.getRecords().stream().map(ConvertToUtils::convertToVO).collect(Collectors.toList());

            // 创建自定义分页结果
            PageResult<ProblemTagVO> result = new PageResult<>(voList, tagPage.getTotal(), tagPage.getSize(), tagPage.getCurrent(), tagPage.getPages());

            // 记录日志信息
            log.info("ProblemTagService--->成功查询标签列表，当前页: {}, 每页大小: {}, 总数: {}", current, size, tagPage.getTotal());
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            // 记录异常信息
            log.error("ProblemTagService--->查询标签列表失败: {}", e.getMessage(), e);
            // 返回空的分页结果
            return JsonData.buildResult(BizCodeEnum.TAG_OPERATION_FAILED);
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
    public JsonData getAllEnabledTags() {
        try {
            // TODO: 调用Cache服务从缓存中获取启用的标签列表，避免频繁数据库查询
            // 查询所有启用的ProblemTagDO对象
            List<ProblemTagDO> tags = problemTagManager.findAllActive();
            // 将查询到的ProblemTagDO对象转换为ProblemTagVO对象列表
            List<ProblemTagVO> list = tags.stream().map(ConvertToUtils::convertToVO).collect(Collectors.toList());
            // 记录查询成功的日志信息
            log.info("ProblemTagService--->查询启用的Tags成功，数量: {}", list.size());
            return JsonData.buildSuccess(list);
        } catch (Exception e) {
            // 记录查询失败的错误日志
            log.error("ProblemTagService--->查询启用的Tags失败: {}", e.getMessage(), e);
            return JsonData.buildResult(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    // ================== 统计操作 ==================

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
    public JsonData getTagUsageStatistics(TagCategoryEnum category) {
        try {
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                log.error("ProblemTagService--->getTagUsageStatistics: 用户未登录");
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
            }
            // TODO: 调用Cache服务从缓存中获取统计数据，避免重复计算
            // TODO: 调用Statistics服务生成详细的使用统计报告
            // 调用 manager 层获取统计数据，注意这里返回的是 TagUsageStatisticsDTO 类型
            List<TagUsageStatisticsDTO> statisticsDTOs = problemTagManager.getTagUsageStatistics(category.toString());
            log.info("ProblemTagService--->获取标签使用统计信息，类别: {}", category);
            // 将 DTO 转换为 VO 对象
            List<TagUsageStatisticsVTO> collect = statisticsDTOs.stream().map(this::convertStatisticsToVO).collect(Collectors.toList());
            return JsonData.buildSuccess(collect);
        } catch (Exception e) {
            // 记录异常信息，便于问题追踪和定位
            log.error("ProblemTagService--->获取标签使用统计信息: {}", e.getMessage(), e);
            // 异常情况下返回 null，上层调用者需要处理可能的 null 值
            return JsonData.buildResult(BizCodeEnum.TAG_OPERATION_FAILED);
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
    public JsonData getCategoryAggregateStatistics() {
        try {
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                log.error("ProblemTagService--->getCategoryAggregateStatistics: 用户未登录");
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
            }
            // TODO: 调用Cache服务从缓存中获取聚合统计数据，避免重复计算
            // TODO: 调用Statistics服务生成详细的分类统计报告
            // 调用 manager 层获取原始聚合统计数据
            List<CategoryAggregateStatisticsDTO> statisticsDTOs = problemTagManager.getCategoryAggregateStatistics();
            log.info("ProblemTagService--->获取分类聚合统计信息成功，统计数量: {}", statisticsDTOs.size());
            // 将 DTO 转换为 VO 对象
            List<CategoryAggregateStatisticsVTO> collect = statisticsDTOs.stream().map(ConvertToUtils::convertToVO).collect(Collectors.toList());
            return JsonData.buildSuccess(collect);
        } catch (Exception e) {
            // 记录异常信息，便于问题追踪和定位
            log.error("ProblemTagService--->获取分类聚合统计信息失败: {}", e.getMessage(), e);
            // 异常情况下返回空列表，避免空指针异常
            return JsonData.buildResult(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }


    // ================== 高级查询操作 ==================

    /**
     * 根据使用次数范围查询标签
     * <p>
     * 此方法查询使用次数在指定范围内的问题标签
     *
     * @param minUsageCount 最小使用次数
     * @param maxUsageCount 最大使用次数
     * @param category      标签分类（可选）
     * @return 符合条件的标签列表
     */
    @Override
    public JsonData findByUsageCountRange(Long minUsageCount, Long maxUsageCount, TagCategoryEnum category) {
        try {
            // TODO: 调用Cache服务从缓存中获取使用次数范围查询结果
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                log.error("ProblemTagService--->findByUsageCountRange: 用户未登录");
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
            }
            // 转换分类参数
            String categoryStr = category != null ? category.toString() : null;

            // 调用manager层方法获取标签DO对象
            List<ProblemTagDO> tagDOs = problemTagManager.findByUsageCountRange(minUsageCount, maxUsageCount, categoryStr);

            // 将DO对象转换为VO对象
            List<ProblemTagVO> voList = tagDOs.stream().map(ConvertToUtils::convertToVO).collect(Collectors.toList());

            // 记录日志
            log.info("ProblemTagService--->查询使用次数范围[{}-{}]、分类[{}]的标签成功，数量: {}", minUsageCount, maxUsageCount, category, voList.size());
            return JsonData.buildSuccess(voList);
        } catch (Exception e) {
            // 记录错误日志
            log.error("ProblemTagService--->查询使用次数范围[{}-{}]、分类[{}]的标签失败: {}", minUsageCount, maxUsageCount, category, e.getMessage(), e);
            return JsonData.buildResult(BizCodeEnum.TAG_OPERATION_FAILED);
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
    public JsonData findPopularTags(int limit, TagCategoryEnum category) {
        try {
            // TODO: 调用Cache服务从缓存中获取热门标签列表，避免频繁计算
            // TODO: 调用Statistics服务记录热门标签查询统计
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                log.error("ProblemTagService--->findPopularTags: 用户未登录");
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN);
            }
            // 转换分类参数
            String categoryStr = category != null ? category.toString() : null;

            // 调用manager层方法获取热门标签
            List<ProblemTagDO> tagDOs = problemTagManager.findPopularTags(limit, categoryStr);

            // 将DO对象转换为VO对象
            List<ProblemTagVO> voList = tagDOs.stream().map(ConvertToUtils::convertToVO).collect(Collectors.toList());

            // 记录日志
            log.info("ProblemTagService--->查询热门标签成功，分类: {}, 限制数量: {}, 实际数量: {}", category, limit, voList.size());
            return JsonData.buildSuccess(voList);
        } catch (Exception e) {
            // 记录错误日志
            log.error("ProblemTagService--->查询热门标签失败，分类: {}, 限制数量: {}, 错误: {}", category, limit, e.getMessage(), e);
            return JsonData.buildResult(BizCodeEnum.TAG_OPERATION_FAILED);
        }
    }

    /**
     * 增加标签使用次数
     * <p>
     * 此方法通过调用manager层来增加指定标签的使用计数
     * TODO: 此接口不对外暴露使用，仅供内部调用
     *
     * @param tagId 标签ID
     * @return 操作是否成功
     */
    @Override
    public boolean incrementUsageCount(Long tagId) {
        try {
            // TODO: 调用Cache服务更新标签使用次数缓存
            // TODO: 调用Statistics服务更新标签使用统计
            // TODO: 调用Notification服务发送使用次数变更通知（可选）
            // 参数校验
            if (tagId == null || tagId <= 0) {
                throw new RuntimeException("无效的标签ID");
            }
            //  获取当前登录用户
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException(BizCodeEnum.ACCOUNT_UNLOGIN.getMessage());
            }

            // 调用manager层方法增加使用次数
            int result = problemTagManager.incrementUsageCount(tagId);

            // 记录日志
            if (result > 0) {
                log.info("ProblemTagService--->增加标签[{}]使用次数成功", tagId);
                return true;
            } else {
                log.warn("ProblemTagService--->增加标签[{}]使用次数失败，标签可能不存在", tagId);
                return false;
            }
        } catch (Exception e) {
            // 记录错误日志
            log.error("ProblemTagService--->增加标签[{}]使用次数异常: {}", tagId, e.getMessage(), e);
            throw new RuntimeException("增加标签使用次数失败", e);
        }
    }

    /**
     * 减少标签使用次数
     * <p>
     * 此方法通过调用manager层来减少指定标签的使用计数
     * TODO: 此接口不对外暴露使用，仅供内部调用
     *
     * @param tagId 标签ID
     * @return 操作是否成功
     */
    @Override
    public boolean decrementUsageCount(Long tagId) {
        try {
            // TODO: 调用Cache服务更新标签使用次数缓存
            // TODO: 调用Statistics服务更新标签使用统计
            // TODO: 调用Notification服务发送使用次数变更通知（可选）
            // 参数校验
            if (tagId == null || tagId <= 0) {
                throw new RuntimeException("无效的标签ID");
            }
            //  获取当前登录用户
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException(BizCodeEnum.ACCOUNT_UNLOGIN.getMessage());
            }


            // 调用manager层方法减少使用次数
            int result = problemTagManager.decrementUsageCount(tagId);

            // 记录日志
            if (result > 0) {
                log.info("ProblemTagService--->减少标签[{}]使用次数成功", tagId);
                return true;
            } else {
                log.warn("ProblemTagService--->减少标签[{}]使用次数失败，标签可能不存在或使用次数已为0", tagId);
                return false;
            }
        } catch (Exception e) {
            // 记录错误日志
            log.error("ProblemTagService--->减少标签[{}]使用次数异常: {}", tagId, e.getMessage(), e);
            throw new RuntimeException("减少标签使用次数失败", e);
        }
    }

    /**
     * 批量增加标签使用次数
     * <p>
     * 此方法通过调用manager层来批量增加多个标签的使用计数
     * TODO: 此接口不对外暴露使用，仅供内部调用
     *
     * @param tagIds    标签ID列表
     * @param increment 增加的次数
     * @return 受影响的记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchIncrementUsageCount(List<Long> tagIds, int increment) {
        try {
            // TODO: 调用MessageQueue服务将批量操作任务加入异步处理队列
            // TODO: 调用Notification服务发送批量操作通知给管理员
            // TODO: 调用Cache服务批量更新标签使用次数缓存
            // TODO: 调用Statistics服务批量更新标签统计信息
            // 参数校验
            if (tagIds == null || tagIds.isEmpty()) {
                log.warn("ProblemTagService--->批量增加标签使用次数失败：标签ID列表为空");
                return false;
            }
            if (increment <= 0) {
                log.warn("ProblemTagService--->批量增加标签使用次数失败：增加次数必须大于0");
                return false;
            }
            //  获取当前登录用户
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException(BizCodeEnum.ACCOUNT_UNLOGIN.getMessage());
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                throw new RuntimeException(BizCodeEnum.ACCOUNT_PERMISSION_DENIED.getMessage());
            }
            // 调用manager层方法批量增加使用次数（内部使用悲观锁）
            int affectedRows = problemTagManager.batchIncrementUsageCount(tagIds, increment);

            // 记录日志
            log.info("ProblemTagService--->批量增加标签使用次数成功，标签数量: {}, 增加次数: {}, 受影响行数: {}", tagIds.size(), increment, affectedRows);
            return affectedRows > 0;
        } catch (Exception e) {
            // 记录错误日志
            log.error("ProblemTagService--->批量增加标签使用次数异常: {}", e.getMessage(), e);
            throw new RuntimeException("批量增加标签使用次数失败", e);
        }
    }

    /**
     * 批量减少标签使用次数
     * <p>
     * 此方法通过调用manager层来批量减少多个标签的使用计数
     * TODO: 此接口不对外暴露使用，仅供内部调用
     *
     * @param tagIds    标签ID列表
     * @param decrement 减少的次数
     * @return 受影响的记录数
     */
    @Override
    @Transactional
    public boolean batchDecrementUsageCount(List<Long> tagIds, int decrement) {
        try {
            // TODO: 调用MessageQueue服务将批量操作任务加入异步处理队列
            // TODO: 调用Notification服务发送批量操作通知给管理员
            // TODO: 调用Cache服务批量更新标签使用次数缓存
            // TODO: 调用Statistics服务批量更新标签统计信息
            // 参数校验
            if (tagIds == null || tagIds.isEmpty()) {
                log.warn("ProblemTagService--->批量减少标签使用次数失败：标签ID列表为空");
                return false;
            }
            if (decrement <= 0) {
                log.warn("ProblemTagService--->批量减少标签使用次数失败：减少次数必须大于0");
                return false;
            }

            //  获取当前登录用户
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException(BizCodeEnum.ACCOUNT_UNLOGIN.getMessage());
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                throw new RuntimeException(BizCodeEnum.ACCOUNT_PERMISSION_DENIED.getMessage());
            }

            // 调用manager层方法批量减少使用次数
            int affectedRows = problemTagManager.batchDecrementUsageCount(tagIds, decrement);
            // 记录日志
            log.info("ProblemTagService--->批量减少标签使用次数成功，标签数量: {}, 减少次数: {}, 受影响行数: {}", tagIds.size(), decrement, affectedRows);
            return affectedRows > 0;
        } catch (Exception e) {
            // 记录错误日志
            log.error("ProblemTagService--->批量减少标签使用次数异常: {}", e.getMessage(), e);
            throw new RuntimeException("批量减少标签使用次数失败", e); // 抛出异常以触发事务回滚
        }
    }

    /**
     * 批量更新标签状态
     * <p>
     * 此方法通过调用manager层来批量更新多个标签的状态
     * TODO: 此接口不对外暴露使用，仅供内部调用
     *
     * @param tagIds 标签ID列表
     * @param status 新的状态值（1-启用，0-禁用）
     * @return 受影响的记录数
     */
    @Override
    @Transactional
    public boolean batchUpdateStatus(List<Long> tagIds, Integer status) {
        try {
            // TODO: 调用Account服务验证用户身份和管理员权限
            // TODO: 调用MessageQueue服务将批量状态更新任务加入异步处理队列
            // TODO: 调用Notification服务发送批量状态更新通知给相关用户
            // TODO: 调用Cache服务批量更新标签状态缓存
            // TODO: 调用Statistics服务更新标签状态统计信息
            // TODO: 调用ProblemTagRelation服务检查标签关联，影响状态更新策略
            // 参数校验
            if (tagIds == null || tagIds.isEmpty()) {
                log.warn("ProblemTagService--->批量更新标签状态失败：标签ID列表为空");
                return false;
            }
            // 严格校验状态值，必须是1或0
            if (status == null || (status != 0 && status != 1)) {
                log.warn("ProblemTagService--->批量更新标签状态失败：状态值无效，必须为0或1，当前值: {}", status);
                return false;
            }

            //  获取当前登录用户
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException(BizCodeEnum.ACCOUNT_UNLOGIN.getMessage());
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                throw new RuntimeException(BizCodeEnum.ACCOUNT_PERMISSION_DENIED.getMessage());
            }
            // 调用manager层方法批量更新标签状态
            int affectedRows = problemTagManager.batchUpdateStatus(tagIds, status);

            // 记录日志
            log.info("ProblemTagService--->批量更新标签状态成功，标签数量: {}, 新状态: {}, 受影响行数: {}", tagIds.size(), status, affectedRows);
            return affectedRows > 0;
        } catch (Exception e) {
            // 记录错误日志
            log.error("ProblemTagService--->批量更新标签状态异常: {}", e.getMessage(), e);
            throw new RuntimeException("批量更新标签状态失败", e); // 抛出异常以触发事务回滚
        }
    }

    /**
     * 批量更新标签使用次数
     * <p>
     * 此方法根据操作类型，统一处理标签使用次数的增加或减少
     * TODO: 此接口不对外暴露使用，仅供内部调用
     *
     * @param tagIds 标签ID列表
     * @param value  更新值（必须为正数）
     * @param type   操作类型（"increment"或"decrement"）
     * @return 受影响的记录数
     */
    @Override
    @Transactional
    public boolean batchUpdateUsageCount(List<Long> tagIds, int value, String type) {
        try {
            // TODO: 调用Account服务验证用户身份和管理员权限
            // TODO: 调用MessageQueue服务将批量操作任务加入异步处理队列
            // TODO: 调用Notification服务发送批量操作通知给管理员
            // TODO: 调用Cache服务批量更新标签使用次数缓存
            // TODO: 调用Statistics服务批量更新标签统计信息
            // 参数校验
            if (tagIds == null || tagIds.isEmpty()) {
                log.warn("ProblemTagService--->批量更新标签使用次数失败：标签ID列表为空");
                return false;
            }

            if (value <= 0) {
                log.warn("ProblemTagService--->批量更新标签使用次数失败：更新值必须大于0，当前值: {}", value);
                return false;
            }

            // 验证操作类型
            if (!"increment".equals(type) && !"decrement".equals(type)) {
                log.warn("ProblemTagService--->批量更新标签使用次数失败：无效的操作类型，当前类型: {}", type);
                return false;
            }

            //  获取当前登录用户
            LoginUser loginUser = LoginInterceptor.threadLocal.get();
            if (loginUser == null) {
                throw new RuntimeException(BizCodeEnum.ACCOUNT_UNLOGIN.getMessage());
            }
            if (loginUser.getAuth() == null || !loginUser.getAuth().equals("ADMIN")) {
                throw new RuntimeException(BizCodeEnum.ACCOUNT_PERMISSION_DENIED.getMessage());
            }

            int affectedRows;
            // 根据操作类型调用不同的manager方法
            if ("increment".equals(type)) {
                affectedRows = problemTagManager.batchIncrementUsageCount(tagIds, value);
                log.info("ProblemTagService--->批量增加标签使用次数成功，标签数量: {}, 增加次数: {}, 受影响行数: {}", tagIds.size(), value, affectedRows);
            } else {
                affectedRows = problemTagManager.batchDecrementUsageCount(tagIds, value);
                log.info("ProblemTagService--->批量减少标签使用次数成功，标签数量: {}, 减少次数: {}, 受影响行数: {}", tagIds.size(), value, affectedRows);
            }

            return affectedRows > 0;
        } catch (Exception e) {
            // 记录错误日志
            log.error("ProblemTagService--->批量更新标签使用次数异常: {}", e.getMessage(), e);
            throw new RuntimeException("批量增加标签使用次数失败", e); // 抛出异常以触发事务回滚
        }
    }

    /**
     * 根据问题ID获取标签列表
     *
     * @param problemId 问题ID
     * @return 标签列表
     */
    @Override
    public List<ProblemTagVO> getTagListByProblemId(Long problemId) {
        if (problemId == null || problemId <= 0) throw new RuntimeException("问题ID无效");
        List<Long> tagIds = relationManager.getTagIdsByProblemId(problemId);
        if (tagIds == null || tagIds.isEmpty()) {
            log.warn("ProblemTagService--->getTagListByProblemId: 问题ID {} 没有关联的标签", problemId);
            return List.of(); // 返回空列表而不是null，避免空指针异常
        }
        List<ProblemTagDO> tagDOList = problemTagManager.batchFindByIds(tagIds);
        List<ProblemTagVO> voList = tagDOList.stream().map(ConvertToUtils::convertToVO).collect(Collectors.toList());
        if (voList.isEmpty()) {
            log.warn("ProblemTagService--->getTagListByProblemId: 问题ID {} 没有关联的标签", problemId);
            return List.of();
        }
        return voList;
    }

    /**
     * 根据问题ID列表批量获取标签列表
     *
     * @param problemIds 问题ID列表
     * @return
     */
    @Override
    public Map<Long, List<ProblemTagVO>> getTagListByProblemIds(List<Long> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            log.warn("ProblemTagService--->getTagListByProblemIds: 问题ID列表为空");
            return Map.of();
        }

        Map<Long, List<ProblemTagDO>> tagListByProblemIds = relationManager.getTagListByProblemIds(problemIds);
        log.debug("ProblemTagService--->getTagListByProblemIds: 获取{}个问题的标签信息成功", tagListByProblemIds.size());

        // 创建结果Map，将DO对象转换为VO对象
        Map<Long, List<ProblemTagVO>> result = new HashMap<>();

        // 遍历每个问题ID的标签列表进行转换
        for (Map.Entry<Long, List<ProblemTagDO>> entry : tagListByProblemIds.entrySet()) {
            Long problemId = entry.getKey();
            List<ProblemTagDO> tagDOList = entry.getValue();

            // 将DO对象列表转换为VO对象列表
            List<ProblemTagVO> tagVOList = tagDOList.stream().map(ConvertToUtils::convertToVO).collect(Collectors.toList());
            result.put(problemId, tagVOList);
        }

        log.info("ProblemTagService--->getTagListByProblemIds: 成功获取{}个问题的标签信息", result.size());

        return result;
    }

    /**
     * 将标签使用统计DTO转换为VO
     *
     * @param dto 标签使用统计DTO
     * @return 标签使用统计VO
     */
    private TagUsageStatisticsVTO convertStatisticsToVO(TagUsageStatisticsDTO dto) {
        TagUsageStatisticsVTO vo = new TagUsageStatisticsVTO();
        BeanUtils.copyProperties(dto, vo);
        return vo;
    }


}
