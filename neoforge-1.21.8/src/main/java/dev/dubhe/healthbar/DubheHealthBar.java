package dev.dubhe.healthbar;

import dev.dubhe.healthbar.client.ClientTickEvents;
import dev.dubhe.healthbar.client.GuiEvents;
import dev.dubhe.healthbar.client.HealthBarRenderer;
import dev.dubhe.healthbar.render.HealthBarRenderTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Dubhe Health Bar 客户端模组入口。
 *
 * <p>模组只在物理客户端加载，不注册网络通道与服务端内容。</p>
 */
@Mod(value = DubheHealthBar.MOD_ID, dist = Dist.CLIENT)
public final class DubheHealthBar {
    /** 模组 id。 */
    public static final String MOD_ID = "dubhe_health_bar";

    /**
     * 模组构造入口，注册渲染管线、tick 与渲染事件。
     *
     * @param modEventBus mod 事件总线
     */
    public DubheHealthBar(IEventBus modEventBus) {
        modEventBus.addListener(HealthBarRenderTypes::onRegisterRenderPipelines);
        NeoForge.EVENT_BUS.addListener(ClientTickEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(GuiEvents::onRenderGui);
        NeoForge.EVENT_BUS.addListener(HealthBarRenderer::onRenderLiving);
    }
}
