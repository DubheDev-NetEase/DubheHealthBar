package dev.dubhe.healthbar.render;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;

/**
 * 在 RenderStateShard 子类内访问 1.20.1 的 protected 状态常量。
 */
final class HealthBarRenderState extends RenderStateShard {
    private HealthBarRenderState() {
        super("dubhe_health_bar_state", () -> {
        }, () -> {
        });
    }

    static RenderType.CompositeState compositeState(ShaderInstance shader) {
        return RenderType.CompositeState.builder()
                .setShaderState(new ShaderStateShard(() -> shader))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
    }
}
