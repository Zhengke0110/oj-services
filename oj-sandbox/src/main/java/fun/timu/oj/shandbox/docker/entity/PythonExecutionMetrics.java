package fun.timu.oj.shandbox.docker.entity;

/**
 * Python执行指标实现类
 */
public class PythonExecutionMetrics implements ExecutionMetrics {
    private final String status;
    private final String output;
    private final long executionTime;
    private final long memoryUsed;
    private final boolean outputMatched;

    public PythonExecutionMetrics(String status, String output, long executionTime, long memoryUsed, boolean outputMatched) {
        this.status = status;
        this.output = output;
        this.executionTime = executionTime;
        this.memoryUsed = memoryUsed;
        this.outputMatched = outputMatched;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getOutput() {
        return output;
    }

    @Override
    public long getExecutionTime() {
        return executionTime;
    }

    @Override
    public long getMemoryUsed() {
        return memoryUsed;
    }

    @Override
    public boolean isOutputMatched() {
        return outputMatched;
    }
}
