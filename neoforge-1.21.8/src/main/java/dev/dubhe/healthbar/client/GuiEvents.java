package dev.dubhe.healthbar.client;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * GUI 事件入口。
 */
public final class GuiEvents {
    private GuiEvents() {
    }

    /**
     * HUD 渲染结束后绘制生命值变化数字。
     *
     * @param event GUI 渲染事件
     */
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        FloatingNumberRenderer.render(event.getGuiGraphics());
    }
}
