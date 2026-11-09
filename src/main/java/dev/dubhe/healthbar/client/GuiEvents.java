package dev.dubhe.healthbar.client;

import dev.dubhe.healthbar.DubheHealthBar;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * GUI 与关卡渲染事件入口。
 */
@Mod.EventBusSubscriber(modid = DubheHealthBar.MOD_ID, value = Dist.CLIENT)
public final class GuiEvents {
    private GuiEvents() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            FloatingNumberRenderer.captureProjection(event);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        FloatingNumberRenderer.render(event.getGuiGraphics());
    }
}
