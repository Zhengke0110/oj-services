package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.judge.manager.ProblemTagManager;
import fun.timu.oj.judge.mapper.ProblemTagMapper;
import fun.timu.oj.judge.model.DO.ProblemTagDO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProblemTagManagerImpl implements ProblemTagManager {
    private final ProblemTagMapper problemTagMapper;

    public ProblemTagManagerImpl(ProblemTagMapper problemTagMapper) {
        this.problemTagMapper = problemTagMapper;
    }

    /**
     * 根据问题标签的ID查找问题标签实体
     *
     * @param id 问题标签的唯一标识符
     * @return 返回找到的问题标签实体，如果未找到则返回null
     */
    @Override
    public ProblemTagDO findById(Long id) {
        return problemTagMapper.selectById(id);
    }


    /**
     * 查询所有活跃的问题标签
     * <p>
     * 本方法通过构造一个Lambda查询包装器来查询数据库中所有状态为激活且未被删除的��题标签
     * 查询结果会根据标签的使用次数降序排序，然后根据标签名称升序排序
     *
     * @return 返回一个包含所有符合条件的问题标签的列表
     */
    @Override
    public List<ProblemTagDO> findAllActive() {
        // 创建Lambda查询包装器实例
        LambdaQueryWrapper<ProblemTagDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件：状态为激活（status=1）且未被删除（isDeleted=false）
        queryWrapper.eq(ProblemTagDO::getStatus, 1).eq(ProblemTagDO::getIsDeleted, false)
                // 设置查询结果的排序方式：首先按使用次数降序排序
                .orderByDesc(ProblemTagDO::getUsageCount)
                // 然后按标签名称升序排序
                .orderByAsc(ProblemTagDO::getTagName);
        // 执行查询并返回结果
        return problemTagMapper.selectList(queryWrapper);
    }


    /**
     * 根据类别查找问题标签列表
     * 此方法旨在通过特定类别筛选出问题标签，并确保筛选出的标签是活跃且未被删除的，
     * 同时按照使用次数降序排列，以便优先展示常用标签
     *
     * @param category 标签的类别，用于筛选问题标签
     * @return 符合条件的问题标签列表
     */
    @Override
    public List<ProblemTagDO> findByCategory(String category) {
        // 创建Lambda查询包装器，用于构建查询条件和排序
        LambdaQueryWrapper<ProblemTagDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件：类别等于传入的category，状态为1（例如：启用状态），未删除
        queryWrapper.eq(ProblemTagDO::getCategory, category).eq(ProblemTagDO::getStatus, 1).eq(ProblemTagDO::getIsDeleted, false)
                // 设置按照使用次数降序排列
                .orderByDesc(ProblemTagDO::getUsageCount);
        // 执行查询并返回结果列表
        return problemTagMapper.selectList(queryWrapper);
    }


    /**
     * 根据标签名称查询问题标签实体
     *
     * @param tagName 标签名称，用于查询特定的问题标签实体
     * @return 如果找到匹配的标签，则返回对应的ProblemTagDO对象；否则返回null
     */
    @Override
    public ProblemTagDO findByTagName(String tagName) {
        // 创建LambdaQueryWrapper对象，用于构建查询条件
        LambdaQueryWrapper<ProblemTagDO> queryWrapper = new LambdaQueryWrapper<>();
        // 设置查询条件：标签名称等于传入的tagName且未删除
        queryWrapper.eq(ProblemTagDO::getTagName, tagName).eq(ProblemTagDO::getIsDeleted, false);
        // 执行查询并返回结果
        return problemTagMapper.selectOne(queryWrapper);
    }


    /**
     * 根据关键词搜索问题标签
     * <p>
     * 本方法通过构建一个Lambda查询包装器来实现对问题标签的搜索功能它主要完成了以下几个任务：
     * 1. 定义了查询条件，包括标签名（中文和英文）的模糊匹配
     * 2. 设置了标签的状态为有效（status=1）且未被删除（isDeleted=false）
     * 3. 按照标签的使用次数降序排序，以确保最常用的标签优先展示
     *
     * @param keyword 用户输入的搜索关键词，用于匹配标签的中文或英文名称
     * @return 返回一个ProblemTagDO对象列表，每个对象包含了一个与关键词相关的标签信息
     */
    @Override
    public List<ProblemTagDO> searchTags(String keyword) {
        // 创建Lambda查询包装器，用于构建查询条件
        LambdaQueryWrapper<ProblemTagDO> queryWrapper = new LambdaQueryWrapper<>();
        // 构建查询条件：模糊匹配标签名或英文名，且状态为有效，未被删除，按使用次数降序排序
        queryWrapper.like(ProblemTagDO::getTagName, keyword).or().like(ProblemTagDO::getTagNameEn, keyword).eq(ProblemTagDO::getStatus, 1).eq(ProblemTagDO::getIsDeleted, false).orderByDesc(ProblemTagDO::getUsageCount);
        // 执行查询并返回结果列表
        return problemTagMapper.selectList(queryWrapper);
    }


    /**
     * 保存问题标签信息
     * <p>
     * 此方法用于将问题标签数据对象插入到数据库中
     * 它重写了父类或接口中的方法，以实现具体的问题标签保存逻辑
     *
     * @param problemTagDO 问题标签数据对象，包含要保存的问题标签信息
     * @return 返回插入操作影响的行数，通常为1表示成功，0表示失败
     */
    @Override
    public int save(ProblemTagDO problemTagDO) {
        return problemTagMapper.insert(problemTagDO);
    }


    /**
     * 根据ID更新问题标签信息，只有当字段有值时才进行更新
     *
     * @param problemTagDO 问题标签的数据对象，包含需要更新的字段
     * @return 返回更新操作影响的行数，通常为1，如果未找到对应ID的数据则为0
     */
    @Override
    public int updateById(ProblemTagDO problemTagDO) {
        // 创建UpdateWrapper对象，用于构建更新条件
        UpdateWrapper<ProblemTagDO> updateWrapper = new UpdateWrapper<>();
        // 指定更新的记录ID
        updateWrapper.eq("id", problemTagDO.getId());

        // 只有当字段不为null时，才将其添加到更新条件中
        if (problemTagDO.getStatus() != null) {
            updateWrapper.set("status", problemTagDO.getStatus());
        }
        if (problemTagDO.getTagName() != null) {
            updateWrapper.set("tag_name", problemTagDO.getTagName());
        }
        if (problemTagDO.getTagNameEn() != null) {
            updateWrapper.set("tag_name_en", problemTagDO.getTagNameEn());
        }
        if (problemTagDO.getTagColor() != null) {
            updateWrapper.set("tag_color", problemTagDO.getTagColor());
        }
        if (problemTagDO.getCategory() != null) {
            updateWrapper.set("category", problemTagDO.getCategory());
        }
        if (problemTagDO.getDescription() != null) {
            updateWrapper.set("description", problemTagDO.getDescription());
        }

        // 如果没有字段需要更新，则直接返回0
        if (updateWrapper.getSqlSet() == null || updateWrapper.getSqlSet().isEmpty()) {
            return 0;
        }

        // 执行更新操作，传入一个空的实体对象，因为所有更新的字段都已经在updateWrapper中设置了
        return problemTagMapper.update(new ProblemTagDO(), updateWrapper);
    }


    /**
     * 根据ID删除问题标签记录
     *
     * @param id 需要删除的记录的ID
     * @return 删除操作影响的行数
     */
    @Override
    public int deleteById(Long id) {
        // 创建一个ProblemTagDO对象，用于封装要删除的记录的ID和删除标记
        ProblemTagDO problemTagDO = new ProblemTagDO();
        // 设置记录的ID
        problemTagDO.setId(id);
        // 设置删除标记为1，表示该记录已被删除
        problemTagDO.setIsDeleted(1);
        // 调用mapper的updateById方法更新数据库中的记录，实现逻辑删除
        return problemTagMapper.updateById(problemTagDO);
    }

    /**
     * 根据指定条件分页查询问题标签列表
     *
     * @param page     页码，从1开始
     * @param size     每页大小
     * @param keyword  搜索关键词，可为空，用于模糊搜索标签名或英文名
     * @param category 标签分类，可为空
     * @param status   标签状态，可为空
     * @return 返回符合条件的标签列表
     */
    @Override
    public List<ProblemTagDO> findTagList(int page, int size, String keyword, String category, Integer status) {
        // 创建查询条件包装器
        LambdaQueryWrapper<ProblemTagDO> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        // 1. 关键词搜索（标签名或英文名）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(ProblemTagDO::getTagName, keyword)
                    .or()
                    .like(ProblemTagDO::getTagNameEn, keyword));
        }

        // 2. 分类筛选
        if (category != null && !category.trim().isEmpty()) {
            queryWrapper.eq(ProblemTagDO::getCategory, category);
        }

        // 3. 状态筛选
        if (status != null) {
            queryWrapper.eq(ProblemTagDO::getStatus, status);
        }

        // 4. 默认只查询未删除的标签
        queryWrapper.eq(ProblemTagDO::getIsDeleted, false);

        // 5. 设置排序规则：先按使用次数降序，再按标签名称升序
        queryWrapper.orderByDesc(ProblemTagDO::getUsageCount)
                .orderByAsc(ProblemTagDO::getTagName);

        // 创建分页对象
        Page<ProblemTagDO> pageable = new Page<>(page, size);

        // 执行分页查询
        Page<ProblemTagDO> resultPage = problemTagMapper.selectPage(pageable, queryWrapper);

        // 返回查询结果列表
        return resultPage.getRecords();
    }


    /**
     * 增加标签的使用次数
     * <p>
     * 本方法通过接收��个标签ID来定位数据库中的标签对���，并将其使用次数增加1如果标签不存在，则方法返回0
     * 此方法主要用于跟踪标签的使用频率，以便于后续的分析和统计
     *
     * @param tagId 标签的唯一标识符如果为null或数据库中不存在此ID，则不进行增加操作
     * @return 更新操作的影响行数如果返回0，表示未找到对应的标签或更新操作失败
     */
    @Override
    public int incrementUsageCount(Long tagId) {
        // 根据标签ID查询数据库中的标签对象
        ProblemTagDO problemTagDO = problemTagMapper.selectById(tagId);
        // 检查查询结果是否为空，如果不为空，则增加其使用次数并更新数据库
        if (problemTagDO != null) {
            problemTagDO.setUsageCount(problemTagDO.getUsageCount() + 1);
            return problemTagMapper.updateById(problemTagDO);
        }
        // 如果查询结果为空，返回0表示操作失败
        return 0;
    }


    /**
     * 重写减少标签使用计数的方法
     * <p>
     * 当一个标签被一个问题解除关联时，该方法会减少标签的使用计数
     * 如果标签不存在或其使用计数已经为0，则该方法不执行任何操作
     *
     * @param tagId 标签的唯一标识符
     * @return 如果标签使用计数成功减少，则返回影响的行数；否则返回0
     */
    @Override
    public int decrementUsageCount(Long tagId) {
        // 根据标签ID查询标签实体
        ProblemTagDO problemTagDO = problemTagMapper.selectById(tagId);
        // 检查标签是否存在且使用计数大于0
        if (problemTagDO != null && problemTagDO.getUsageCount() > 0) {
            // 减少标签的使用计数
            problemTagDO.setUsageCount(problemTagDO.getUsageCount() - 1);
            // 更��数据库中的标签信息
            return problemTagMapper.updateById(problemTagDO);
        }
        // 如果标签不存在或使用计数为0，则不进行更新，返回0
        return 0;
    }

    /**
     * 批量增加标签使用次数
     *
     * @param tagIds    标签ID列表
     * @param increment 增加的数量
     * @return 受影响的行数
     */
    @Override
    public int batchIncrementUsageCount(List<Long> tagIds, int increment) {
        if (tagIds == null || tagIds.isEmpty()) {
            return 0;
        }
        return problemTagMapper.batchIncrementUsageCount(tagIds, increment);
    }

    /**
     * 批量减少标签使用次数
     *
     * @param tagIds    标签ID列表
     * @param decrement 减少的数量
     * @return 受影响的行数
     */
    @Override
    public int batchDecrementUsageCount(List<Long> tagIds, int decrement) {
        if (tagIds == null || tagIds.isEmpty()) {
            return 0;
        }
        return problemTagMapper.batchDecrementUsageCount(tagIds, decrement);
    }

    /**
     * 根据使用次数范围查询标签
     *
     * @param minUsageCount 最小使用次数
     * @param maxUsageCount 最大使用次数
     * @return 标签列表
     */
    @Override
    public List<ProblemTagDO> findByUsageCountRange(Long minUsageCount, Long maxUsageCount) {
        return problemTagMapper.findByUsageCountRange(minUsageCount, maxUsageCount);
    }

    /**
     * 获取标签使用统计信息
     *
     * @param category 标签分类（可选）
     * @return 统计信息
     */
    @Override
    public List<ProblemTagDO> getTagUsageStatistics(String category) {
        return problemTagMapper.getTagUsageStatistics(category);
    }

    /**
     * 批量更新标签状态
     *
     * @param tagIds 标签ID列表
     * @param status 新状态
     * @return 受影响的行数
     */
    @Override
    public int batchUpdateStatus(List<Long> tagIds, Integer status) {
        if (tagIds == null || tagIds.isEmpty()) {
            return 0;
        }
        return problemTagMapper.batchUpdateStatus(tagIds, status);
    }

    /**
     * 查询热门标签
     *
     * @param limit    限制数量
     * @param category 标签分类（可选）
     * @return 热门标签列表
     */
    @Override
    public List<ProblemTagDO> findPopularTags(int limit, String category) {
        return problemTagMapper.findPopularTags(limit, category);
    }

    /**
     * 根据颜色查询标签
     *
     * @param tagColor 标签颜色
     * @return 标签列表
     */
    @Override
    public List<ProblemTagDO> findByTagColor(String tagColor) {
        return problemTagMapper.findByTagColor(tagColor);
    }

    /**
     * 检查标签名是否存在（排除指定ID）
     *
     * @param tagName   标签名
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    @Override
    public boolean existsByTagNameExcludeId(String tagName, Long excludeId) {
        return problemTagMapper.existsByTagNameExcludeId(tagName, excludeId);
    }

}
