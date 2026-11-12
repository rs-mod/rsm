#version 330

#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in vec2 texCoord;
layout(location = 1) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

float screenPxRange() {
    vec2 unitRange = vec2(8.0) / vec2(textureSize(Sampler0, 0));

    vec2 screenTexSize = inversesqrt(
        vec2(dot(dFdx(texCoord), dFdx(texCoord)), dot(dFdy(texCoord), dFdy(texCoord)))
    );

    return max(
        0.5 * dot(unitRange, screenTexSize),
        1.0
    );
}

void main() {
    vec3 sampleValue = texture(Sampler0, texCoord).rgb;

    float signedDistance = median(sampleValue.r, sampleValue.g, sampleValue.b) - 0.5;
    float pxRange = screenPxRange();
    float opacity = clamp(signedDistance * pxRange + 0.5, 0.0, 1.0);
    fragColor = vec4(vertexColor.rgb, vertexColor.a * opacity);

    if (fragColor.a < 0.001) {
        discard;
    }
}
