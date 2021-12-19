#version 440

layout(location = 1) in vec3 inColor;
layout(location = 2) in flat int elementID;

out vec4 outColor;
void main() {
	outColor = vec4(inColor, 1.0);

}
