#version 120

uniform float softness;
uniform float radius;
uniform vec2 size;
uniform vec4 color1;
uniform vec4 color2;
uniform vec4 color3;
uniform vec4 color4;

float alpha(vec2 p, vec2 b) {
    return length(max(abs(p) - b, .0f)) - radius;
}

void main() {
    vec2 coords = gl_TexCoord[0].st;
    vec4 color = mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);
    vec2 rectCenter = .5f * size;
    float distance = alpha(rectCenter - (gl_TexCoord[0].st * size), rectCenter - radius - softness);
    float smoothedAlpha =  1.0f-smoothstep(-softness, softness, distance);
    gl_FragColor = vec4(color.rgb, color.a * smoothedAlpha);
}
