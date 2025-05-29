package fun.timu.oj.common.utils;

import java.security.SecureRandom;

/**
 * 不连续雪花算法ID生成器
 */
public class NonSequentialSnowflakeIdGenerator {

    // 开始时间截 (2023-01-01)
    private final long twepoch = 1672531200000L;

    // 机器id所占的位数
    private final long workerIdBits = 5L;
    // 数据标识id所占的位数
    private final long datacenterIdBits = 5L;

    // 支持的最大机器id
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    // 支持的最大数据标识id
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    // 序列在id中占的位数
    private final long sequenceBits = 12L;

    // 机器ID向左移12位
    private final long workerIdShift = sequenceBits;
    // 数据标识id向左移17位(12+5)
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    // 时间截向左移22位(5+5+12)
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    // 生成序列的掩码
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    // 工作机器ID(0~31)
    private long workerId;
    // 数据中心ID(0~31)
    private long datacenterId;
    // 毫秒内序列(0~4095)
    private long sequence = 0L;
    // 上次生成ID的时间截
    private long lastTimestamp = -1L;

    // 随机数生成器
    private final SecureRandom random = new SecureRandom();

    // 构造函数保持不变...

    public NonSequentialSnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("Worker ID不能大于%d或小于0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("Datacenter ID不能大于%d或小于0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;

        // 初始化随机种子
        random.setSeed(System.nanoTime());
    }

    /**
     * 获得下一个不规律递增ID (线程安全)
     * 确保ID总体递增但不以固定步长增长
     *
     * @return 不规律递增的雪花ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("时钟向后移动。拒绝生成ID %d毫秒", lastTimestamp - timestamp));
        }

        // 使用随机跳跃的序列号，但保证递增
        if (lastTimestamp == timestamp) {
            // 随机增加序列，增量为1-10之间的随机数
            // 这样同一毫秒内生成的ID会以不规则步长递增
            sequence = (sequence + 1 + random.nextInt(10)) & sequenceMask;

            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 时间戳改变，使用较小的随机值初始化序列
            // 这样确保时间戳增加时，新生成的ID总是大于上一毫秒的ID
            sequence = random.nextInt(10);
        }

        // 上次生成ID的时间截
        lastTimestamp = timestamp;

        // 生成基础ID (保留时间戳的递增特性)
        long id = ((timestamp - twepoch) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;

        // 对ID的低位进行轻微扰动，但保留高位时间戳的有序性
        return partialScrambleId(id);
    }

    /**
     * 对ID进行部分扰动，只扰动低位，保留高位的递增特性
     */
    private long partialScrambleId(long id) {
        // 提取高位时间戳部分(保持不变)
        long highBits = id & (~0L << 22); // 保留时间戳部分

        // 提取低位部分(进行扰动)
        long lowBits = id & ((1L << 22) - 1);

        // 只对低位部分进行轻微扰动
        lowBits = lowBits ^ (lowBits >> 4);

        // 组合高位和扰动后的低位
        return highBits | (lowBits & ((1L << 22) - 1));
    }

    // tilNextMillis 和 timeGen 方法保持不变...

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }
}