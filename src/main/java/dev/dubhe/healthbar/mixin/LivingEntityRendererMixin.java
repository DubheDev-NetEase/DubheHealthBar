package dev.dubhe.healthbar.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.dubhe.healthbar.HealthBarKind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在 {@link LivingEntityRenderer#extractRenderState} 末尾把血条数据写入渲染状态，并在 submit 头部标记是否为世界渲染。
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At("TAIL")
    )
    private void dubhe$extractRenderState(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        if (!(state instanceof HealthBarRenderState data)) {
            return;
        }
        data.dubhe$setHealth(entity.getHealth());
        data.dubhe$setMaxHealth(entity.getMaxHealth());
        data.dubhe$setKind(HealthBarKind.of(entity));
        data.dubhe$setVisible(entity != Minecraft.getInstance().getCameraEntity());
    }

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
        at = @At("HEAD")
    )
    private void dubhe$markWorldRender(
        LivingEntityRenderState state,
        PoseStack poseStack,
        SubmitNodeCollector collector,
        CameraRenderState camera,
        CallbackInfo ci
    ) {
        if (state instanceof HealthBarRenderState data) {
            data.dubhe$setWorldRender(camera.initialized);
        }
    }
}
