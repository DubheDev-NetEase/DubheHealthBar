package dev.dubhe.healthbar;

import dev.dubhe.healthbar.render.HealthBarStyle;
import net.minecraft.Util;
import net.minecraft.util.Mth;

/**
 * 单条生命值变化浮动数字的动画数据。
 *
 * <p>生成时刻使用 {@link Util#getMillis()} 记录，位置与透明度都基于真实时间的秒数换算，
 * 因此不受实体 tick 位置跳变影响，也不会出现按 tick 阶梯推进的卡顿。</p>
 */
public final class FloatingNumber {
    private final long spawnTimeMillis;
    private final double startX;
    private final double startY;
    private final double startZ;
    private final double feetY;
    private final double velocityX;
    private final double velocityY;
    private final double velocityZ;
    private final float value;
    private final boolean gain;

    /**
     * 构造一条浮动数字。
     *
     * @param spawnTimeMillis 生成时刻的毫秒时间戳
     * @param startX          生成位置 X
     * @param startY          生成位置 Y
     * @param startZ          生成位置 Z
     * @param feetY           实体脚部 Y
     * @param velocityX       水平速度 X，单位为格每 tick
     * @param velocityY       竖直初速度，单位为格每 tick
     * @param velocityZ       水平速度 Z，单位为格每 tick
     * @param value           变化数值的绝对值
     * @param gain            生命值是否增加
     */
    public FloatingNumber(
        long spawnTimeMillis,
        double startX,
        double startY,
        double startZ,
        double feetY,
        double velocityX,
        double velocityY,
        double velocityZ,
        float value,
        boolean gain
    ) {
        this.spawnTimeMillis = spawnTimeMillis;
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.feetY = feetY;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.value = value;
        this.gain = gain;
    }

    /**
     * 判断该数字是否已经超过生命周期。
     *
     * @return 是否过期
     */
    public boolean isExpired() {
        return this.seconds(Util.getMillis()) >= HealthBarStyle.LIFETIME_SECONDS;
    }

    /**
     * 计算指定时刻的动画进度，单位为 tick。
     *
     * @param nowMillis 当前毫秒时间戳
     * @return 动画进度
     */
    public double tick(long nowMillis) {
        return this.seconds(nowMillis) * 20.0;
    }

    /**
     * 计算指定时刻的世界坐标 X。
     *
     * @param nowMillis 当前毫秒时间戳
     * @return 世界坐标 X
     */
    public double positionX(long nowMillis) {
        return this.startX + this.velocityX * this.tick(nowMillis);
    }

    /**
     * 计算指定时刻的世界坐标 Y。
     *
     * @param nowMillis 当前毫秒时间戳
     * @return 世界坐标 Y
     */
    public double positionY(long nowMillis) {
        double tick = this.tick(nowMillis);
        return this.startY + this.velocityY * tick - 0.5 * HealthBarStyle.GRAVITY * tick * tick;
    }

    /**
     * 计算指定时刻的世界坐标 Z。
     *
     * @param nowMillis 当前毫秒时间戳
     * @return 世界坐标 Z
     */
    public double positionZ(long nowMillis) {
        return this.startZ + this.velocityZ * this.tick(nowMillis);
    }

    /**
     * 计算指定时刻的透明度，同时应用生命周期淡出与脚部淡出，取较小值。
     *
     * @param nowMillis 当前毫秒时间戳
     * @return 0 到 1 之间的透明度
     */
    public float alpha(long nowMillis) {
        double progress = this.seconds(nowMillis) / HealthBarStyle.LIFETIME_SECONDS;
        float lifeAlpha = progress <= 1.0 - HealthBarStyle.FADE_TIME_RATIO
            ? 1.0F
            : (float) Mth.clamp(
                1.0 - (progress - (1.0 - HealthBarStyle.FADE_TIME_RATIO)) / HealthBarStyle.FADE_TIME_RATIO,
                0.0,
                1.0
            );
        float feetAlpha = (float) Mth.clamp(
            (this.positionY(nowMillis) - this.feetY) / HealthBarStyle.FEET_FADE_DISTANCE,
            0.0,
            1.0
        );
        return Math.min(lifeAlpha, feetAlpha);
    }

    /**
     * 计算指定时刻带有 alpha 通道的 ARGB 颜色。
     *
     * @param nowMillis 当前毫秒时间戳
     * @return ARGB 颜色
     */
    public int color(long nowMillis) {
        int alpha = Mth.clamp(Math.round(this.alpha(nowMillis) * 255.0F), 0, 255);
        int rgb = (this.gain ? HealthBarStyle.GAIN_COLOR : HealthBarStyle.LOSS_COLOR) & 0xFFFFFF;
        return alpha << 24 | rgb;
    }

    /**
     * 返回带正负号的显示文本。
     *
     * @return 显示文本
     */
    public String text() {
        return (this.gain ? "+" : "-") + HealthBarStyle.formatNumber(this.value);
    }

    private double seconds(long nowMillis) {
        return Math.max(0L, nowMillis - this.spawnTimeMillis) / 1000.0;
    }
}
