package dev.dubhe.healthbar;

import dev.dubhe.healthbar.client.ClientTickEvents;
import dev.dubhe.healthbar.client.GuiEvents;
import dev.dubhe.healthbar.client.HealthBarRenderer;
import dev.dubhe.healthbar.render.HealthBarRenderTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Dubhe Health Bar 模组入口与常量。
 */
@Mod(value = DubheHealthBar.MOD_ID, dist = Dist.CLIENT)
public final class DubheHealthBar {
    /**
     * 模组 id。
     */
    public static final String MOD_ID = "dubhe_health_bar";

    public DubheHealthBar(IEventBus modEventBus) {
        modEventBus.addListener(RegisterRenderPipelinesEvent.class, HealthBarRenderTypes::registerPipelines);
        NeoForge.EVENT_BUS.addListener(ClientTickEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(GuiEvents::onRenderGui);
        NeoForge.EVENT_BUS.addListener(HealthBarRenderer::onRenderLiving);
    }
}
