package dev.dubhe.healthbar.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.dubhe.healthbar.DubheHealthBar;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 血条着色器与 RenderType 的注册入口。
 */
@Mod.EventBusSubscriber(modid = DubheHealthBar.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HealthBarRenderTypes {
    private static final Logger LOGGER = LoggerFactory.getLogger("DubheHealthBar");
    private static final VertexFormat HEALTH_BAR_FORMAT = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElement.COLOR)
            .add("UV0", VertexFormatElement.UV0)
            .build();
    private static RenderType renderType;

    private HealthBarRenderTypes() {
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            ShaderInstance shader = new ShaderInstance(event.getResourceProvider(),
                    ResourceLocation.fromNamespaceAndPath(DubheHealthBar.MOD_ID, "health_bar"), HEALTH_BAR_FORMAT);
            event.registerShader(shader, loaded -> renderType = createRenderType(loaded));
        } catch (IOException exception) {
            LOGGER.error("Failed to create the Dubhe Health Bar shader", exception);
        }
    }

    private static RenderType createRenderType(ShaderInstance shader) {
        return RenderType.create("dubhe_health_bar_bar", HEALTH_BAR_FORMAT, VertexFormat.Mode.QUADS,
                256, false, false, HealthBarRenderState.compositeState(shader));
    }

    /**
     * 获取血条 RenderType，着色器未加载时返回 null。
     *
     * @return 血条 RenderType
     */
    public static RenderType getRenderType() {
        return renderType;
    }
}
