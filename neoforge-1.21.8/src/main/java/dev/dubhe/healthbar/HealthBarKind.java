package dev.dubhe.healthbar;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;

/**
 * 阵营分类与对应的血条填充颜色。
 *
 * <p>判定顺序与规范一致：敌对优先，其次中立，其余为友好。</p>
 */
public enum HealthBarKind {
    /** 友好阵营，例如玩家、村民与普通动物。 */
    FRIENDLY(0xFF3CCB5A),
    /** 中立阵营，例如狼与中立状态下的生物。 */
    NEUTRAL(0xFFE8C33A),
    /** 敌对阵营，例如怪物与敌对 Boss。 */
    HOSTILE(0xFFE04747);

    private final int color;

    HealthBarKind(int color) {
        this.color = color;
    }

    /**
     * 获取血条填充颜色，格式为不透明的 ARGB。
     *
     * @return 填充颜色
     */
    public int getColor() {
        return this.color;
    }

    /**
     * 按规范优先级判定生物的阵营。
     *
     * @param entity 目标生物
     * @return 阵营分类
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
