package fun.timu.oj.shandbox.interfaces;

import java.util.List;

/**
 * 代码执行响应类 - 用于接口层
 */
public class ExecuteCodeResponse {
    /**
     * 执行状态
     */
    private ExecuteStatus status;
    
    /**
     * 执行后的输出
     */
    private List<String> output;
    
    /**
     * 执行信息（内存、时间消耗等）
     */
    private JudgeInfo judgeInfo;
    
    /**
     * 执行状态枚举
     */
    public enum ExecuteStatus {
        /**
         * 成功
         */
        SUCCEED,
        
        /**
         * 失败
         */
        FAILED
    }

    public ExecuteStatus getStatus() {
        return status;
    }

    public void setStatus(ExecuteStatus status) {
        this.status = status;
    }

    public List<String> getOutput() {
        return output;
    }

    public void setOutput(List<String> output) {
        this.output = output;
    }

    public JudgeInfo getJudgeInfo() {
        return judgeInfo;
    }

    public void setJudgeInfo(JudgeInfo judgeInfo) {
        this.judgeInfo = judgeInfo;
    }
}
