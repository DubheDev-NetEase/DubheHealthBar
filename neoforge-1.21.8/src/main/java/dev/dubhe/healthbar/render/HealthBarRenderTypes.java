package dev.dubhe.healthbar.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.dubhe.healthbar.DubheHealthBar;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

/**
 * 血条自定义着色器的 RenderPipeline 注册与 RenderType 创建。
 *
 * <p>1.21.8 不再使用 shader JSON，而是在 mod 事件总线的
 * {@link RegisterRenderPipelinesEvent} 中注册 {@link RenderPipeline}，
 * 再用该版本的 {@code RenderType.create(String, int, RenderPipeline, CompositeState)} 创建渲染类型。</p>
 */
public final class HealthBarRenderTypes {
    /** 着色器位置，对应 {@code assets/dubhe_health_bar/shaders/core/health_bar.vsh/.fsh}。 */
    public static final ResourceLocation SHADER_LOCATION = ResourceLocation.fromNamespaceAndPath(
            DubheHealthBar.MOD_ID, "core/health_bar");
    /** 血条顶点格式：Position、Color、UV0。
     *
     * <p>1.21.8 的 {@link DefaultVertexFormat} 没有名为 {@code POSITION_COLOR_TEX} 的常量，
     * 属性集合相同（Position + Color + UV0）的官方常量是 {@code POSITION_TEX_COLOR}；
     * 管线与着色器按属性名绑定，语义等价。</p>
     */
    public static final VertexFormat VERTEX_FORMAT = DefaultVertexFormat.POSITION_TEX_COLOR;
    /** 血条缓冲区大小。 */
    private static final int BUFFER_SIZE = 1536;
    /** 血条 RenderType 名称。 */
    private static final String RENDER_TYPE_NAME = "dubhe_health_bar_bar";

    @Nullable
    private static RenderType renderType;

    private HealthBarRenderTypes() {
    }

    /**
     * 在 mod 事件总线上注册血条 RenderPipeline 并创建 RenderType。
     *
     * @param event 渲染管线注册事件
     */
    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(ResourceLocation.fromNamespaceAndPath(DubheHealthBar.MOD_ID, "pipeline/health_bar"))
                .withVertexShader(SHADER_LOCATION)
                .withFragmentShader(SHADER_LOCATION)
                .withVertexFormat(VERTEX_FORMAT, VertexFormat.Mode.QUADS)
                .withBlend(BlendFunction.TRANSLUCENT)
                .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                .withDepthWrite(false)
                .withCull(false)
                .build();
        event.registerPipeline(pipeline);
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setTextureState(RenderStateShard.NO_TEXTURE)
                .createCompositeState(false);
        renderType = RenderType.create(RENDER_TYPE_NAME, BUFFER_SIZE, pipeline, compositeState);
    }

    /**
     * 获取血条 RenderType。
     *
     * <p>管线尚未注册时返回 {@code null}，调用方应跳过渲染。</p>
     *
     * @return 血条 RenderType 或 {@code null}
     */
    @Nullable
    public static RenderType getRenderType() {
        return renderType;
    }
}
