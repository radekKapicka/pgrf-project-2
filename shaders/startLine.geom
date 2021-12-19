#version 430

layout(lines_adjacency) in;

layout(line_strip, max_vertices = 100) out;

//stejna promenna jako pro plochu, menim ji vyhlazeni krivky
uniform int iter;

layout(location = 1) in vec3 vColor[];

layout(location = 1) out vec3 fColor;

vec3 p[4];

//separatni geometry shader pro generovani krivky a jeji vyhlazeni
void main() {

    p[0]=gl_in[0].gl_Position.xyz*0.5+gl_in[1].gl_Position.xyz*0.5;
    p[1]=gl_in[1].gl_Position.xyz;
    p[2]=gl_in[2].gl_Position.xyz;
    p[3]=gl_in[2].gl_Position.xyz*0.5+gl_in[3].gl_Position.xyz*0.5;

    fColor = vColor[0];

    for(int i=0; i<=iter; i++) {
        float t=1-i*1.0/iter;
        gl_Position = vec4(p[0]*t*t*t + p[1]*t*(1-t)*3*t + p[2]*3*t*(1-t)*(1-t) + p[3]*(1-t)*(1-t)*(1-t),1);
        EmitVertex();
    }
    EndPrimitive();

}
