package fun.timu.oj.judge.service.impl.Problem;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.timu.oj.common.model.PageResult;
import fun.timu.oj.judge.manager.ProblemManager;
import fun.timu.oj.judge.model.DO.ProblemDO;
import fun.timu.oj.judge.model.VO.ProblemVO;
import fun.timu.oj.judge.service.Problem.ProblemFilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemFilterServiceImpl implements ProblemFilterService {
    private final ProblemManager problemManager;

    /**
     * 根据创建时间范围查询题目
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 分页题目列表
     */
    @Override
    public PageResult<ProblemVO> selectByDateRange(Date startDate, Date endDate, int pageNum, int pageSize) {
        try {
            // 记录日志
            log.info("按时间范围查询题目, 开始时间: {}, 结束时间: {}, 页码: {}, 每页大小: {}", startDate, endDate, pageNum, pageSize);

            // 默认查询状态为1（激活状态）的题目
            Integer status = 1;

            // TODO 多表联查优化：在ProblemManager中新增selectByDateRangeWithTagsAndCreator()方法
            // TODO 通过LEFT JOIN problem_tag_relation、problem_tag 和 user 表一次性获取题目、标签、创建者信息
            // TODO 或调用ProblemTagRelationManager.getTagNamesByProblemIds()批量获取标签名称
            // 调用Manager层方法获取分页数据
            IPage<ProblemDO> problemPage = problemManager.selectByDateRange(startDate, endDate, status, pageNum, pageSize);

            if (problemPage == null || problemPage.getRecords().isEmpty()) {
                throw new RuntimeException("没有找到符合要求的题目");
            }

            // 转换DO对象为VO对象
            List<ProblemVO> problemVOList = problemPage.getRecords().stream().map(ProblemUtils::convertToVO).collect(Collectors.toList());

            // TODO 多表联查优化：调用ProblemTagRelationManager.getTagNamesByProblemIds()批量获取所有题目的标签信息
            // TODO 调用UserManager.findByIds()批量获取创建者信息，避免N+1查询问题
            // TODO 在ProblemTagRelationManager中新增findByProblemIdsWithCreatorInfo()方法

            PageResult<ProblemVO> pageResult = new PageResult<>(problemVOList, problemPage.getTotal(), (int) problemPage.getSize(), (int) problemPage.getCurrent(), (int) problemPage.getPages());
            log.info("成功查询时间范围内的题目列表，当前页: {}, 每页大小: {}, 总数: {}", pageNum, pageSize, problemPage.getTotal());
            return pageResult;
        } catch (Exception e) {
            log.error("按时间范围查询题目失败: {}", e.getMessage(), e);
            return new PageResult<>();
        }
    }


    /**
     * 查询长时间未更新的题目
     *
     * @param days     超过多少天未更新视为长时间未更新
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<ProblemVO> selectStaleProblems(int days, int pageNum, int pageSize) {
        try {
            // 计算目标日期：当前日期减去指定的天数
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -days);
            Date lastUpdateBefore = calendar.getTime();

            // TODO 多表联查优化：在ProblemManager中新增selectStaleProblemsWithAnalysisData()方法
            // TODO 通过JOIN problem_tag_relation、problem_tag、user 表获取长期未更新题目的完整分析数据
            // TODO 包括题目标签分布、创建者活跃度等信息，用于分析长期未更新的原因
            // 调用管理层方法查询数据
            IPage<ProblemDO> problemPage = problemManager.selectStaleProblems(lastUpdateBefore, pageNum, pageSize);


            // 转换DO对象为VO对象
            List<ProblemVO> problemVOList = problemPage.getRecords().stream().map(ProblemUtils::convertToVO).collect(Collectors.toList());

            // TODO 多表联查优化：调用ProblemTagRelationManager.getTagNamesByProblemIds()获取题目标签
            // TODO 调用UserManager.findByIds()获取创建者信息和活跃度数据
            // TODO 用于分析长期未更新题目的标签分布和创建者活跃情况

            // 构建并返回分页结果
            PageResult<ProblemVO> pageResult = new PageResult<>(problemVOList, problemPage.getTotal(), (int) problemPage.getSize(), (int) problemPage.getCurrent(), (int) problemPage.getPages());
            log.info("ProblemService--->成功查询长时间未更新题目，当前页: {}, 每页大小: {}, 总数: {}", pageNum, pageSize, problemPage.getTotal());
            return pageResult;
        } catch (Exception e) {
            log.error("ProblemService--->查询长时间未更新题目失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询长时间未更新题目失败", e);
        }
    }

    /**
     * 根据题目ID列表获取题目基本信息
     *
     * @param problemIds 题目ID列表
     * @return 包含题目基本信息的列表
     */
    @Override
    public List<ProblemVO> selectBasicInfoByIds(List<Long> problemIds) {
        try {
            log.info("ProblemService--->批量获取题目基本信息, 题目数量: {}", problemIds == null ? 0 : problemIds.size());

            // 参数校验
            if (problemIds == null || problemIds.isEmpty()) {
                log.warn("批量获取题目基本信息失败：题目ID列表为空");
                return new ArrayList<>();
            }

            // TODO 多表联查优化：在ProblemManager中新增selectBasicInfoWithTagsByIds()方法
            // TODO 通过LEFT JOIN problem_tag_relation 和 problem_tag 表一次性获取题目基本信息和相关标签
            // TODO 或者调用ProblemTagRelationManager.getTagNamesByProblemIds()批量获取标签信息
            // TODO 提高批量查询的效率，减少数据库查询次数
            // 调用manager层获取题目基本信息
            List<ProblemDO> problemDOList = problemManager.selectBasicInfoByIds(problemIds);

            // 将DO列表转换为VO列表
            List<ProblemVO> problemVOList = problemDOList.stream().map(ProblemUtils::convertToBasicVO).filter(Objects::nonNull).collect(Collectors.toList());

            log.info("批量获取题目基本信息成功, 获取到的题目数量: {}", problemVOList.size());
            return problemVOList;
        } catch (Exception e) {
            log.error("ProblemService--->批量获取题目基本信息失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据支持的编程语言查询题目
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param language 编程语言
     * @return 分页题目列表结果
     */
    @Override
    public PageResult<ProblemVO> selectByLanguage(int pageNum, int pageSize, String language) {
        try {
            log.info("ProblemService--->根据支持的编程语言查询题目, 页码: {}, 每页数量: {}, 语言: {}", pageNum, pageSize, language);

            // TODO 多表联查优化：在ProblemManager中新增selectByLanguageWithTags()方法
            // TODO 通过LEFT JOIN problem_tag_relation 和 problem_tag 表联查题目和标签信息
            // TODO 为支持特定语言的题目提供标签分类信息，帮助用户更好地选择题目
            // 调用manager层获取分页数据
            IPage<ProblemDO> problemPage = problemManager.selectByLanguage(pageNum, pageSize, language);

            // 将DO列表转换为VO列表
            List<ProblemVO> problemVOList = problemPage.getRecords().stream().map(ProblemUtils::convertToVO).filter(Objects::nonNull).collect(Collectors.toList());

            // TODO 多表联查优化：调用ProblemTagRelationManager.getTagNamesByProblemIds()批量获取题目标签信息
            // TODO 帮助用户了解支持特定语言的题目在算法分类上的分布情况

            // 构建分页结果
            PageResult<ProblemVO> pageResult = new PageResult<>(problemVOList, problemPage.getTotal(), (int) problemPage.getSize(), (int) problemPage.getCurrent(), (int) problemPage.getPages());

            log.info("根据编程语言查询题目成功, 语言: {}, 总数: {}", language, pageResult.getTotal());
            return pageResult;
        } catch (Exception e) {
            log.error("ProblemService--->根据支持的编程语言查询题目失败: {}", e.getMessage(), e);
            return new PageResult<>();
        }
    }


    /**
     * 查询最近创建的题目
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param limit    限制返回的题目总数（可为null，表示无上限）
     * @return 最近创建的题目列表
     */
    @Override
    public List<ProblemVO> selectRecentProblems(int pageNum, int pageSize, Integer limit) {
        try {
            // TODO 多表联查优化：在ProblemManager中新增selectRecentProblemsWithDetails()方法
            // TODO 通过LEFT JOIN problem_tag_relation、problem_tag、user 表联查题目、标签、创建者信息
            // TODO 为最近创建的题目提供更丰富的展示信息，包括创建者昵称、题目标签等
            // 调用manager层查询最近创建的题目
            IPage<ProblemDO> problemPage = problemManager.selectRecentProblems(pageNum, pageSize, limit);

            // 将DO列表转换为VO列表
            List<ProblemVO> problemVOList = problemPage.getRecords().stream().map(ProblemUtils::convertToVO).filter(Objects::nonNull).collect(Collectors.toList());

            // TODO 多表联查优化：调用ProblemTagRelationManager.getTagNamesByProblemIds()批量获取最近创建题目的标签
            // TODO 调用UserManager.findByIds()批量获取创建者昵称和头像信息，提升用户体验

            log.info("ProblemService--->获取最近创建的题目成功，页码: {}, 每页数量: {}, 限制数量: {}, 实际获取数量: {}", pageNum, pageSize, limit, problemVOList.size());

            return problemVOList;
        } catch (Exception e) {
            log.error("ProblemService--->获取最近创建的题目失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
