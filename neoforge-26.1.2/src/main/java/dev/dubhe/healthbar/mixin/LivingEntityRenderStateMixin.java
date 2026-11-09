package dev.dubhe.healthbar.mixin;

import dev.dubhe.healthbar.HealthBarKind;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * 为生物渲染状态附加血条数据字段。
 */
@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements HealthBarRenderState {
    @Unique
    private float dubhe$health;
    @Unique
    private float dubhe$maxHealth;
    @Unique
    private HealthBarKind dubhe$kind = HealthBarKind.FRIENDLY;
    @Unique
    private boolean dubhe$visible;
    @Unique
    private boolean dubhe$worldRender;

    @Override
    public float dubhe$getHealth() {
        return this.dubhe$health;
    }

    @Override
    public void dubhe$setHealth(float health) {
        this.dubhe$health = health;
    }

    @Override
    public float dubhe$getMaxHealth() {
        return this.dubhe$maxHealth;
    }

    @Override
    public void dubhe$setMaxHealth(float maxHealth) {
        this.dubhe$maxHealth = maxHealth;
    }

    @Override
    public HealthBarKind dubhe$getKind() {
        return this.dubhe$kind;
    }

    @Override
    public void dubhe$setKind(HealthBarKind kind) {
        this.dubhe$kind = kind;
    }

    @Override
    public boolean dubhe$isVisible() {
        return this.dubhe$visible;
    }

    @Override
    public void dubhe$setVisible(boolean visible) {
        this.dubhe$visible = visible;
    }

    @Override
    public boolean dubhe$isWorldRender() {
        return this.dubhe$worldRender;
    }

    @Override
    public void dubhe$setWorldRender(boolean worldRender) {
        this.dubhe$worldRender = worldRender;
    }
}
