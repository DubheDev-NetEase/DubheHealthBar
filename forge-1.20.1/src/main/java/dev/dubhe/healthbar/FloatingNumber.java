package dev.dubhe.healthbar;

/**
 * 单条生命值变化数字的世界坐标动画数据。
 */
public final class FloatingNumber {
    private static final double LIFETIME_SECONDS = 1.1;
    private static final double FADE_START_SECONDS = LIFETIME_SECONDS * 0.75;
    private static final double GRAVITY = 0.02;
    private static final double FOOT_FADE_DISTANCE = 0.55;

    private final double startX;
    private final double startY;
    private final double startZ;
    private final double velocityX;
    private final double velocityY;
    private final double velocityZ;
    private final double feetY;
    private final long spawnMillis;
    private final String text;
    private final int color;

    public FloatingNumber(double startX, double startY, double startZ, double velocityX, double velocityY, double velocityZ,
            double feetY, long spawnMillis, String text, int color) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.feetY = feetY;
        this.spawnMillis = spawnMillis;
        this.text = text;
        this.color = color;
    }

    /**
     * 把经过的毫秒数换算为动画 tick 数。
     *
     * @param nowMillis 当前毫秒时间
     * @return tick 数
     */
    public double tick(long nowMillis) {
        return (nowMillis - this.spawnMillis) / 1000.0 * 20.0;
    }

    public double x(long nowMillis) {
        return this.startX + this.velocityX * this.tick(nowMillis);
    }

    public double y(long nowMillis) {
        double tick = this.tick(nowMillis);
        return this.startY + this.velocityY * tick - 0.5 * GRAVITY * tick * tick;
    }

    public double z(long nowMillis) {
        return this.startZ + this.velocityZ * this.tick(nowMillis);
    }

    /**
     * 计算生命周期淡出与脚部淡出两者中较小的 alpha。
     *
     * @param nowMillis 当前毫秒时间
     * @return 0 到 1 之间的 alpha
     */
    public float alpha(long nowMillis) {
        double age = (nowMillis - this.spawnMillis) / 1000.0;
        double lifeFade = age <= FADE_START_SECONDS
                ? 1.0
                : 1.0 - (age - FADE_START_SECONDS) / (LIFETIME_SECONDS - FADE_START_SECONDS);
        double footFade = (this.y(nowMillis) - this.feetY) / FOOT_FADE_DISTANCE;
        double alpha = Math.min(lifeFade, footFade);
        return (float) Math.max(0.0, Math.min(1.0, alpha));
    }

    public boolean isExpired(long nowMillis) {
        return nowMillis - this.spawnMillis >= (long) (LIFETIME_SECONDS * 1000.0);
    }

    public String text() {
        return this.text;
    }

    public int color() {
        return this.color;
    }
}
