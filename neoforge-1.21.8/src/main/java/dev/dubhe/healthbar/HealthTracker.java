package dev.dubhe.healthbar;

import dev.dubhe.healthbar.render.HealthBarStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 客户端 tick 驱动的生命值缓存与浮动数字列表。
 *
 * <p>生命值以 {@link UUID} 为键缓存；首次看到的生物只记录，不生成数字。
 * 数字动画按真实时间在 GUI 渲染时计算，这里只负责生成与过期清理。
 * 世界切换或断开连接时清空全部数据。</p>
 */
public final class HealthTracker {
    /** 活动数字上限。 */
    public static final int MAX_FLOATING_NUMBERS = 256;
    /** 生命值差值忽略阈值。 */
    private static final float DIFFERENCE_EPSILON = 0.01F;
    /** 数字生成位置的 X/Z 随机偏移半径，格。 */
    private static final double SPAWN_OFFSET = 0.15;
    /** 数字生成位置相对头顶的上移量，格。 */
    private static final double SPAWN_HEIGHT_OFFSET = 0.25;
    /** 水平速度最小值，格/tick。 */
    private static final double MIN_HORIZONTAL_SPEED = 0.035;
    /** 水平速度最大值，格/tick。 */
    private static final double MAX_HORIZONTAL_SPEED = 0.065;
    /** 竖直速度最小值，格/tick。 */
    private static final double MIN_VERTICAL_SPEED = 0.16;
    /** 竖直速度最大值，格/tick。 */
    private static final double MAX_VERTICAL_SPEED = 0.21;

    private static final HealthTracker INSTANCE = new HealthTracker();

    private final Map<UUID, Float> healthCache = new HashMap<>();
    private final Set<UUID> visibleEntities = new HashSet<>();
    private final List<FloatingNumber> numbers = new ArrayList<>();
    private final List<FloatingNumber> numbersView = Collections.unmodifiableList(this.numbers);
    private final RandomSource random = RandomSource.create();
    @Nullable
    private ClientLevel trackedLevel;

    private HealthTracker() {
    }

    /**
     * 获取全局追踪器实例。
     *
     * @return 追踪器实例
     */
    public static HealthTracker getInstance() {
        return INSTANCE;
    }

    /**
     * 每个客户端 tick 调用：清理过期数字、扫描渲染生物并生成新的变化数字。
     */
    public void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            this.reset();
            return;
        }
        if (level != this.trackedLevel) {
            this.reset();
            this.trackedLevel = level;
        }

        this.removeExpiredNumbers();

        Entity cameraEntity = minecraft.getCameraEntity();
        this.visibleEntities.clear();
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }
            if (livingEntity == cameraEntity) {
                continue;
            }
            UUID uuid = livingEntity.getUUID();
            this.visibleEntities.add(uuid);
            float maxHealth = livingEntity.getMaxHealth();
            if (livingEntity.isDeadOrDying()) {
                this.handleDeath(livingEntity, uuid);
                continue;
            }
            if (maxHealth <= 0.0F) {
                this.healthCache.remove(uuid);
                continue;
            }

            Float previousHealth = this.healthCache.put(uuid, livingEntity.getHealth());
            if (previousHealth == null) {
                continue;
            }
            float difference = livingEntity.getHealth() - previousHealth;
            if (Math.abs(difference) < DIFFERENCE_EPSILON) {
                continue;
            }
            this.spawnNumber(livingEntity, difference);
        }
        this.healthCache.keySet().retainAll(this.visibleEntities);
    }

    /**
     * 获取当前活动的数字列表，列表只读。
     *
     * @return 只读数字列表
     */
    public List<FloatingNumber> getNumbers() {
        return this.numbersView;
    }

    private void removeExpiredNumbers() {
        for (Iterator<FloatingNumber> iterator = this.numbers.iterator(); iterator.hasNext(); ) {
            if (iterator.next().isExpired()) {
                iterator.remove();
            }
        }
    }

    private void handleDeath(LivingEntity entity, UUID uuid) {
        Float previousHealth = this.healthCache.remove(uuid);
        if (previousHealth == null || Math.abs(previousHealth) < DIFFERENCE_EPSILON) {
            return;
        }
        this.spawnNumber(entity, -previousHealth);
    }

    private void spawnNumber(LivingEntity entity, float difference) {
        double x = entity.getX() + (this.random.nextDouble() - 0.5) * SPAWN_OFFSET * 2.0;
        double y = entity.getY() + entity.getBbHeight() + SPAWN_HEIGHT_OFFSET;
        double z = entity.getZ() + (this.random.nextDouble() - 0.5) * SPAWN_OFFSET * 2.0;
        double direction = this.random.nextDouble() * Math.PI * 2.0;
        double horizontalSpeed = MIN_HORIZONTAL_SPEED + this.random.nextDouble() * (MAX_HORIZONTAL_SPEED - MIN_HORIZONTAL_SPEED);
        double verticalSpeed = MIN_VERTICAL_SPEED + this.random.nextDouble() * (MAX_VERTICAL_SPEED - MIN_VERTICAL_SPEED);
        double velocityX = Math.cos(direction) * horizontalSpeed;
        double velocityZ = Math.sin(direction) * horizontalSpeed;
        String text = (difference >= 0.0F ? "+" : "-") + HealthBarStyle.formatNumber(Math.abs(difference));
        int color = difference >= 0.0F ? HealthBarStyle.INCREASE_COLOR : HealthBarStyle.DECREASE_COLOR;
        FloatingNumber number = new FloatingNumber(
                x,
                y,
                z,
                velocityX,
                verticalSpeed,
                velocityZ,
                entity.getY(),
                text,
                color);
        if (this.numbers.size() >= MAX_FLOATING_NUMBERS) {
            this.numbers.remove(0);
        }
        this.numbers.add(number);
    }

    private void reset() {
        this.healthCache.clear();
        this.visibleEntities.clear();
        this.numbers.clear();
        this.trackedLevel = null;
    }
}
