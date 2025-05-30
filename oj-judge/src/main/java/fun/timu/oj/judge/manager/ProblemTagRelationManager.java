package fun.timu.oj.judge.manager;

import fun.timu.oj.judge.model.DO.ProblemTagRelationDO;

import java.util.List;

public interface ProblemTagRelationManager {
    public List<Long> findTagIdsByProblemId(Long problemId);

    public List<Long> findProblemIdsByTagId(Long tagId);

    public ProblemTagRelationDO findByProblemIdAndTagId(Long problemId, Long tagId);

    public int save(ProblemTagRelationDO relationDO);

    public int deleteByProblemIdAndTagId(Long problemId, Long tagId);

    public int deleteByProblemId(Long problemId);

    public int deleteByTagId(Long tagId);

    public int batchSave(List<ProblemTagRelationDO> relations);
}
