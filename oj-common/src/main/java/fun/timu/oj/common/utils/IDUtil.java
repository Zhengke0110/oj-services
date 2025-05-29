package fun.timu.oj.common.utils;


public class IDUtil {

    // 创建雪花ID生成器实例，使用固定的工作机器ID和数据中心ID
    // 在实际生产环境中，这些值应该根据部署情况配置
//    private static final SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 1);
    private static final NonSequentialSnowflakeIdGenerator snowflakeIdGenerator = new NonSequentialSnowflakeIdGenerator(1, 1);

    /**
     * 雪花算法生成器
     * 使用雪花算法生成一个唯一的ID作为分片键
     * 雪花算法是一种分布式ID生成算法，可以生成出唯一的、有序的、可排序的ID
     * 选择使用雪花算法生成ID的原因是它能够在分布式系统中高效地生成不重复的ID，
     * 同时保证ID的有序性，有利于数据的排序和查询
     *
     * @return 生成的雪花ID，作为方法的返回值，用于在分布式系统中唯一标识某个实体或事件
     */
    public static Comparable<?> geneSnowFlakeID() {
        return snowflakeIdGenerator.nextId();
    }
}