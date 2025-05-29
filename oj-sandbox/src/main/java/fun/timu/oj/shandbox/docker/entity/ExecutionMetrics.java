package fun.timu.oj.shandbox.docker.entity;

/**
 * 代码执行指标接口
 */
public interface ExecutionMetrics {
    String getStatus();
    String getOutput();
    long getExecutionTime();
    long getMemoryUsed();
    boolean isOutputMatched();
}
