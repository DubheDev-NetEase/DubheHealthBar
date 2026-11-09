#version 330

in vec3 fillColor;
in vec2 localPos;
in float fillProgress;

out vec4 fragColor;

const vec2 HALF_SIZE = vec2(20.0, 2.0);
const float RADIUS = 1.2;
const float EDGE = 0.75;

float roundedBox(vec2 point, vec2 halfSize, float radius) {
    vec2 distance = abs(point) - halfSize + radius;
    return min(max(distance.x, distance.y), 0.0) + length(max(distance, 0.0)) - radius;
}

void main() {
    float backgroundMask = 1.0 - smoothstep(-EDGE, EDGE, roundedBox(localPos, HALF_SIZE, RADIUS));
    if (backgroundMask <= 0.0) {
        discard;
    }

    float fillMask = 0.0;
    if (fillProgress > 0.0) {
        float fillHalfWidth = HALF_SIZE.x * fillProgress;
        vec2 fillCenter = vec2(-HALF_SIZE.x + fillHalfWidth, 0.0);
        vec2 fillPoint = localPos - fillCenter;
        float innerRadius = max(RADIUS - 0.8, 0.0);
        fillMask = 1.0 - smoothstep(-EDGE, EDGE, roundedBox(fillPoint, vec2(fillHalfWidth, HALF_SIZE.y - 0.8), innerRadius));
    }

    vec3 backgroundColor = vec3(0.125, 0.125, 0.125);
    vec3 color = mix(backgroundColor, fillColor, fillMask);
    float alpha = mix(0.60, 0.92, fillMask) * backgroundMask;
    fragColor = vec4(color, alpha);
}
