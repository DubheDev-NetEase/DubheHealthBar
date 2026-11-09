package dev.dubhe.healthbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 客户端 tick 生命值缓存与浮动数字列表管理。
 */
public final class HealthTracker {
    /**
     * 忽略生命值变化的最小差值。
     */
    public static final float MIN_HEALTH_DELTA = 0.01F;
    /**
     * 活动数字的最大保留数量。
     */
    public static final int MAX_NUMBERS = 256;

    private static final Map<UUID, HealthRecord> HEALTH_RECORDS = new HashMap<>();
    private static final List<FloatingNumber> NUMBERS = new ArrayList<>();
    private static final RandomSource RANDOM = RandomSource.create();

    private static ClientLevel level;

    private HealthTracker() {
    }

    /**
     * 在每个客户端 tick 末尾更新生命值缓存并生成浮动数字。
     */
    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel currentLevel = minecraft.level;
        if (currentLevel == null) {
            reset(null);
            return;
        }
        if (currentLevel != level) {
            reset(currentLevel);
        }
        long nowMillis = Util.getMillis();
        NUMBERS.removeIf(number -> number.isExpired(nowMillis));
        Entity cameraEntity = minecraft.getCameraEntity();
        Set<UUID> processed = new HashSet<>();
        Set<UUID> present = new HashSet<>();
        for (Entity entity : currentLevel.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            UUID id = living.getUUID();
            present.add(id);
            if (living == cameraEntity || !living.isAlive() || living.getMaxHealth() <= 0.0F) {
                continue;
            }
            processed.add(id);
            float health = living.getHealth();
            HealthRecord record = HEALTH_RECORDS.get(id);
            double baseY = living.getY() + living.getBbHeight() + 0.25;
            double feetY = living.getY();
            if (record == null) {
                HEALTH_RECORDS.put(id, new HealthRecord(health, living.getX(), living.getZ(), baseY, feetY));
                continue;
            }
            float delta = health - record.health;
            record.health = health;
            record.x = living.getX();
            record.z = living.getZ();
            record.baseY = baseY;
            record.feetY = feetY;
            if (Math.abs(delta) < MIN_HEALTH_DELTA) {
                continue;
            }
            spawnNumber(record, Math.abs(delta), delta > 0.0F);
        }
        Iterator<Map.Entry<UUID, HealthRecord>> iterator = HEALTH_RECORDS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, HealthRecord> entry = iterator.next();
            if (processed.contains(entry.getKey())) {
                continue;
            }
            HealthRecord record = entry.getValue();
            if (present.contains(entry.getKey()) && record.health > 0.0F) {
                spawnNumber(record, record.health, false);
            }
            iterator.remove();
        }
        while (NUMBERS.size() > MAX_NUMBERS) {
            NUMBERS.remove(0);
        }
    }

    /**
     * 返回当前活动的浮动数字列表。
     *
     * @return 浮动数字列表
     */
    public static List<FloatingNumber> numbers() {
        return NUMBERS;
    }

    private static void spawnNumber(HealthRecord record, float amount, boolean increase) {
        if (amount <= 0.0F) {
            return;
        }
        NUMBERS.add(FloatingNumber.spawn(record.x, record.z, record.baseY, record.feetY, amount, increase, RANDOM));
    }

    private static void reset(ClientLevel newLevel) {
        level = newLevel;
        HEALTH_RECORDS.clear();
        NUMBERS.clear();
    }

    private static final class HealthRecord {
        private float health;
        private double x;
        private double z;
        private double baseY;
        private double feetY;

        private HealthRecord(float health, double x, double z, double baseY, double feetY) {
            this.health = health;
            this.x = x;
            this.z = z;
            this.baseY = baseY;
            this.feetY = feetY;
        }
    }
}
