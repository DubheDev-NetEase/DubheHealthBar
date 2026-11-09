package dev.dubhe.healthbar.client;

import dev.dubhe.healthbar.FloatingNumber;
import dev.dubhe.healthbar.HealthTracker;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;

/**
 * 将浮动数字投影到 GUI 坐标系并绘制。
 */
public final class FloatingNumberRenderer {
    private static final double SCREEN_MARGIN = 32.0;
    private static final int TEXT_TOP_OFFSET = -4;

    private FloatingNumberRenderer() {
    }

    /**
     * 在 HUD 渲染完成后绘制全部活动数字。
     *
     * @param guiGraphics HUD 绘制工具
     */
    public static void render(GuiGraphicsExtractor guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        List<FloatingNumber> numbers = HealthTracker.numbers();
        if (numbers.isEmpty()) {
            return;
        }
        GameRenderer gameRenderer = minecraft.gameRenderer;
        Camera camera = gameRenderer.getMainCamera();
        Vec3 cameraPosition = camera.position();
        Vec3 cameraForward = new Vec3(camera.forwardVector());
        Font font = minecraft.font;
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        long nowMillis = Util.getMillis();
        for (FloatingNumber number : numbers) {
            double seconds = number.elapsedSeconds(nowMillis);
            if (seconds >= FloatingNumber.LIFETIME_SECONDS) {
                continue;
            }
            Vec3 position = number.position(seconds);
            if (position.subtract(cameraPosition).dot(cameraForward) <= 0.0) {
                continue;
            }
            Vec3 projected = gameRenderer.projectPointToScreen(position);
            if (!Double.isFinite(projected.x) || !Double.isFinite(projected.y) || !Double.isFinite(projected.z)) {
                continue;
            }
            if (projected.z < -1.0 || projected.z > 1.0) {
                continue;
            }
            double screenX = (projected.x * 0.5 + 0.5) * guiWidth;
            double screenY = (1.0 - (projected.y * 0.5 + 0.5)) * guiHeight;
            if (screenX < -SCREEN_MARGIN
                    || screenX > guiWidth + SCREEN_MARGIN
                    || screenY < -SCREEN_MARGIN
                    || screenY > guiHeight + SCREEN_MARGIN) {
                continue;
            }
            int color = number.argb(seconds);
            if ((color >>> 24) == 0) {
                continue;
            }
            int x = (int) Math.round(screenX) - font.width(number.text()) / 2;
            int y = (int) Math.round(screenY) + TEXT_TOP_OFFSET;
            guiGraphics.text(font, number.text(), x, y, color, true);
        }
    }
}
