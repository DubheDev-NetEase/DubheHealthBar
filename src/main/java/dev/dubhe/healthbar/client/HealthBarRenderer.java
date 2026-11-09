package dev.dubhe.healthbar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.dubhe.healthbar.DubheHealthBar;
import dev.dubhe.healthbar.HealthBarKind;
import dev.dubhe.healthbar.render.HealthBarRenderTypes;
import dev.dubhe.healthbar.render.HealthBarStyle;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 世界空间血条与右侧生命值文本的渲染。
 */
@Mod.EventBusSubscriber(modid = DubheHealthBar.MOD_ID, value = Dist.CLIENT)
public final class HealthBarRenderer {
    private HealthBarRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) {
            return;
        }
        if (entity == minecraft.getCameraEntity() || entity.isDeadOrDying() || entity.getMaxHealth() <= 0.0F) {
            return;
        }
        RenderType renderType = HealthBarRenderTypes.getRenderType();
        if (renderType == null) {
            return;
        }
        float maxHealth = entity.getMaxHealth();
        float health = HealthBarStyle.clampHealth(entity.getHealth(), maxHealth);
        int barColor = HealthBarKind.classify(entity).color();
        String text = HealthBarStyle.formatHealth(health) + "/" + HealthBarStyle.formatHealth(maxHealth);
        Font font = minecraft.font;
        float textWidth = font.width(text);
        float barCenterX = -(HealthBarStyle.TEXT_GAP + textWidth) / 2.0F;
        float halfWidth = HealthBarStyle.BAR_WIDTH / 2.0F;
        float halfHeight = HealthBarStyle.BAR_HEIGHT / 2.0F;
        int fillAlpha = Math.round(HealthBarStyle.fillRatio(health, maxHealth) * 255.0F);

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        double offset = HealthBarStyle.barOffset(entity.hasCustomName());
        poseStack.translate(barCenterX, (double) entity.getBbHeight() + HealthBarStyle.ATTACHMENT_HEIGHT + offset, 0.0);
        Camera camera = minecraft.gameRenderer.getMainCamera();
        poseStack.mulPose(camera.rotation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(-HealthBarStyle.SCALE, -HealthBarStyle.SCALE, HealthBarStyle.SCALE);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(renderType);
        vertex(consumer, matrix, -halfWidth, -halfHeight, fillAlpha, barColor);
        vertex(consumer, matrix, -halfWidth, halfHeight, fillAlpha, barColor);
        vertex(consumer, matrix, halfWidth, halfHeight, fillAlpha, barColor);
        vertex(consumer, matrix, halfWidth, -halfHeight, fillAlpha, barColor);
        font.drawInBatch(text, halfWidth + HealthBarStyle.TEXT_GAP, -4.0F, HealthBarStyle.TEXT_COLOR, false, matrix,
                event.getMultiBufferSource(), Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, int fillAlpha, int color) {
        consumer.vertex(matrix, x, y, 0.0F)
                .color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, fillAlpha)
                .uv(x, y)
                .endVertex();
    }
}
