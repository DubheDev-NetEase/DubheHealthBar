package dev.dubhe.healthbar.client;

import dev.dubhe.healthbar.FloatingNumber;
import dev.dubhe.healthbar.HealthTracker;
import dev.dubhe.healthbar.render.HealthBarStyle;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * GUI 层浮动数字渲染与投影。
 */
public final class FloatingNumberRenderer {
    private static final float SCREEN_MARGIN = 32.0F;

    private FloatingNumberRenderer() {
    }

    /**
     * 把活动浮动数字投影到屏幕并绘制。
     *
     * @param guiGraphics GUI 绘制上下文
     */
    public static void render(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        List<FloatingNumber> numbers = HealthTracker.numbers();
        if (numbers.isEmpty()) {
            return;
        }
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return;
        }

        long nowMillis = Util.getMillis();
        Vec3 cameraPos = camera.getPosition();
        Vector3f lookVector = camera.getLookVector();
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        Font font = minecraft.font;

        for (FloatingNumber number : numbers) {
            Vec3 worldPos = new Vec3(number.positionX(nowMillis), number.positionY(nowMillis), number.positionZ(nowMillis));
            Vec3 relative = worldPos.subtract(cameraPos);
            if (lookVector.dot(relative.toVector3f()) <= 0.0F) {
                continue;
            }
            Vec3 projected = minecraft.gameRenderer.projectPointToScreen(worldPos);
            if (!isProjectable(projected)) {
                continue;
            }
            float screenX = (float) ((projected.x * 0.5 + 0.5) * guiWidth);
            float screenY = (float) ((1.0 - (projected.y * 0.5 + 0.5)) * guiHeight);
            if (screenX < -SCREEN_MARGIN
                || screenX > guiWidth + SCREEN_MARGIN
                || screenY < -SCREEN_MARGIN
                || screenY > guiHeight + SCREEN_MARGIN) {
                continue;
            }
            String text = number.text();
            int textX = Math.round(screenX) - font.width(text) / 2;
            int textY = Math.round(screenY) + Math.round(HealthBarStyle.NUMBER_TOP);
            guiGraphics.drawString(font, text, textX, textY, number.color(nowMillis), true);
        }
    }

    private static boolean isProjectable(Vec3 projected) {
        return Double.isFinite(projected.x)
            && Double.isFinite(projected.y)
            && Double.isFinite(projected.z)
            && projected.z >= -1.0
            && projected.z <= 1.0;
    }
}
