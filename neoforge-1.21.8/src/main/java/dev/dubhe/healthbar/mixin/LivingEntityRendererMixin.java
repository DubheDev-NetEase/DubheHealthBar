package dev.dubhe.healthbar.mixin;

import dev.dubhe.healthbar.HealthBarKind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在 {@link LivingEntityRenderer#extractRenderState} 末尾绑定血条数据。
 *
 * <p>渲染事件阶段只能拿到 {@link LivingEntityRenderState}，
 * 因此生命值、最大生命值、阵营与可见标记都在这里写入。</p>
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void dubhe$captureHealthBarState(
            LivingEntity entity,
            LivingEntityRenderState renderState,
            float partialTick,
            CallbackInfo callbackInfo) {
        if (!(renderState instanceof HealthBarRenderState data)) {
            return;
        }
        data.dubhe$setHealth(entity.getHealth());
        data.dubhe$setMaxHealth(entity.getMaxHealth());
        data.dubhe$setKind(HealthBarKind.of(entity));
        data.dubhe$setVisible(entity != Minecraft.getInstance().getCameraEntity());
    }
}
