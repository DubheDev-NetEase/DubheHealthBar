#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec3 fillColor;
out vec2 localPos;
out float fillProgress;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    fillColor = Color.rgb;
    localPos = UV0;
    fillProgress = Color.a;
}
