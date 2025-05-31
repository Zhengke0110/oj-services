package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
     * 分页查询问题标签列表（返回分页对象）
     *
     * @param page     页码，从1开始
     * @param size     每页大小
     * @param keyword  搜索关键词，可为空，用于模糊搜索标签名或英文名
     * @param category 标签分类，可为空
     * @param status   标签状态，可为空
     * @param tagColor 标签颜色，可为空
     * @return 返回分页结果
     */
    @Override
    public IPage<ProblemTagDO> findTagListWithPage(int page, int size, String keyword, String category, Integer status, String tagColor) {
        // 创建查询条件包装器
        LambdaQueryWrapper<ProblemTagDO> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        // 1. 关键词搜索（标签名或英文名）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper.like(ProblemTagDO::getTagName, keyword).or().like(ProblemTagDO::getTagNameEn, keyword));
        }

        // 2. 分类筛选
        if (category != null && !category.trim().isEmpty()) {
            queryWrapper.eq(ProblemTagDO::getCategory, category);
        }

        // 3. 状态筛选
        if (status != null) {
            queryWrapper.eq(ProblemTagDO::getStatus, status);
        }

        // 4. 颜色筛选（新增）
        if (tagColor != null && !tagColor.trim().isEmpty()) {
            queryWrapper.eq(ProblemTagDO::getTagColor, tagColor);
        }

        // 5. 默认只查询未删除的标签
        queryWrapper.eq(ProblemTagDO::getIsDeleted, false);

        // 6. 设置排序规则：先按使用次数降序，再按标签名称升序
        queryWrapper.orderByDesc(ProblemTagDO::getUsageCount).orderByAsc(ProblemTagDO::getTagName);

        // 创建分页对象
        Page<ProblemTagDO> pageable = new Page<>(page, size);

        // 执行分页查询并返回结果
        return problemTagMapper.selectPage(pageable, queryWrapper);
    }

}
