package fun.timu.oj.common.utils;

import org.apache.shardingsphere.core.strategy.keygen.SnowflakeShardingKeyGenerator;

public class IDUtil {

    private static SnowflakeShardingKeyGenerator shardingKeyGenerator = new SnowflakeShardingKeyGenerator();

    /**
     * 雪花算法生成器
     * 使用雪花算法生成一个唯一的ID作为分片键
     * 雪花算法是一种分布式ID生成算法，可以生成出唯一的、有序的、可排序的ID
     * 选择使用雪花算法生成ID的原因是它能够在分布式系统中高效地生成不重复的ID，
     * 同时保证ID的有序性，有利于数据的排序和查询
     *
     * @return 生成的雪花ID，作为方法的返回值，用于在分布式系统中唯一标识某个实体或事件
     */
    public static Comparable<?> geneSnowFlakeID(){
        // 调用shardingKeyGenerator实例的generateKey方法来生成一个新的雪花ID
        return shardingKeyGenerator.generateKey();
    }

}
