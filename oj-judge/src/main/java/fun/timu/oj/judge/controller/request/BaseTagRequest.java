package fun.timu.oj.judge.controller.request;

import fun.timu.oj.common.enmus.ColorEnum;
import lombok.Data;

/**
 * 问题标签请求基类，封装公共的颜色处理逻辑
 */
@Data
public abstract class BaseTagRequest {

    protected String color;

    /**
     * 设置颜色，支持颜色名称或颜色代码
     *
     * @param colorInput 颜色名称或颜色代码
     */
    public void setColor(String colorInput) {
        if (colorInput == null) {
            this.color = null;
            return;
        }

        // 如果输入的是颜色代码（以#开头），直接使用
        if (colorInput.startsWith("#")) {
            this.color = ColorEnum.isValidColorCode(colorInput) ? colorInput : getDefaultColor();
            return;
        }

        // 根据颜色名称查找对应的颜色代码
        for (ColorEnum colorEnum : ColorEnum.values()) {
            if (colorEnum.getColorName().equals(colorInput)) {
                this.color = colorEnum.getColorCode();
                return;
            }
        }

        // 如果找不到对应的颜色，使用默认颜色
        this.color = getDefaultColor();
    }

    /**
     * 获取默认颜色，由子类实现
     *
     * @return 默认颜色代码
     */
    protected abstract String getDefaultColor();
}