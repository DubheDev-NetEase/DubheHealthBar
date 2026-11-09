package dev.dubhe.healthbar.render;

import dev.dubhe.healthbar.HealthBarKind;

/**
 * 附加到 {@code LivingEntityRenderState} 的血条数据接口。
 */
public interface HealthBarRenderState {
    /**
     * 返回当前生命值。
     *
     * @return 当前生命值
     */
    float dubhe$getHealth();

    /**
     * 设置当前生命值。
     *
     * @param health 当前生命值
     */
    void dubhe$setHealth(float health);

    /**
     * 返回最大生命值。
     *
     * @return 最大生命值
     */
    float dubhe$getMaxHealth();

    /**
     * 设置最大生命值。
     *
     * @param maxHealth 最大生命值
     */
    void dubhe$setMaxHealth(float maxHealth);

    /**
     * 返回阵营。
     *
     * @return 阵营
     */
    HealthBarKind dubhe$getKind();

    /**
     * 设置阵营。
     *
     * @param kind 阵营
     */
    void dubhe$setKind(HealthBarKind kind);

    /**
     * 返回是否应显示血条。
     *
     * @return 是否显示
     */
    boolean dubhe$isVisible();

    /**
     * 设置是否应显示血条。
     *
     * @param visible 是否显示
     */
    void dubhe$setVisible(boolean visible);

    /**
     * 返回当前是否处于世界渲染。
     *
     * @return 是否世界渲染
     */
    boolean dubhe$isWorldRender();

    /**
     * 设置当前是否处于世界渲染。
     *
     * @param worldRender 是否世界渲染
     */
    void dubhe$setWorldRender(boolean worldRender);
}
