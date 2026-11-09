package dev.dubhe.healthbar.mixin;

import dev.dubhe.healthbar.HealthBarKind;

/**
 * 生物渲染状态上的血条数据访问接口。
 */
public interface HealthBarRenderState {
    /**
     * 返回当前生命值。
     *
     * @return 当前生命值
     */
    float dubhe$getHealth();

    /**
     * 写入当前生命值。
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
     * 写入最大生命值。
     *
     * @param maxHealth 最大生命值
     */
    void dubhe$setMaxHealth(float maxHealth);

    /**
     * 返回阵营分类。
     *
     * @return 阵营分类
     */
    HealthBarKind dubhe$getKind();

    /**
     * 写入阵营分类。
     *
     * @param kind 阵营分类
     */
    void dubhe$setKind(HealthBarKind kind);

    /**
     * 返回是否应显示血条。
     *
     * @return 是否显示血条
     */
    boolean dubhe$isVisible();

    /**
     * 写入是否显示血条。
     *
     * @param visible 是否显示血条
     */
    void dubhe$setVisible(boolean visible);

    /**
     * 返回本次提交是否来自世界渲染。
     *
     * @return 是否来自世界渲染
     */
    boolean dubhe$isWorldRender();

    /**
     * 写入本次提交是否来自世界渲染。
     *
     * @param worldRender 是否来自世界渲染
     */
    void dubhe$setWorldRender(boolean worldRender);
}
