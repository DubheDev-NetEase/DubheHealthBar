package dev.dubhe.healthbar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.dubhe.healthbar.HealthBarKind;
import dev.dubhe.healthbar.mixin.HealthBarRenderState;
import dev.dubhe.healthbar.render.HealthBarRenderTypes;
import dev.dubhe.healthbar.render.HealthBarStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Quaternionf;

/**
 * 世界空间血条与生命值文本渲染。
 *
 * <p>在 {@link RenderLivingEvent.Post} 中通过 {@link SubmitNodeCollector} 提交自定义几何与文本，
 * 由渲染管线统一批次绘制，不直接操作渲染系统矩阵栈。</p>
 */
public final class HealthBarRenderer {
    private HealthBarRenderer() {
    }

    /**
     * 在生物渲染结束后提交血条几何与生命值文本。
     *
     * @param event 生物渲染事件
     */
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?, ?> event) {
        if (!(event.getRenderState() instanceof HealthBarRenderState data)) {
            return;
        }
        LivingEntityRenderState state = event.getRenderState();
        if (!data.dubhe$isWorldRender() || !data.dubhe$isVisible() || data.dubhe$getMaxHealth() <= 0.0F) {
            return;
        }
        RenderType renderType = HealthBarRenderTypes.bar();
        if (renderType == null) {
            return;
        }

        float maxHealth = data.dubhe$getMaxHealth();
        float health = Mth.clamp(data.dubhe$getHealth(), 0.0F, maxHealth);
        String text = HealthBarStyle.formatHealth(health, maxHealth);
        Font font = Minecraft.getInstance().font;
        float totalWidth = HealthBarStyle.BAR_WIDTH + HealthBarStyle.TEXT_GAP + font.width(text);
        float barLeft = -totalWidth / 2.0F;
        float barCenter = barLeft + HealthBarStyle.BAR_WIDTH / 2.0F;
        float textX = barLeft + HealthBarStyle.BAR_WIDTH + HealthBarStyle.TEXT_GAP;
        float fillProgress = HealthBarStyle.fillProgress(health, maxHealth);
        int color = (data.dubhe$getKind() == null ? HealthBarKind.FRIENDLY : data.dubhe$getKind()).color();

        Vec3 attachment = state.nameTagAttachment;
        double attachX = attachment == null ? 0.0 : attachment.x;
        double attachY = attachment == null ? state.boundingBoxHeight + HealthBarStyle.FALLBACK_HEIGHT_OFFSET : attachment.y;
        double attachZ = attachment == null ? 0.0 : attachment.z;
        double offsetY = state.nameTag != null ? HealthBarStyle.NAME_TAG_BAR_OFFSET : HealthBarStyle.DEFAULT_BAR_OFFSET;

        Quaternionf cameraRotation = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        PoseStack poseStack = event.getPoseStack();
        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        poseStack.pushPose();
        try {
            poseStack.translate(attachX, attachY + offsetY, attachZ);
            poseStack.mulPose(cameraRotation);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(-HealthBarStyle.SCALE, -HealthBarStyle.SCALE, HealthBarStyle.SCALE);

            poseStack.pushPose();
            try {
                poseStack.translate(barCenter, 0.0F, 0.0F);
                collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> submitBar(buffer, pose, fillProgress, color));
            } finally {
                poseStack.popPose();
            }

            collector.submitText(
                poseStack,
                textX,
                HealthBarStyle.TEXT_TOP,
                FormattedCharSequence.forward(text, Style.EMPTY),
                true,
                Font.DisplayMode.NORMAL,
                LightTexture.FULL_BRIGHT,
                HealthBarStyle.TEXT_COLOR,
                0,
                0
            );
        } finally {
            poseStack.popPose();
        }
    }

    private static void submitBar(VertexConsumer buffer, PoseStack.Pose pose, float fillProgress, int color) {
        float halfWidth = HealthBarStyle.BAR_WIDTH / 2.0F;
        float halfHeight = HealthBarStyle.BAR_HEIGHT / 2.0F;
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        int fillAlpha = Mth.clamp(Math.round(fillProgress * 255.0F), 0, 255);
        submitVertex(buffer, pose, -halfWidth, -halfHeight, red, green, blue, fillAlpha);
        submitVertex(buffer, pose, halfWidth, -halfHeight, red, green, blue, fillAlpha);
        submitVertex(buffer, pose, halfWidth, halfHeight, red, green, blue, fillAlpha);
        submitVertex(buffer, pose, -halfWidth, halfHeight, red, green, blue, fillAlpha);
    }

    private static void submitVertex(
        VertexConsumer buffer,
        PoseStack.Pose pose,
        float localX,
        float localY,
        int red,
        int green,
        int blue,
        int fillAlpha
    ) {
        buffer.addVertex(pose, localX, localY, 0.0F).setColor(red, green, blue, fillAlpha).setUv(localX, localY);
    }
}
