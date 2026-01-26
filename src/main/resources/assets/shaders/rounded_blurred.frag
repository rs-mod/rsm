#version 120

uniform float softness;
uniform float radius;
uniform vec2 size;
uniform vec4 color;

float alpha(vec2 p, vec2 b) {
    return length(max(abs(p) - b, .0f)) - radius;
}

void main() {
    vec2 rectCenter = .5f * size;
    float distance = alpha(rectCenter - (gl_TexCoord[0].st * size), rectCenter - 1.f);
    float smoothedAlpha =  1.0f-smoothstep(-softness, softness,distance);

    gl_FragColor = vec4(color.rgb, color.a * smoothedAlpha);
}
