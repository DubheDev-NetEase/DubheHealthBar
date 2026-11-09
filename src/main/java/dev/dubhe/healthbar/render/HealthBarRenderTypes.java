package dev.dubhe.healthbar.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.dubhe.healthbar.DubheHealthBar;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

/**
 * 血条专用渲染管线与 RenderType。
 *
 * <p>着色器同时输出灰色背景与阵营色填充，使用 {@link DefaultVertexFormat#POSITION_TEX_COLOR} 顶点格式，
 * 其中 UV0 携带血条局部像素坐标，Color 的 alpha 通道携带血量填充比例。</p>
 */
public final class HealthBarRenderTypes {
    private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withLocation(DubheHealthBar.id("pipeline/health_bar"))
        .withVertexShader(DubheHealthBar.id("core/health_bar"))
        .withFragmentShader(DubheHealthBar.id("core/health_bar"))
        .withBlend(BlendFunction.TRANSLUCENT)
        .withCull(false)
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
        .build();

    private static boolean registered;
    private static RenderType bar;

    private HealthBarRenderTypes() {
    }

    /**
     * 在模组事件总线上注册血条渲染管线。
     *
     * @param event 渲染管线注册事件
     */
    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
        registered = true;
    }

    /**
     * 返回血条 RenderType；渲染管线尚未注册时返回 {@code null}。
     *
     * @return 血条 RenderType 或 {@code null}
     */
    public static RenderType bar() {
        if (!registered) {
            return null;
        }
        if (bar == null) {
            bar = RenderType.create(
                "dubhe_health_bar_bar",
                1536,
                false,
                false,
                PIPELINE,
                RenderType.CompositeState.builder().createCompositeState(false)
            );
        }
        return bar;
    }
}
