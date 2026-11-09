package dev.dubhe.healthbar.mixin;

import dev.dubhe.healthbar.render.HealthBarRenderState;
import dev.dubhe.healthbar.HealthBarKind;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * 为 {@link LivingEntityRenderState} 附加血条渲染数据。
 *
 * <p>1.21.8 使用即时渲染 API，渲染事件不再直接提供实体，
 * 因此需要在 {@code extractRenderState} 阶段把生命值等数据写入渲染状态。</p>
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
}
