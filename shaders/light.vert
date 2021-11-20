#version 150
in vec2 inPosition; // input from the vertex buffer

uniform mat4 view;
uniform mat4 projection;

uniform int solid;
uniform float time; //modifikace telesa v case

const float PI = 3.14159;

vec3 getSphere(vec2 pos){
    float az = pos.x * PI;
    float ze = pos.y * PI / 2;
    float r = 1;

    float x = r * cos(az) * cos(ze);
    float y = r * sin(az) * cos(ze);
    float z = r * sin(ze);

    return vec3(x-1,y-1,z);
}


vec3 getElephant(vec2 pos){
    float az = ((pos.x + 1)/2) * PI;
    float ze = ((pos.y + 1)/2) * (2 * PI);
    float r = 3 + cos(4*az);

    float x = r * sin(ze + time) * cos(az + time);
    float y = r * sin(ze + time) * sin(az + time);
    float z = r * cos(ze);

    return vec3((x/3)+1,(y/3)+1,(z/3)+0.5);
}

vec3 getSombrero(vec2 pos){
    float r = ((pos.x + 1)/2) * (2 * PI);
    float az = ((pos.y + 1)/2) * (2 * PI);
    float v = 2 * sin(r);

    float x = r * cos(az);
    float y = r * sin(az);
    float z = v;

    return vec3((x/6)-2,(y/6)+2,(z/4)+0.5);
}
vec3 getCylinder(vec2 pos){
    float r = ((pos.x + 1)/2) * (2 * PI);
    float az = ((pos.y + 1)/2) * (2 * PI);

    float x = cos(r) * (3+1*cos(az));
    float y = sin(r) * (3+1*cos(az));
    float z = 1 * sin(az);

    return vec3((x/5)+3,(y/5)-2,(z/5));
}

vec3 getCurve(vec2 pos){
    float x = 2 * cos(pos.x + time);
    float y = 2 * sin(pos.x + time);
    float z = pos.y;
    return vec3((x/2)+4,(y/2)+3,(z/4));
}

vec3 getPlane(vec2 pos){
    return vec3(pos * 5, -1);
}

void main() {
    vec2 position = inPosition *2 - 1;
    vec3 pos3;

    if(solid == 1){
        pos3 = getSphere(position);
    }else if(solid == 2){
        pos3 = getPlane(position);
    }else if(solid == 3){
        pos3 = getElephant(position);
    }else if(solid == 4){
        pos3 = getSombrero(position);
    }else if(solid == 5){
        pos3 = getCylinder(position);
    }else if(solid == 6){
        pos3 = getCurve(position);
    }

    gl_Position = projection * view * vec4(pos3, 1.0);
}
