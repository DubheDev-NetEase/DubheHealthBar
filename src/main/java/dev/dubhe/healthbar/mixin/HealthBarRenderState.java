package dev.dubhe.healthbar.mixin;

import dev.dubhe.healthbar.HealthBarKind;

/**
 * 由 {@code LivingEntityRenderState} 的 mixin 实现的血条渲染数据接口。
 *
 * <p>所有方法使用 {@code dubhe$} 前缀，避免与其它模组冲突。</p>
 */
public interface HealthBarRenderState {
    /**
     * 获取当前生命值。
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
     * 获取最大生命值。
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
     * 获取阵营分类。
     *
     * @return 阵营分类
     */
    HealthBarKind dubhe$getKind();

    /**
     * 设置阵营分类。
     *
     * @param kind 阵营分类
     */
    void dubhe$setKind(HealthBarKind kind);

    /**
     * 判断该渲染状态是否应显示血条。
     *
     * @return 应显示时返回 {@code true}
     */
    boolean dubhe$isVisible();

    /**
     * 设置是否应显示血条。
     *
     * @param visible 是否显示
     */
    void dubhe$setVisible(boolean visible);
}
