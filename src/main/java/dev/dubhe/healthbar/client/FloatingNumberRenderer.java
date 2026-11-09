package dev.dubhe.healthbar.client;

import dev.dubhe.healthbar.FloatingNumber;
import dev.dubhe.healthbar.HealthTracker;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

/**
 * GUI 层的生命值变化数字渲染与世界坐标投影。
 */
public final class FloatingNumberRenderer {
    private static final float SCREEN_MARGIN = 32.0F;
    private static final float MIN_ALPHA = 1.0F / 255.0F;
    private static final Matrix4f projectionMatrix = new Matrix4f();
    private static final Quaternionf cameraRotation = new Quaternionf();
    private static double cameraX;
    private static double cameraY;
    private static double cameraZ;
    private static boolean projectionValid;

    private FloatingNumberRenderer() {
    }

    /**
     * 缓存当帧世界渲染使用的投影矩阵与相机状态。
     *
     * @param event 关卡渲染阶段事件
     */
    public static void captureProjection(RenderLevelStageEvent event) {
        projectionMatrix.set(event.getProjectionMatrix());
        Camera camera = event.getCamera();
        Vec3 position = camera.getPosition();
        cameraX = position.x;
        cameraY = position.y;
        cameraZ = position.z;
        cameraRotation.set(camera.rotation());
        projectionValid = true;
    }

    /**
     * 把数字按世界坐标投影到 GUI 坐标并绘制。
     *
     * @param guiGraphics GUI 绘制上下文
     */
    public static void render(GuiGraphics guiGraphics) {
        long now = Util.getMillis();
        List<FloatingNumber> numbers = HealthTracker.numbers();
        numbers.removeIf(number -> number.isExpired(now));
        Minecraft minecraft = Minecraft.getInstance();
        if (!projectionValid || minecraft.level == null || minecraft.options.hideGui || numbers.isEmpty()) {
            return;
        }
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        Font font = minecraft.font;
        Quaternionf inverseRotation = new Quaternionf(cameraRotation).conjugate();
        Matrix4f viewMatrix = new Matrix4f().rotation(inverseRotation);
        Vector3f lookVector = new Vector3f(0.0F, 0.0F, -1.0F).rotate(cameraRotation);
        for (FloatingNumber number : numbers) {
            float alpha = number.alpha(now);
            if (alpha < MIN_ALPHA) {
                continue;
            }
            double relativeX = number.x(now) - cameraX;
            double relativeY = number.y(now) - cameraY;
            double relativeZ = number.z(now) - cameraZ;
            if (lookVector.x * relativeX + lookVector.y * relativeY + lookVector.z * relativeZ <= 0.0) {
                continue;
            }
            Vector4f point = new Vector4f((float) relativeX, (float) relativeY, (float) relativeZ, 1.0F);
            viewMatrix.transform(point);
            projectionMatrix.transform(point);
            if (point.w <= 1.0E-4F) {
                continue;
            }
            float ndcX = point.x / point.w;
            float ndcY = point.y / point.w;
            float ndcZ = point.z / point.w;
            if (!Float.isFinite(ndcX) || !Float.isFinite(ndcY) || !Float.isFinite(ndcZ)
                    || ndcZ < -1.0F || ndcZ > 1.0F) {
                continue;
            }
            float screenX = (ndcX * 0.5F + 0.5F) * guiWidth;
            float screenY = (1.0F - (ndcY * 0.5F + 0.5F)) * guiHeight;
            if (screenX < -SCREEN_MARGIN || screenX > guiWidth + SCREEN_MARGIN
                    || screenY < -SCREEN_MARGIN || screenY > guiHeight + SCREEN_MARGIN) {
                continue;
            }
            int color = ((int) (alpha * 255.0F) & 0xFF) << 24 | number.color() & 0xFFFFFF;
            int textX = Math.round(screenX - font.width(number.text()) / 2.0F);
            int textY = Math.round(screenY) - 4;
            guiGraphics.drawString(font, number.text(), textX, textY, color, true);
        }
    }
}
