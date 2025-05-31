package fun.timu.oj.common.enmus;

import lombok.Getter;


/**
 * 标签颜色枚举，限定可选颜色值
 */
@Getter
public enum ColorEnum {
    BLUE("#007bff", "蓝色"),
    GREEN("#28a745", "绿色"),
    CYAN("#17a2b8", "青色"),
    YELLOW("#ffc107", "黄色"),
    ORANGE("#fd7e14", "橙色"),
    PURPLE("#6f42c1", "紫色"),
    PINK("#e83e8c", "粉色"),
    GRAY("#6c757d", "灰色"),
    TEAL("#20c997", "蓝绿色"),
    RED("#dc3545", "红色"),
    DARK_BLUE("#0d6efd", "深蓝色"),
    LIGHT_BLUE("#0dcaf0", "浅蓝色"),
    INDIGO("#6610f2", "靛蓝色"),
    LIGHT_GREEN("#8bc34a", "浅绿色"),
    DARK_GREEN("#198754", "深绿色"),
    OLIVE("#6c8c3f", "橄榄绿"),
    LIME("#cddc39", "柠檬绿"),
    AMBER("#ffc107", "琥珀色"),
    DEEP_ORANGE("#ff5722", "深橙色"),
    BROWN("#795548", "棕色"),
    DEEP_PURPLE("#673ab7", "深紫色"),
    LIGHT_PURPLE("#ba68c8", "浅紫色"),
    MAGENTA("#e91e63", "洋红色"),
    LIGHT_PINK("#f48fb1", "浅粉色"),
    DARK_GRAY("#343a40", "深灰色"),
    LIGHT_GRAY("#adb5bd", "浅灰色"),
    BLACK("#000000", "黑色"),
    WHITE("#ffffff", "白色"),
    SILVER("#c0c0c0", "银色"),
    GOLD("#ffd700", "金色"),
    MAROON("#800000", "栗色"),
    NAVY("#000080", "海军蓝"),
    FOREST("#228b22", "森林绿");

    private final String colorCode;
    private final String colorName;

    ColorEnum(String colorCode, String colorName) {
        this.colorCode = colorCode;
        this.colorName = colorName;
    }

    /**
     * 根据颜色代码获取枚举值
     */
    public static ColorEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ColorEnum color : values()) {
            if (color.getColorCode().equals(code)) {
                return color;
            }
        }
        return null;
    }

    /**
     * 验证颜色代码是否有效
     */
    public static boolean isValidColorCode(String code) {
        return getByCode(code) != null;
    }

    /**
     * 获取所有颜色代码
     */
    public static String[] getAllColorCodes() {
        ColorEnum[] values = values();
        String[] codes = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            codes[i] = values[i].getColorCode();
        }
        return codes;
    }
}