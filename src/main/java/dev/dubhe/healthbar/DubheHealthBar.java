package dev.dubhe.healthbar;

import dev.dubhe.healthbar.client.ClientTickEvents;
import dev.dubhe.healthbar.client.GuiEvents;
import dev.dubhe.healthbar.client.HealthBarRenderer;
import dev.dubhe.healthbar.render.HealthBarRenderTypes;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 模组入口，仅在物理客户端加载。
 *
 * <p>本模组不注册任何网络通道与服务端内容，所有逻辑都运行在客户端。</p>
 */
@Mod(value = DubheHealthBar.MOD_ID, dist = Dist.CLIENT)
public final class DubheHealthBar {
    /**
     * 模组 id。
     */
    public static final String MOD_ID = "dubhe_health_bar";

    /**
     * 注册渲染管线与游戏事件监听器。
     *
     * @param modEventBus 模组事件总线
     */
    public DubheHealthBar(IEventBus modEventBus) {
        modEventBus.addListener(HealthBarRenderTypes::onRegisterRenderPipelines);
        NeoForge.EVENT_BUS.addListener(ClientTickEvents::onClientTick);
        NeoForge.EVENT_BUS.addListener(GuiEvents::onRenderGui);
        NeoForge.EVENT_BUS.addListener(HealthBarRenderer::onRenderLiving);
    }

    /**
     * 构造本模组命名空间下的资源标识符。
     *
     * @param path 资源路径
     * @return 资源标识符
     */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
