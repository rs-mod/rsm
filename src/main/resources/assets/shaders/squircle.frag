#version 120

/*
Made by wouf
*/

uniform float round;
uniform vec2 size;
uniform vec4 color;

float alpha(vec2 d, vec2 d1) {
    return length(abs(d) - d1) - round;
}

void main() {

    vec2 centre = 0.5 * size;
    vec2 normalizedCoord = gl_TexCoord[0].st * size;
    float squircle = alpha(centre - normalizedCoord, vec2(1.0)) / sqrt(2);
    float smoothSquircle = smoothstep(0.0, 1.0, squircle);

    gl_FragColor = vec4(color.rgb, color.a * (1.0 - smoothSquircle));
}
