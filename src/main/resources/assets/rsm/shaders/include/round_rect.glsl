/*
 * Original code Copyright (c) 2026, odtheking (https://github.com/odtheking/Odin/blob/main/src/main/resources/assets/odin/shaders/include/round_rect.glsl)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 localPos;
layout(location = 2) flat in vec2 halfSize;
layout(location = 3) flat in vec4 radii;

layout(location = 4) flat in float edgeWidth;

float roundedBoxSDF(vec2 p, vec2 extent, float radius) {
    vec2 q = abs(p) - extent + radius;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

float shapeDistance() {
    float radius = localPos.x < 0.0
        ? (localPos.y < 0.0 ? radii.x : radii.w)
        : (localPos.y < 0.0 ? radii.y : radii.z);
    radius = min(radius, min(halfSize.x, halfSize.y));

    return roundedBoxSDF(localPos, halfSize, radius);
}

float shapeCoverage() {
    float dist = shapeDistance();
    float aa = max(fwidth(dist), 1e-4);

    float outer = clamp(0.5 - dist / aa, 0.0, 1.0);
    if (edgeWidth == 0.0) return outer;

    float inner = clamp(0.5 - (dist + abs(edgeWidth)) / aa, 0.0, 1.0);
    if (edgeWidth > 0.0) return outer - inner;

    return inner / max(1.0 - outer + inner, 1e-4);
}

float shadowCoverage() {
    float dist = shapeDistance();
    float falloff = max(edgeWidth, max(fwidth(dist), 1e-4));

    float t = clamp(1.0 - dist / falloff, 0.0, 1.0);

    return t * t * t;
}

vec2 shapeUV() {
    return clamp(localPos / halfSize * 0.5 + 0.5, 0.0, 1.0);
}
