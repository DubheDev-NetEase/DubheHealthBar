package dev.dubhe.healthbar;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;

/**
 * 血条填充颜色对应的阵营。
 */
public enum HealthBarKind {
    FRIENDLY(0xFF3CCB5A),
    NEUTRAL(0xFFE8C33A),
    HOSTILE(0xFFE04747);

    private final int color;

    HealthBarKind(int color) {
        this.color = color;
    }

    public int color() {
        return this.color;
    }

    /**
     * 按敌对、中立、友好的优先级判定实体阵营。
     *
     * @param entity 待判定的生物
     * @return 阵营
     */
    public static HealthBarKind classify(LivingEntity entity) {
        if (entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER) {
            return HOSTILE;
        }
        if (entity instanceof NeutralMob) {
            return NEUTRAL;
        }
        return FRIENDLY;
    }
}
