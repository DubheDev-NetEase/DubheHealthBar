package dev.dubhe.healthbar.client;

import dev.dubhe.healthbar.FloatingNumber;
import dev.dubhe.healthbar.HealthTracker;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * GUI 层的生命值变化数字渲染。
 *
 * <p>使用 {@code GameRenderer.projectPointToScreen(Vec3)} 把数字自身的世界坐标投影到屏幕，
 * 位置只由数字动画时间决定，不对实体位置做插值。alpha 通过 ARGB 颜色通道实现淡出。</p>
 */
public final class FloatingNumberRenderer {
    /** 屏幕外裁剪边距，像素。 */
    private static final double SCREEN_MARGIN = 32.0;
    /** 文本顶部相对投影点的偏移，像素。 */
    private static final int TEXT_TOP_OFFSET = -4;

    private FloatingNumberRenderer() {
    }

    /**
     * GUI 渲染入口：投影并绘制全部活动数字。
     *
     * @param guiGraphics GUI 绘制上下文
     */
    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return;
        }
        Vec3 cameraPosition = camera.getPosition();
        Vec3 cameraForward = Vec3.directionFromRotation(camera.getXRot(), camera.getYRot());
        Font font = minecraft.font;
        int guiWidth = guiGraphics.guiWidth();
        int guiHeight = guiGraphics.guiHeight();
        for (FloatingNumber number : HealthTracker.getInstance().getNumbers()) {
            Vec3 worldPosition = new Vec3(number.getX(), number.getY(), number.getZ());
            if (worldPosition.subtract(cameraPosition).dot(cameraForward) <= 0.0) {
                continue;
            }
            Vec3 projected = minecraft.gameRenderer.projectPointToScreen(worldPosition);
            if (!Double.isFinite(projected.x) || !Double.isFinite(projected.y) || !Double.isFinite(projected.z)) {
                continue;
            }
            if (projected.z < -1.0 || projected.z > 1.0) {
                continue;
            }
            int screenX = (int) Math.round((projected.x * 0.5 + 0.5) * guiWidth);
            int screenY = (int) Math.round((1.0 - (projected.y * 0.5 + 0.5)) * guiHeight);
            if (screenX < -SCREEN_MARGIN || screenX > guiWidth + SCREEN_MARGIN
                    || screenY < -SCREEN_MARGIN || screenY > guiHeight + SCREEN_MARGIN) {
                continue;
            }
            int alpha = Mth.clamp(Math.round(number.getAlpha() * 255.0F), 0, 255);
            if (alpha <= 0) {
                continue;
            }
            int color = ARGB.color(alpha, number.getColor());
            String text = number.getText();
            guiGraphics.drawString(
                    font,
                    text,
                    screenX - font.width(text) / 2,
                    screenY + TEXT_TOP_OFFSET,
                    color,
                    true);
        }
    }
}
