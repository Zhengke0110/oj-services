package fun.timu.oj.judge.model.VTO;

import fun.timu.oj.judge.model.VO.ProblemVO;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 带推荐评分的题目VO
 * 
 * @author zhengke
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemWithScoreVTO {
    
    /**
     * 题目信息
     */
    private ProblemVO problem;
    
    /**
     * 推荐评分（0.0-1.0）
     */
    private Double score;
    
    /**
     * 推荐理由
     */
    private String reason;
    
    /**
     * 推荐算法类型
     */
    private String recommendationType;
    
    /**
     * 创建带评分的题目VO
     * 
     * @param problem 题目信息
     * @param score 推荐评分
     * @return 带评分的题目VO
     */
    public static ProblemWithScoreVTO of(ProblemVO problem, Double score) {
        return ProblemWithScoreVTO.builder()
                .problem(problem)
                .score(score)
                .build();
    }
    
    /**
     * 创建带评分和推荐理由的题目VO
     * 
     * @param problem 题目信息
     * @param score 推荐评分
     * @param reason 推荐理由
     * @param recommendationType 推荐算法类型
     * @return 带评分的题目VO
     */
    public static ProblemWithScoreVTO of(ProblemVO problem, Double score, String reason, String recommendationType) {
        return ProblemWithScoreVTO.builder()
                .problem(problem)
                .score(score)
                .reason(reason)
                .recommendationType(recommendationType)
                .build();
    }
}
