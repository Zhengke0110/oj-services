package fun.timu.oj.judge.controller.request;

import lombok.Data;

import java.util.List;

@Data
public class ProblemTagBatchRequest {
    public List<Long> tagIds;
    public Integer value;
    public Type type;   // INCREMENT/DECREMENT
    public Status status; // DISABLED/ENABLED

    /**
     * 操作类型枚举：increment（增加）、decrement（减少）
     */
    public enum Type {
        INCREMENT("increment"),
        DECREMENT("decrement");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        /**
         * 根据字符串值获取对应的枚举
         */
        public static Type fromValue(String value) {
            for (Type type : Type.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid type value: " + value);
        }
    }

    /**
     * 状态枚举：0-禁用，1-启用
     */
    public enum Status {
        DISABLED(0),
        ENABLED(1);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        /**
         * 根据状态码获取对应的枚举
         */
        public static Status fromCode(int code) {
            for (Status status : Status.values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid status code: " + code);
        }
    }
}
