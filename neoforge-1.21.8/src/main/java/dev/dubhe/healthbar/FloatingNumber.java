package dev.dubhe.healthbar;

import net.minecraft.Util;
import net.minecraft.util.Mth;

/**
 * 单条生命值变化数字的动画数据。
 *
 * <p>世界坐标与速度全部使用 {@code double} 保存；动画时间取自
 * {@link Util#getMillis()}，因此 GUI 每帧都能得到连续位置，不会出现 tick 阶梯。
 * 实体位置只在生成瞬间取样，之后与实体完全无关。</p>
 */
public final class FloatingNumber {
    /** 生命周期，秒。 */
    public static final float LIFETIME_SECONDS = 1.1F;
    /** 最近淡出部分占整个生命周期的比例。 */
    public static final float FADE_FRACTION = 0.25F;
    /** 重力加速度，格/tick²。 */
    public static final double GRAVITY = 0.02;
    /** 每秒对应的 tick 数。 */
    public static final double TICKS_PER_SECOND = 20.0;
    /** 脚部淡出距离，格。 */
    public static final double FEET_FADE_DISTANCE = 0.55;

    private final double startX;
    private final double startY;
    private final double startZ;
    private final double velocityX;
    private final double velocityY;
    private final double velocityZ;
    private final double feetY;
    private final String text;
    private final int color;
    private final long spawnTimeMillis;

    /**
     * 创建一条数字动画，生成时刻取当前系统毫秒。
     *
     * @param startX 起始 X 世界坐标
     * @param startY 起始 Y 世界坐标
     * @param startZ 起始 Z 世界坐标
     * @param velocityX 水平 X 速度，格/tick
     * @param velocityY 竖直速度，格/tick
     * @param velocityZ 水平 Z 速度，格/tick
     * @param feetY 实体脚部 Y 坐标
     * @param text 显示文本
     * @param color RGB 颜色，alpha 由淡出计算
     */
    public FloatingNumber(
            double startX,
            double startY,
            double startZ,
            double velocityX,
            double velocityY,
            double velocityZ,
            double feetY,
            String text,
            int color) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.feetY = feetY;
        this.text = text;
        this.color = color;
        this.spawnTimeMillis = Util.getMillis();
    }

    /**
     * 判断是否已超过生命周期。
     *
     * @return 超过生命周期时返回 {@code true}
     */
    public boolean isExpired() {
        return this.getElapsedSeconds() >= LIFETIME_SECONDS;
    }

    /**
     * 获取当前 X 世界坐标。
     *
     * @return X 坐标
     */
    public double getX() {
        return this.startX + this.velocityX * this.elapsedTicks();
    }

    /**
     * 获取当前 Y 世界坐标。
     *
     * @return Y 坐标
     */
    public double getY() {
        double tick = this.elapsedTicks();
        return this.startY + this.velocityY * tick - 0.5 * GRAVITY * tick * tick;
    }

    /**
     * 获取当前 Z 世界坐标。
     *
     * @return Z 坐标
     */
    public double getZ() {
        return this.startZ + this.velocityZ * this.elapsedTicks();
    }

    /**
     * 计算当前 alpha，同时应用生命周期末尾淡出与脚部淡出，取较小值。
     *
     * @return {@code 0..1} 的 alpha
     */
    public float getAlpha() {
        float progress = this.getElapsedSeconds() / LIFETIME_SECONDS;
        float lifeAlpha = 1.0F - Mth.clamp((progress - (1.0F - FADE_FRACTION)) / FADE_FRACTION, 0.0F, 1.0F);
        float feetAlpha = Mth.clamp((float) ((this.getY() - this.feetY) / FEET_FADE_DISTANCE), 0.0F, 1.0F);
        return Math.min(lifeAlpha, feetAlpha);
    }

    /**
     * 获取显示文本。
     *
     * @return 带正负号的数值文本
     */
    public String getText() {
        return this.text;
    }

    /**
     * 获取 RGB 颜色。
     *
     * @return RGB 颜色，仅低 24 位有效
     */
    public int getColor() {
        return this.color;
    }

    private float getElapsedSeconds() {
        return (Util.getMillis() - this.spawnTimeMillis) / 1000.0F;
    }

    private double elapsedTicks() {
        return this.getElapsedSeconds() * TICKS_PER_SECOND;
    }
}
