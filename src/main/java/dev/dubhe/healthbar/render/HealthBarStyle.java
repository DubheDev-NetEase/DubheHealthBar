package dev.dubhe.healthbar.render;

import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.util.Mth;

/**
 * 血条布局、颜色与文本格式常量及工具。
 */
public final class HealthBarStyle {
    /**
     * 血条宽度，单位为像素。
     */
    public static final int BAR_WIDTH = 40;
    /**
     * 血条高度，单位为像素。
     */
    public static final int BAR_HEIGHT = 4;
    /**
     * 血条与右侧文本的间距，单位为像素。
     */
    public static final int TEXT_GAP = 3;
    /**
     * 血条整体缩放，与 name tag 一致。
     */
    public static final float SCALE = 0.025F;
    /**
     * 存在 name tag 时血条中心相对附着点的高度偏移，单位为格。
     */
    public static final float NAME_TAG_OFFSET = 0.23F;
    /**
     * 不存在 name tag 时血条中心相对附着点的高度偏移，单位为格。
     */
    public static final float NO_NAME_TAG_OFFSET = 0.12F;
    /**
     * 附着点为空时相对生物包围盒高度追加的偏移，单位为格。
     */
    public static final float FALLBACK_ATTACHMENT_HEIGHT = 0.5F;
    /**
     * 血条背景色。
     */
    public static final int BACKGROUND_COLOR = 0x99202020;
    /**
     * 生命值文本颜色。
     */
    public static final int TEXT_COLOR = 0xFFFFFFFF;
    /**
     * 生命值文本顶部相对血条中心的偏移，单位为像素。
     */
    public static final int TEXT_TOP = -4;

    private static final float INTEGER_EPSILON = 1.0E-4F;

    private HealthBarStyle() {
    }

    /**
     * 按规范格式化生命值，整数直接显示，否则保留一位小数。
     *
     * @param value 生命值
     * @return 格式化文本
     */
    public static String formatHealth(float value) {
        float rounded = Math.round(value);
        if (Math.abs(value - rounded) < INTEGER_EPSILON) {
            return Integer.toString((int) rounded);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    /**
     * 生成当前生命值与最大生命值的显示文本。
     *
     * @param health    当前生命值
     * @param maxHealth 最大生命值
     * @return 显示文本
     */
    public static String buildText(float health, float maxHealth) {
        return formatHealth(Mth.clamp(health, 0.0F, maxHealth)) + "/" + formatHealth(maxHealth);
    }

    /**
     * 计算血条填充比例。
     *
     * @param health    当前生命值
     * @param maxHealth 最大生命值
     * @return 0 到 1 之间的填充比例
     */
    public static float healthRatio(float health, float maxHealth) {
        if (maxHealth <= 0.0F) {
            return 0.0F;
        }
        return Mth.clamp(health / maxHealth, 0.0F, 1.0F);
    }

    /**
     * 计算血条与文本的整体宽度。
     *
     * @param font 字体
     * @param text 生命值文本
     * @return 整体宽度，单位为像素
     */
    public static int totalWidth(Font font, String text) {
        return BAR_WIDTH + TEXT_GAP + font.width(text);
    }

    /**
     * 计算文本左侧相对血条中心的像素偏移。
     *
     * @return 文本左侧偏移，单位为像素
     */
    public static float textOffsetX() {
        return BAR_WIDTH / 2.0F + TEXT_GAP;
    }

    /**
     * 计算血条中心相对实体头顶中点的像素偏移，使血条与文本整体居中。
     *
     * @param font 字体
     * @param text 生命值文本
     * @return 血条中心偏移，单位为像素
     */
    public static float barCenterOffset(Font font, String text) {
        return -(TEXT_GAP + font.width(text)) / 2.0F;
    }
}
