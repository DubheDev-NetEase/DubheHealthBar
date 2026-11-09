package dev.dubhe.healthbar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.dubhe.healthbar.HealthBarKind;
import dev.dubhe.healthbar.mixin.HealthBarRenderState;
import dev.dubhe.healthbar.render.HealthBarRenderTypes;
import dev.dubhe.healthbar.render.HealthBarStyle;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

/**
 * 在生物渲染后提交世界空间血条几何与生命值文本。
 */
public final class HealthBarRenderer {
    private HealthBarRenderer() {
    }

    /**
     * 处理生物渲染完成事件，向提交节点收集器写入血条。
     *
     * @param event 生物渲染完成事件
     */
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?, ?> event) {
        LivingEntityRenderState state = event.getRenderState();
        if (!(state instanceof HealthBarRenderState data) || !data.dubhe$isVisible() || !data.dubhe$isWorldRender()) {
            return;
        }
        float maxHealth = data.dubhe$getMaxHealth();
        if (maxHealth <= 0.0F) {
            return;
        }
        RenderType renderType = HealthBarRenderTypes.healthBar();
        if (renderType == null) {
            return;
        }
        float health = Mth.clamp(data.dubhe$getHealth(), 0.0F, maxHealth);
        HealthBarKind kind = data.dubhe$getKind();
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        String text = HealthBarStyle.buildText(health, maxHealth);
        float fillProgress = HealthBarStyle.healthRatio(health, maxHealth);
        Vec3 attachment = state.nameTagAttachment;
        double baseX = attachment != null ? attachment.x : 0.0;
        double baseY = attachment != null ? attachment.y : state.boundingBoxHeight + HealthBarStyle.FALLBACK_ATTACHMENT_HEIGHT;
        double baseZ = attachment != null ? attachment.z : 0.0;
        double offsetY = state.nameTag != null ? HealthBarStyle.NAME_TAG_OFFSET : HealthBarStyle.NO_NAME_TAG_OFFSET;
        Camera camera = minecraft.gameRenderer.getMainCamera();
        PoseStack poseStack = event.getPoseStack();
        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        int red = kind.color() >> 16 & 0xFF;
        int green = kind.color() >> 8 & 0xFF;
        int blue = kind.color() & 0xFF;
        poseStack.pushPose();
        poseStack.translate(baseX, baseY + offsetY, baseZ);
        poseStack.mulPose(camera.rotation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(-HealthBarStyle.SCALE, -HealthBarStyle.SCALE, HealthBarStyle.SCALE);
        poseStack.translate(HealthBarStyle.barCenterOffset(font, text), 0.0F, 0.0F);
        collector.submitCustomGeometry(
                poseStack, renderType, (pose, buffer) -> submitBarGeometry(pose, buffer, fillProgress, red, green, blue));
        collector.submitText(
                poseStack,
                HealthBarStyle.textOffsetX(),
                HealthBarStyle.TEXT_TOP,
                FormattedCharSequence.forward(text, Style.EMPTY),
                true,
                Font.DisplayMode.NORMAL,
                LightCoordsUtil.FULL_BRIGHT,
                HealthBarStyle.TEXT_COLOR,
                0,
                0);
        poseStack.popPose();
    }

    private static void submitBarGeometry(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            float fillProgress,
            int red,
            int green,
            int blue) {
        float halfWidth = HealthBarStyle.BAR_WIDTH / 2.0F;
        float halfHeight = HealthBarStyle.BAR_HEIGHT / 2.0F;
        int fillAlpha = Math.round(fillProgress * 255.0F);
        addVertex(pose, buffer, -halfWidth, -halfHeight, fillAlpha, red, green, blue);
        addVertex(pose, buffer, -halfWidth, halfHeight, fillAlpha, red, green, blue);
        addVertex(pose, buffer, halfWidth, halfHeight, fillAlpha, red, green, blue);
        addVertex(pose, buffer, halfWidth, -halfHeight, fillAlpha, red, green, blue);
    }

    private static void addVertex(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            float x,
            float y,
            int fillAlpha,
            int red,
            int green,
            int blue) {
        buffer.addVertex(pose, x, y, 0.0F).setColor(red, green, blue, fillAlpha).setUv(x, y);
    }
}
