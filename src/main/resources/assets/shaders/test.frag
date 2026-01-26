// Fragment Shader
#version 120

uniform vec3 position;

float alpha(vec2 d, vec2 d1) {
    vec2 v = abs(d) - d1 + vec2(1);
    return min(max(v.x, v.y), 0.0) + length(max(v, vec2(0.0)));
}

void main() {

    float round = 1.0;
    vec2 rectCenter = vec2(5.0, 5.0);
    float edgeSoftness  = 1.0;

    vec2 worldPos = vec2(position.x, position.z);

    vec2 fragmentPos = vec2(gl_FragCoord.x, gl_FragCoord.y);
    float distance = length(worldPos - fragmentPos);

    float smoothedAlpha = 1.0 - smoothstep(0.0, edgeSoftness * 2.0, distance);

    gl_FragColor = vec4(255, 0, 0, 255) * smoothedAlpha;
}
