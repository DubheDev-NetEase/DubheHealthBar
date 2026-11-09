package dev.dubhe.healthbar.client;

import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * GUI 事件入口。
 */
public final class GuiEvents {
    private GuiEvents() {
    }

    /**
     * 处理 HUD 渲染完成事件。
     *
     * @param event HUD 渲染事件
     */
    public static void onRenderGui(RenderGuiEvent.Post event) {
        FloatingNumberRenderer.render(event.getGuiGraphics());
    }
}
