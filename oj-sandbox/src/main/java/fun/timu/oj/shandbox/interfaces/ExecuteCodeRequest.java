package fun.timu.oj.shandbox.interfaces;

import lombok.Data;

import java.util.List;

/**
 * 代码执行请求类 - 用于接口层
 */
@Data
public class ExecuteCodeRequest {
    /**
     * 编程语言
     */
    private ProgrammingLanguage language;

    /**
     * 要执行的代码
     */
    private String code;

    /**
     * 输入参数列表
     */
    private List<String> inputs;

    /**
     * 输入类型: "PARAMS"（命令行参数）或 "FILE"（测试文件）
     */
    private String inputType;

    /**
     * 执行次数，默认为1
     */
    private Integer executionCount = 1;

}
