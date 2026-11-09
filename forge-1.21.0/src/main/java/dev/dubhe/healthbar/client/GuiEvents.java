package dev.dubhe.healthbar.client;

import dev.dubhe.healthbar.DubheHealthBar;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
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

    /**
     * 由 Gui 渲染混入在 HUD 绘制结束后调用。
     *
     * @param guiGraphics GUI 绘制上下文
     */
    public static void render(GuiGraphics guiGraphics) {
        FloatingNumberRenderer.render(guiGraphics);
    }
}
