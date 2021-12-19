#version 440

layout(triangles) in; //vstup ve formě triangles
layout(triangle_strip, max_vertices = 100) out; //vystup ve formě triangle strips

layout(location = 1) in vec3 inColor[];

layout(location = 1) out vec3 outColor;
layout(location = 2) out flat int elementID;

void emitBoundry (vec4 v){
    float pointOffset=0.02;

    //zde si emituji vrcholy pro jednotlivé body, aby byly lépe vidět, místo bodu se tedy zobrazí malý obdélníček (V modelu se zobrazí po prvním kliknutí na "M")

    gl_Position = v + vec4(pointOffset, -pointOffset, 0.0, 0.0);
    EmitVertex();

    gl_Position = v + vec4(-pointOffset, -pointOffset, 0.0, 0.0);
    EmitVertex();

    gl_Position = v + vec4(pointOffset, pointOffset, 0.0, 0.0);
    EmitVertex();

    gl_Position = v + vec4(-pointOffset, pointOffset, 0.0, 0.0);
    EmitVertex();

    EndPrimitive();
}

void main() {

    outColor = inColor[0];
    emitBoundry(gl_in[0].gl_Position);

    outColor = inColor[1];
    emitBoundry(gl_in[1].gl_Position);

    outColor = inColor[2];
    emitBoundry(gl_in[2].gl_Position);

    elementID = gl_InvocationID;

}
