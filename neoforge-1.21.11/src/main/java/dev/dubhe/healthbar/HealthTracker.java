package dev.dubhe.healthbar;

import dev.dubhe.healthbar.render.HealthBarStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 客户端生命值缓存与浮动数字列表。
 *
 * <p>每个客户端 tick 缓存渲染集合中生物的生命值，与上一 tick 比较后生成浮动数字；
 * 实体离开渲染集合时移除对应缓存项，防止 UUID 复用导致误判。</p>
 */
public final class HealthTracker {
    private static final Map<UUID, Float> HEALTH_CACHE = new HashMap<>();
    private static final List<FloatingNumber> NUMBERS = new ArrayList<>();
    private static ClientLevel trackedLevel;

    private HealthTracker() {
    }

    /**
     * 执行一次客户端 tick 的生命值采样与数字生命周期维护。
     */
    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            clear();
            return;
        }
        if (level != trackedLevel) {
            HEALTH_CACHE.clear();
            NUMBERS.clear();
            trackedLevel = level;
        }

        Entity cameraEntity = minecraft.getCameraEntity();
        Set<UUID> present = new HashSet<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            if (living == cameraEntity) {
                continue;
            }
            if (living.getMaxHealth() <= 0.0F) {
                continue;
            }
            UUID uuid = living.getUUID();
            present.add(uuid);
            if (living.isDeadOrDying()) {
                Float previous = HEALTH_CACHE.remove(uuid);
                if (previous != null && previous > 0.01F) {
                    spawnNumber(living, previous, false);
                }
                continue;
            }
            float health = living.getHealth();
            Float previous = HEALTH_CACHE.put(uuid, health);
            if (previous == null) {
                continue;
            }
            float delta = health - previous;
            if (Math.abs(delta) < 0.01F) {
                continue;
            }
            spawnNumber(living, Math.abs(delta), delta > 0.0F);
        }
        HEALTH_CACHE.keySet().retainAll(present);
        NUMBERS.removeIf(FloatingNumber::isExpired);
    }

    /**
     * 返回当前存活浮动数字的只读视图。
     *
     * @return 浮动数字列表
     */
    public static List<FloatingNumber> numbers() {
        return Collections.unmodifiableList(NUMBERS);
    }

    /**
     * 清空生命值缓存与浮动数字列表。
     */
    public static void clear() {
        HEALTH_CACHE.clear();
        NUMBERS.clear();
        trackedLevel = null;
    }

    private static void spawnNumber(LivingEntity entity, float value, boolean gain) {
        double jitterX = (Math.random() - 0.5) * HealthBarStyle.SPAWN_JITTER * 2.0;
        double jitterZ = (Math.random() - 0.5) * HealthBarStyle.SPAWN_JITTER * 2.0;
        double startX = entity.getX() + jitterX;
        double startY = entity.getY() + entity.getBbHeight() + HealthBarStyle.SPAWN_HEIGHT;
        double startZ = entity.getZ() + jitterZ;
        double angle = Math.random() * Math.PI * 2.0;
        double horizontalSpeed = HealthBarStyle.MIN_HORIZONTAL_SPEED
            + Math.random() * (HealthBarStyle.MAX_HORIZONTAL_SPEED - HealthBarStyle.MIN_HORIZONTAL_SPEED);
        double verticalSpeed = HealthBarStyle.MIN_VERTICAL_SPEED
            + Math.random() * (HealthBarStyle.MAX_VERTICAL_SPEED - HealthBarStyle.MIN_VERTICAL_SPEED);
        double velocityX = Math.cos(angle) * horizontalSpeed;
        double velocityZ = Math.sin(angle) * horizontalSpeed;
        NUMBERS.add(
            new FloatingNumber(
                Util.getMillis(),
                startX,
                startY,
                startZ,
                entity.getY(),
                velocityX,
                verticalSpeed,
                velocityZ,
                value,
                gain
            )
        );
        while (NUMBERS.size() > HealthBarStyle.MAX_NUMBERS) {
            NUMBERS.remove(0);
        }
    }
}
