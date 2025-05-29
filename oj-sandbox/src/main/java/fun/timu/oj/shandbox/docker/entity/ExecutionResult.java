package fun.timu.oj.shandbox.docker.entity;

import java.util.List;

/**
 * 代码执行结果基类
 */
public class ExecutionResult {
    protected boolean success;
    protected boolean outputMatched;
    protected List<? extends ExecutionMetrics> executionResults;
    protected long averageExecutionTime;
    protected long averageMemoryUsed;
    protected long maxExecutionTime;
    protected long maxMemoryUsed;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isOutputMatched() {
        return outputMatched;
    }

    public void setOutputMatched(boolean outputMatched) {
        this.outputMatched = outputMatched;
    }

    public List<? extends ExecutionMetrics> getExecutionResults() {
        return executionResults;
    }

    public void setExecutionResults(List<? extends ExecutionMetrics> executionResults) {
        this.executionResults = executionResults;
    }

    public long getAverageExecutionTime() {
        return averageExecutionTime;
    }

    public void setAverageExecutionTime(long averageExecutionTime) {
        this.averageExecutionTime = averageExecutionTime;
    }

    public long getAverageMemoryUsed() {
        return averageMemoryUsed;
    }

    public void setAverageMemoryUsed(long averageMemoryUsed) {
        this.averageMemoryUsed = averageMemoryUsed;
    }

    public long getMaxExecutionTime() {
        return maxExecutionTime;
    }

    public void setMaxExecutionTime(long maxExecutionTime) {
        this.maxExecutionTime = maxExecutionTime;
    }

    public long getMaxMemoryUsed() {
        return maxMemoryUsed;
    }

    public void setMaxMemoryUsed(long maxMemoryUsed) {
        this.maxMemoryUsed = maxMemoryUsed;
    }
}
