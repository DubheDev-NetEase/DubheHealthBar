package dev.dubhe.healthbar.render;

import java.util.Locale;
import net.minecraft.util.Mth;

/**
 * 血条与浮动数字共用的布局、颜色与文本格式常量。
 */
public final class HealthBarStyle {
    /**
     * 世界空间血条整体缩放，与 name tag 一致。
     */
    public static final float SCALE = 0.025F;

    /**
     * 血条宽度，单位为像素。
     */
    public static final float BAR_WIDTH = 40.0F;

    /**
     * 血条高度，单位为像素。
     */
    public static final float BAR_HEIGHT = 4.0F;

    /**
     * 血条与右侧生命值文本的间距，单位为像素。
     */
    public static final float TEXT_GAP = 3.0F;

    /**
     * 生命值文本顶部相对血条中心的 y 偏移，单位为像素。
     */
    public static final float TEXT_TOP = -4.0F;

    /**
     * 浮动数字文本顶部相对投影点的 y 偏移，单位为像素。
     */
    public static final float NUMBER_TOP = -4.0F;

    /**
     * 圆角半径，单位为像素。
     */
    public static final float CORNER_RADIUS = 1.2F;

    /**
     * 圆角抗锯齿宽度，单位为像素。
     */
    public static final float EDGE_WIDTH = 0.75F;

    /**
     * 血条背景色。
     */
    public static final int BACKGROUND_COLOR = 0x99202020;

    /**
     * 生命值文本颜色。
     */
    public static final int TEXT_COLOR = 0xFFFFFFFF;

    /**
     * 生命值增加时浮动数字的颜色。
     */
    public static final int GAIN_COLOR = 0xFF55FF55;

    /**
     * 生命值减少时浮动数字的颜色。
     */
    public static final int LOSS_COLOR = 0xFFFF5555;

    /**
     * name tag 可见时血条中心相对附着点的高度偏移，单位为格。
     */
    public static final double NAME_TAG_BAR_OFFSET = 0.23;

    /**
     * name tag 不可见时血条中心相对附着点的高度偏移，单位为格。
     */
    public static final double DEFAULT_BAR_OFFSET = 0.12;

    /**
     * name tag 附着点为空时使用的基准高度偏移，单位为格。
     */
    public static final double FALLBACK_HEIGHT_OFFSET = 0.5;

    /**
     * 浮动数字生命周期，单位为秒。
     */
    public static final double LIFETIME_SECONDS = 1.1;

    /**
     * 浮动数字生命周期占总时长的淡出比例。
     */
    public static final double FADE_TIME_RATIO = 0.25;

    /**
     * 重力加速度，单位为格每 tick 平方。
     */
    public static final double GRAVITY = 0.02;

    /**
     * 竖直初速度下限，单位为格每 tick。
     */
    public static final double MIN_VERTICAL_SPEED = 0.16;

    /**
     * 竖直初速度上限，单位为格每 tick。
     */
    public static final double MAX_VERTICAL_SPEED = 0.21;

    /**
     * 水平初速度下限，单位为格每 tick。
     */
    public static final double MIN_HORIZONTAL_SPEED = 0.035;

    /**
     * 水平初速度上限，单位为格每 tick。
     */
    public static final double MAX_HORIZONTAL_SPEED = 0.065;

    /**
     * 浮动数字生成位置的水平随机偏移半径，单位为格。
     */
    public static final double SPAWN_JITTER = 0.15;

    /**
     * 浮动数字生成位置高于实体包围盒顶部的距离，单位为格。
     */
    public static final double SPAWN_HEIGHT = 0.25;

    /**
     * 脚部淡出距离，单位为格。
     */
    public static final double FEET_FADE_DISTANCE = 0.55;

    /**
     * 浮动数字数量上限。
     */
    public static final int MAX_NUMBERS = 256;

    private HealthBarStyle() {
    }

    /**
     * 把生命值数值格式化为显示文本：整数直接显示，否则保留一位小数。
     *
     * @param value 生命值数值
     * @return 显示文本
     */
    public static String formatNumber(float value) {
        float rounded = Math.round(value);
        if (Math.abs(value - rounded) < 0.001F) {
            return Integer.toString((int) rounded);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    /**
     * 把当前生命值与最大生命值格式化为 {@code 当前/最大} 文本。
     *
     * @param health    当前生命值
     * @param maxHealth 最大生命值
     * @return 显示文本
     */
    public static String formatHealth(float health, float maxHealth) {
        float clampedHealth = Mth.clamp(health, 0.0F, maxHealth);
        return formatNumber(clampedHealth) + "/" + formatNumber(maxHealth);
    }

    /**
     * 计算血条填充比例。
     *
     * @param health    当前生命值
     * @param maxHealth 最大生命值
     * @return 0 到 1 之间的填充比例
     */
    public static float fillProgress(float health, float maxHealth) {
        if (maxHealth <= 0.0F) {
            return 0.0F;
        }
        return Mth.clamp(health / maxHealth, 0.0F, 1.0F);
    }
}
