//https://www.shadertoy.com/view/WtdSDs
#version 120

uniform float round;
uniform vec2 size;
uniform vec4 color;

float alpha(vec2 d, vec2 d1) {
    vec2 v = abs(d) - d1 + round;
    return min(max(v.x, v.y), 0.0) + length(max(v, .0f)) - round;
}

void main() {

    vec2 rectCenter = .5f * size;
    float edgeSoftness  = 1.0f; //blur
    float distance = alpha(rectCenter - (gl_TexCoord[0].st * size), rectCenter - 1.f);
    float smoothedAlpha =  1.0f-smoothstep(0.0f, edgeSoftness * 2.0f,distance);

    gl_FragColor = vec4(color.rgb, color.a * smoothedAlpha);
}
