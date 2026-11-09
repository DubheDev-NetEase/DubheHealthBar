#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 localPos;
out float fillProgress;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // 1.21.8 的顶点位置已在 CPU 侧乘过 PoseStack 矩阵，Position 是世界坐标，
    // 因此局部像素坐标由 UV0 携带，血量比例由 Color 的 alpha 通道携带。
    vertexColor = vec4(Color.rgb, 1.0);
    localPos = UV0;
    fillProgress = Color.a;
}
