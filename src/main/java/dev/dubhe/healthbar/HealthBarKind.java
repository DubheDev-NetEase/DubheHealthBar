package dev.dubhe.healthbar;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;

/**
 * 血条阵营分类及其填充颜色。
 */
public enum HealthBarKind {
    /**
     * 友好阵营，填充绿色。
     */
    FRIENDLY(0xFF3CCB5A),
    /**
     * 中立阵营，填充黄色。
     */
    NEUTRAL(0xFFE8C33A),
    /**
     * 敌对阵营，填充红色。
     */
    HOSTILE(0xFFE04747);

    private final int color;

    HealthBarKind(int color) {
        this.color = color;
    }

    /**
     * 返回该阵营的 ARGB 颜色。
     *
     * @return 阵营颜色
     */
    public int color() {
        return this.color;
    }

    /**
     * 按敌对、中立、友好的优先级判定生物阵营。
     *
     * @param entity 待判定的生物
     * @return 生物所属阵营
     */
    public static HealthBarKind of(LivingEntity entity) {
        if (entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER) {
            return HOSTILE;
        }
        if (entity instanceof NeutralMob) {
            return NEUTRAL;
        }
        return FRIENDLY;
    }
}
