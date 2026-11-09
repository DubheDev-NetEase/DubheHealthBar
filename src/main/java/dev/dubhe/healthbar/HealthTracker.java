package dev.dubhe.healthbar;

import dev.dubhe.healthbar.render.HealthBarStyle;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 客户端生命值缓存与生命值变化数字列表。
 */
public final class HealthTracker {
    public static final int MAX_NUMBERS = 256;
    private static final float MIN_HEALTH_DELTA = 0.01F;
    private static final Map<UUID, Float> HEALTH = new HashMap<>();
    private static final List<FloatingNumber> NUMBERS = new ArrayList<>();
    private static ClientLevel level;

    private HealthTracker() {
    }

    /**
     * 每个客户端 tick 比较生命值变化并生成数字。
     */
    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            clear();
            return;
        }
        if (level != minecraft.level) {
            clear();
            level = minecraft.level;
        }
        long now = Util.getMillis();
        NUMBERS.removeIf(number -> number.isExpired(now));
        Entity cameraEntity = minecraft.getCameraEntity();
        Set<UUID> seen = new HashSet<>();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity livingEntity) || livingEntity == cameraEntity) {
                continue;
            }
            if (livingEntity.getMaxHealth() <= 0.0F) {
                continue;
            }
            UUID uuid = livingEntity.getUUID();
            Float previous = HEALTH.get(uuid);
            if (livingEntity.isDeadOrDying() || livingEntity.isRemoved()) {
                if (previous != null && previous > MIN_HEALTH_DELTA) {
                    spawn(livingEntity, -previous);
                }
                HEALTH.remove(uuid);
                continue;
            }
            seen.add(uuid);
            float current = livingEntity.getHealth();
            if (previous == null) {
                HEALTH.put(uuid, current);
                continue;
            }
            HEALTH.put(uuid, current);
            float delta = current - previous;
            if (Math.abs(delta) < MIN_HEALTH_DELTA) {
                continue;
            }
            spawn(livingEntity, delta);
        }
        HEALTH.keySet().removeIf(uuid -> !seen.contains(uuid));
    }

    private static void spawn(LivingEntity entity, float delta) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double startX = entity.getX() + random.nextDouble() * 0.3 - 0.15;
        double startZ = entity.getZ() + random.nextDouble() * 0.3 - 0.15;
        double startY = entity.getY() + entity.getBbHeight() + 0.25;
        double speed = 0.035 + random.nextDouble() * 0.03;
        double angle = random.nextDouble() * Math.PI * 2.0;
        double velocityX = Math.cos(angle) * speed;
        double velocityZ = Math.sin(angle) * speed;
        double velocityY = 0.16 + random.nextDouble() * 0.05;
        String sign = delta > 0.0F ? "+" : "-";
        String text = sign + HealthBarStyle.formatHealth(Math.abs(delta));
        int color = delta > 0.0F ? 0xFF55FF55 : 0xFFFF5555;
        long now = Util.getMillis();
        NUMBERS.add(new FloatingNumber(startX, startY, startZ, velocityX, velocityY, velocityZ, entity.getY(), now, text, color));
        if (NUMBERS.size() > MAX_NUMBERS) {
            NUMBERS.remove(0);
        }
    }

    public static List<FloatingNumber> numbers() {
        return NUMBERS;
    }

    /**
     * 清空生命值缓存与数字列表。
     */
    public static void clear() {
        HEALTH.clear();
        NUMBERS.clear();
        level = null;
    }
}
