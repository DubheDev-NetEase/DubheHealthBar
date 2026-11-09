package dev.dubhe.healthbar.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.dubhe.healthbar.HealthBarKind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在生物渲染状态提取末尾写入血条所需数据。
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"))
    private void dubhe$bindHealthBarData(
            LivingEntity entity,
            LivingEntityRenderState state,
            float partialTick,
            CallbackInfo callbackInfo) {
        if (!(state instanceof HealthBarRenderState data)) {
            return;
        }
        float maxHealth = entity.getMaxHealth();
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        data.dubhe$setHealth(entity.getHealth());
        data.dubhe$setMaxHealth(maxHealth);
        data.dubhe$setKind(HealthBarKind.of(entity));
        data.dubhe$setVisible(entity != cameraEntity && entity.isAlive() && maxHealth > 0.0F);
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"))
    private void dubhe$markWorldRender(
            LivingEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera,
            CallbackInfo callbackInfo) {
        if (state instanceof HealthBarRenderState data) {
            data.dubhe$setWorldRender(camera.initialized);
        }
    }
}
