package dev.dubhe.healthbar.client;

import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * GUI 事件入口。
 */
public final class GuiEvents {
    private GuiEvents() {
    }

    /**
     * 在 HUD 绘制结束后渲染浮动数字。
     *
     * @param event HUD 渲染事件
     */
    public static void onRenderGui(RenderGuiEvent.Post event) {
        FloatingNumberRenderer.render(event.getGuiGraphics());
    }
}
