package dev.dubhe.healthbar.render;

import java.util.Locale;

/**
 * 世界空间血条的布局、颜色与文本格式常量。
 *
 * <p>长度单位均为血条局部像素，最终由 {@link #SCALE} 缩放为世界空间尺寸。</p>
 */
public final class HealthBarStyle {
    /** 血条宽度，像素。 */
    public static final float BAR_WIDTH = 40.0F;
    /** 血条高度，像素。 */
    public static final float BAR_HEIGHT = 4.0F;
    /** 血条整体缩放，与 name tag 的 0.025 一致。 */
    public static final float SCALE = 0.025F;
    /** 血条与右侧文本的间距，像素。 */
    public static final float TEXT_GAP = 3.0F;
    /** 文本顶部相对血条中心的偏移，像素。 */
    public static final float TEXT_TOP = -4.0F;
    /** name tag 可见时血条中心相对附着点的上移量，格。 */
    public static final float OFFSET_WITH_NAME_TAG = 0.23F;
    /** name tag 不可见时血条中心相对附着点的上移量，格。 */
    public static final float OFFSET_WITHOUT_NAME_TAG = 0.12F;
    /** 生命值文本颜色。 */
    public static final int TEXT_COLOR = 0xFFFFFFFF;
    /** 生命值增加数字的颜色。 */
    public static final int INCREASE_COLOR = 0xFF55FF55;
    /** 生命值减少数字的颜色。 */
    public static final int DECREASE_COLOR = 0xFFFF5555;

    private HealthBarStyle() {
    }

    /**
     * 格式化生命值数值：整数直接显示整数，否则保留一位小数。
     *
     * @param value 数值
     * @return 文本形式
     */
    public static String formatNumber(float value) {
        float rounded = Math.round(value);
        if (Math.abs(value - rounded) < 0.001F) {
            return Integer.toString((int) rounded);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    /**
     * 生成血条右侧的“当前/最大”文本。
     *
     * @param current 当前生命值，调用方需保证位于 {@code 0..max} 内
     * @param max 最大生命值
     * @return 生命值文本
     */
    public static String healthText(float current, float max) {
        return formatNumber(current) + "/" + formatNumber(max);
    }
}
