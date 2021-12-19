#version 430 core

layout (vertices = 3) out;

layout(location = 1) in vec3 inColor[];
layout(location = 1) out vec3 outColor[];

//pomocí oterace měním rozlišení gridu
uniform int iter;

void main(void){

    if (gl_InvocationID == 0){
        gl_TessLevelInner[0] = iter;
        gl_TessLevelOuter[0] = 4.0;
        gl_TessLevelOuter[1] = 2.0;
        gl_TessLevelOuter[2] = 3.0;
    }

    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;

    outColor[gl_InvocationID] = inColor[gl_InvocationID];
}