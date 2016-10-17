#version 330
layout (location = 0) out vec3 out_color;
in vec2 UV;
uniform sampler2D textureSampler;
void main() {
	out_color = vec3(texture(textureSampler, UV));
}
