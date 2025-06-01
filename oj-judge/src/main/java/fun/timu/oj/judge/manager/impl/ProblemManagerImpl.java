package fun.timu.oj.judge.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.mapper.ProblemMapper;
import fun.timu.oj.judge.model.DO.ProblemDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProblemManagerImpl implements ProblemManager {
    private final ProblemMapper problemMapper;

    /**
     * 根据id查询问题
     *
     * @param id 问题id
     * @return 问题
     */
    @Override
    public ProblemDO getById(Long id) {
        return problemMapper.selectById(id);
    }
}
