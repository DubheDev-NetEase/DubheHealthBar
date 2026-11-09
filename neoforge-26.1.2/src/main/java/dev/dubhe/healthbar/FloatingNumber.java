package dev.dubhe.healthbar;

import dev.dubhe.healthbar.render.HealthBarStyle;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;

/**
 * 单条浮动伤害数字的动画数据，世界坐标使用 double 保存并在渲染时按真实时间换算。
 */
public final class FloatingNumber {
    /**
     * 生命周期，单位为秒。
     */
    public static final double LIFETIME_SECONDS = 1.1;
    /**
     * 每秒对应的 tick 数。
     */
    public static final double TICKS_PER_SECOND = 20.0;
    /**
     * 重力加速度，单位为格每 tick 平方。
     */
    public static final double GRAVITY = 0.02;
    /**
     * 最近 25% 生命周期的线性淡出时长，单位为秒。
     */
    public static final double FADE_SECONDS = LIFETIME_SECONDS * 0.25;
    /**
     * 脚部淡出距离，单位为格。
     */
    public static final double FEET_FADE_DISTANCE = 0.55;
    /**
     * 生命值增加数字的颜色。
     */
    public static final int INCREASE_COLOR = 0xFF55FF55;
    /**
     * 生命值减少数字的颜色。
     */
    public static final int DECREASE_COLOR = 0xFFFF5555;

    private static final double SPAWN_OFFSET = 0.15;
    private static final double MIN_HORIZONTAL_SPEED = 0.035;
    private static final double MAX_HORIZONTAL_SPEED = 0.065;
    private static final double MIN_VERTICAL_SPEED = 0.16;
    private static final double MAX_VERTICAL_SPEED = 0.21;
    private static final double FULL_CIRCLE = Math.PI * 2.0;
    private static final int FULL_ALPHA = 255;

    private final double startX;
    private final double startY;
    private final double startZ;
    private final double velocityX;
    private final double velocityY;
    private final double velocityZ;
    private final double feetY;
    private final long spawnMillis;
    private final String text;
    private final int baseColor;

    private FloatingNumber(
            double startX,
            double startY,
            double startZ,
            double velocityX,
            double velocityY,
            double velocityZ,
            double feetY,
            long spawnMillis,
            String text,
            int baseColor) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.feetY = feetY;
        this.spawnMillis = spawnMillis;
        this.text = text;
        this.baseColor = baseColor;
    }

    /**
     * 在生成瞬间采样实体位置并创建一条数字动画。
     *
     * @param x       实体世界 X 坐标
     * @param z       实体世界 Z 坐标
     * @param baseY   数字生成基准 Y 坐标
     * @param feetY   实体脚部 Y 坐标
     * @param amount  变化量绝对值
     * @param increase 是否为生命值增加
     * @param random  随机源
     * @return 新建的数字动画
     */
    public static FloatingNumber spawn(
            double x,
            double z,
            double baseY,
            double feetY,
            float amount,
            boolean increase,
            RandomSource random) {
        double angle = random.nextDouble() * FULL_CIRCLE;
        double horizontalSpeed = MIN_HORIZONTAL_SPEED + random.nextDouble() * (MAX_HORIZONTAL_SPEED - MIN_HORIZONTAL_SPEED);
        double verticalSpeed = MIN_VERTICAL_SPEED + random.nextDouble() * (MAX_VERTICAL_SPEED - MIN_VERTICAL_SPEED);
        double startX = x + (random.nextDouble() * 2.0 - 1.0) * SPAWN_OFFSET;
        double startZ = z + (random.nextDouble() * 2.0 - 1.0) * SPAWN_OFFSET;
        String text = (increase ? "+" : "-") + HealthBarStyle.formatHealth(amount);
        return new FloatingNumber(
                startX,
                baseY,
                startZ,
                Math.cos(angle) * horizontalSpeed,
                verticalSpeed,
                Math.sin(angle) * horizontalSpeed,
                feetY,
                Util.getMillis(),
                text,
                increase ? INCREASE_COLOR : DECREASE_COLOR);
    }

    /**
     * 返回从生成时刻到指定毫秒经过的秒数。
     *
     * @param nowMillis 当前毫秒时间戳
     * @return 经过的秒数
     */
    public double elapsedSeconds(long nowMillis) {
        return (nowMillis - this.spawnMillis) / 1000.0;
    }

    /**
     * 判断数字是否已超过生命周期。
     *
     * @param nowMillis 当前毫秒时间戳
     * @return 是否过期
     */
    public boolean isExpired(long nowMillis) {
        return this.elapsedSeconds(nowMillis) >= LIFETIME_SECONDS;
    }

    /**
     * 计算指定时刻的世界坐标位置。
     *
     * @param seconds 从生成时刻经过的秒数
     * @return 世界坐标位置
     */
    public Vec3 position(double seconds) {
        double ticks = seconds * TICKS_PER_SECOND;
        return new Vec3(
                this.startX + this.velocityX * ticks,
                this.startY + this.velocityY * ticks - 0.5 * GRAVITY * ticks * ticks,
                this.startZ + this.velocityZ * ticks);
    }

    /**
     * 计算指定时刻的透明度字节值，同时应用生命周期淡出与脚部淡出。
     *
     * @param seconds 从生成时刻经过的秒数
     * @return 0 到 255 的透明度
     */
    public int alpha(double seconds) {
        double clampedSeconds = Math.min(seconds, LIFETIME_SECONDS);
        double lifeFade = Mth.clamp((LIFETIME_SECONDS - clampedSeconds) / FADE_SECONDS, 0.0, 1.0);
        double feetFade = Mth.clamp((this.position(clampedSeconds).y - this.feetY) / FEET_FADE_DISTANCE, 0.0, 1.0);
        return (int) Math.round(FULL_ALPHA * Math.min(lifeFade, feetFade));
    }

    /**
     * 返回带当前透明度的 ARGB 颜色。
     *
     * @param seconds 从生成时刻经过的秒数
     * @return ARGB 颜色
     */
    public int argb(double seconds) {
        return this.alpha(seconds) << 24 | this.baseColor & 0xFFFFFF;
    }

    /**
     * 返回带正负号的显示文本。
     *
     * @return 显示文本
     */
    public String text() {
        return this.text;
    }
}
