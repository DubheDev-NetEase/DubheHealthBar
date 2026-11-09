package dev.dubhe.healthbar.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
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
    private static RenderType renderType;

    private HealthBarRenderTypes() {
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        ResourceLocation shaderLocation = ResourceLocation.tryBuild(DubheHealthBar.MOD_ID, "health_bar");
        if (shaderLocation == null) {
            LOGGER.error("Invalid shader location for Dubhe Health Bar");
            return;
        }
        try {
            ShaderInstance shader = new ShaderInstance(event.getResourceProvider(), shaderLocation,
                    DefaultVertexFormat.POSITION_COLOR_TEX);
            event.registerShader(shader, loaded -> renderType = createRenderType(loaded));
        } catch (IOException exception) {
            LOGGER.error("Failed to create the Dubhe Health Bar shader", exception);
        }
    }

    private static RenderType createRenderType(ShaderInstance shader) {
        return RenderType.create("dubhe_health_bar_bar", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
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
