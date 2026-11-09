package dev.dubhe.healthbar.render;

import java.util.Locale;

/**
 * 血条布局、文本格式与位置常量。
 */
public final class HealthBarStyle {
    public static final float BAR_WIDTH = 40.0F;
    public static final float BAR_HEIGHT = 4.0F;
    public static final float SCALE = 0.025F;
    public static final float TEXT_GAP = 3.0F;
    public static final float CORNER_RADIUS = 1.2F;
    public static final float EDGE_WIDTH = 0.75F;
    public static final int TEXT_COLOR = 0xFFFFFFFF;
    public static final double ATTACHMENT_HEIGHT = 0.5;
    public static final double NAME_TAG_OFFSET = 0.23;
    public static final double NO_NAME_TAG_OFFSET = 0.12;

    private HealthBarStyle() {
    }

    /**
     * 按整数优先、否则保留一位小数的规则格式化生命值。
     *
     * @param value 生命值
     * @return 文本
     */
    public static String formatHealth(float value) {
        long rounded = Math.round(value);
        if (Math.abs(value - (float) rounded) < 1.0E-4F) {
            return Long.toString(rounded);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    public static float clampHealth(float current, float max) {
        if (max <= 0.0F) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(current, max));
    }

    public static float fillRatio(float current, float max) {
        if (max <= 0.0F) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, current / max));
    }

    public static double barOffset(boolean hasNameTag) {
        return hasNameTag ? NAME_TAG_OFFSET : NO_NAME_TAG_OFFSET;
    }
}
