package dev.dubhe.healthbar.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.dubhe.healthbar.DubheHealthBar;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

/**
 * 世界空间血条的自定义渲染管线与 RenderType。
 */
public final class HealthBarRenderTypes {
    /**
     * 与 1.21.8 的 POSITION_COLOR_TEX 语义一致的自定义顶点格式，属性名与着色器输入一一对应。
     */
    public static final VertexFormat POSITION_COLOR_TEX = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElement.COLOR)
            .add("UV0", VertexFormatElement.UV0)
            .build();
    /**
     * 血条 RenderType 名称。
     */
    public static final String RENDER_TYPE_NAME = "dubhe_health_bar_bar";

    private static final Identifier PIPELINE_ID = Identifier.fromNamespaceAndPath(DubheHealthBar.MOD_ID, "pipeline/health_bar");
    private static final Identifier SHADER_ID = Identifier.fromNamespaceAndPath(DubheHealthBar.MOD_ID, "core/health_bar");

    private static RenderType healthBarType;

    private HealthBarRenderTypes() {
    }

    /**
     * 注册血条渲染管线并创建对应的 RenderType。
     *
     * @param event 渲染管线注册事件
     */
    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(PIPELINE_ID)
                .withVertexShader(SHADER_ID)
                .withFragmentShader(SHADER_ID)
                .withVertexFormat(POSITION_COLOR_TEX, VertexFormat.Mode.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .withCull(false)
                .build();
        event.registerPipeline(pipeline);
        healthBarType = RenderType.create(RENDER_TYPE_NAME, RenderSetup.builder(pipeline).createRenderSetup());
    }

    /**
     * 返回血条 RenderType，管线尚未注册时返回 {@code null}。
     *
     * @return 血条 RenderType
     */
    public static RenderType healthBar() {
        return healthBarType;
    }
}
