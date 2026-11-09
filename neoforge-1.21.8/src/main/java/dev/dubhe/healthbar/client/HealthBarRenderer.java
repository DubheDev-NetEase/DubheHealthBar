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
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

/**
 * 世界空间血条与右侧生命值文本渲染。
 *
 * <p>1.21.8 为即时渲染 API：{@link RenderLivingEvent.Post} 提供渲染状态、PoseStack 与
 * MultiBufferSource。顶点由 {@link PoseStack} 矩阵预先变换到世界空间，血条与文本都提交到
 * 事件提供的同一个 {@link MultiBufferSource}，由原版在实体批次结束后统一绘制。</p>
 *
 * <p>由于 1.21.8 没有逐绘制模型矩阵，着色器无法再从 {@code Position} 取到局部像素坐标：
 * 血条顶点的 UV0 携带局部像素坐标，Color 的 alpha 通道携带血量比例，Color 的 RGB 仍为阵营颜色，
 * 片元着色器据此完成圆角、抗锯齿与按比例填充。</p>
 */
public final class HealthBarRenderer {
    private HealthBarRenderer() {
    }

    /**
     * 渲染事件入口：为可见生物绘制血条与生命值文本。
     *
     * @param event 生物渲染后事件
     */
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?, ?> event) {
        if (Minecraft.getInstance().screen != null) {
            return;
        }
        LivingEntityRenderState renderState = event.getRenderState();
        if (!(renderState instanceof HealthBarRenderState data)) {
            return;
        }
        if (!data.dubhe$isVisible()) {
            return;
        }
        RenderType renderType = HealthBarRenderTypes.getRenderType();
        if (renderType == null) {
            return;
        }
        float maxHealth = data.dubhe$getMaxHealth();
        if (maxHealth <= 0.0F) {
            return;
        }
        float health = Mth.clamp(data.dubhe$getHealth(), 0.0F, maxHealth);
        float fillProgress = health / maxHealth;
        HealthBarKind kind = data.dubhe$getKind();
        Font font = Minecraft.getInstance().font;
        String text = HealthBarStyle.healthText(health, maxHealth);
        float totalWidth = HealthBarStyle.BAR_WIDTH + HealthBarStyle.TEXT_GAP + font.width(text);
        float left = -totalWidth / 2.0F;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        Vec3 attachment = renderState.nameTagAttachment;
        if (attachment == null) {
            attachment = new Vec3(0.0, renderState.boundingBoxHeight + 0.5, 0.0);
        }
        float verticalOffset = renderState.nameTag != null
                ? HealthBarStyle.OFFSET_WITH_NAME_TAG
                : HealthBarStyle.OFFSET_WITHOUT_NAME_TAG;
        poseStack.translate(attachment.x, attachment.y + verticalOffset, attachment.z);
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        poseStack.mulPose(camera.rotation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(-HealthBarStyle.SCALE, -HealthBarStyle.SCALE, HealthBarStyle.SCALE);
        Matrix4f matrix = poseStack.last().pose();
        MultiBufferSource bufferSource = event.getMultiBufferSource();

        addBarVertices(bufferSource, renderType, matrix, kind, fillProgress);
        font.drawInBatch(
                text,
                left + HealthBarStyle.BAR_WIDTH + HealthBarStyle.TEXT_GAP,
                HealthBarStyle.TEXT_TOP,
                HealthBarStyle.TEXT_COLOR,
                true,
                matrix,
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    private static void addBarVertices(
            MultiBufferSource bufferSource,
            RenderType renderType,
            Matrix4f matrix,
            HealthBarKind kind,
            float fillProgress) {
        int rgb = kind.getColor();
        int packedFill = Mth.clamp(Math.round(fillProgress * 255.0F), 0, 255);
        int color = ARGB.color(packedFill, rgb);
        float halfWidth = HealthBarStyle.BAR_WIDTH / 2.0F;
        float halfHeight = HealthBarStyle.BAR_HEIGHT / 2.0F;
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        consumer.addVertex(matrix, -halfWidth, -halfHeight, 0.0F)
                .setColor(color)
                .setUv(-halfWidth, -halfHeight);
        consumer.addVertex(matrix, -halfWidth, halfHeight, 0.0F)
                .setColor(color)
                .setUv(-halfWidth, halfHeight);
        consumer.addVertex(matrix, halfWidth, halfHeight, 0.0F)
                .setColor(color)
                .setUv(halfWidth, halfHeight);
        consumer.addVertex(matrix, halfWidth, -halfHeight, 0.0F)
                .setColor(color)
                .setUv(halfWidth, -halfHeight);
    }
}
