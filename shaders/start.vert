#version 150
in vec2 inPosition; // input from the vertex buffer

uniform mat4 view;
uniform mat4 projection;
uniform mat4 lightVP;

uniform int solid;
uniform vec3 lightPosition;
uniform vec3 eyePosition;
uniform float time; //modifikace telesa v case

out vec2 texCoord;
out vec3 normal;
out vec3 light;
out vec3 viewDirection;
out vec4 depthTextureCoord;

const float PI = 3.14159;

vec3 getSphere(vec2 pos){
	float az = pos.x * PI;
	float ze = pos.y * PI / 2;
	float r = 1;

	float x = r * cos(az + time) * cos(ze); //modifikace telesa v case
	float y = r * sin(az + time) * cos(ze + time);
	float z = r * sin(ze);

	return vec3(x,y,z);
}

vec3 getSphereNormal(vec2 pos){
	vec3 u = getSpheere(pos + vec2(0.001,0)) - getSphere(pos - vec2(0.001,0));
	vec3 v = getSpheere(pos + vec2(0,0.001)) - getSphere(pos - vec2(0,0.001));

	return cross(u,v);
}

vec3 getSphereNormalByDerivate(vec2 pos){
	float az = pos.x * PI;
	float ze = pos.y * PI /2;
	// x = cos(u * PI) * cos(v* PI/2)
	// y = 2 * sin(u * PI) * cos(v * PI/2)
	// z = 0.5 * sin(v * PI/2)

	vec3 u = vec3(-sin(az) * cos(ze) * PI, cos(az) * cos(ze) * PI,0);
	vec3 v = vec3(cos(az) * -sin(ze) * PI/2, sin(az) * -sin(ze) * PI/2, cos(ze) * PI/2);

	return cross(u,v);
}

float getZ(vec2 pos){
	return sin(pos.x * 5);
}

vec3 getPlane(vec2 pos){
	return vec3(pos * 3, -1);
}

vec3 getPlaneNormal(vec2 pos){
	vec3 u = getPlane(pos + vec2(0.001,0)) - getPlane(pos - vec2(0.001,0));
	vec3 v = getPlane(pos + vec2(0,0.001)) - getPlane(pos - vec2(0,0.001));

	return cross(u,v);
}

void main() {
	texCoord = inPosition;
	vec2 position = inPosition *2 - 1;
	vec3 pos3;

	if(solid == 1){
		pos3 = getSphere(position);
		normal  = getSphereNormal(position);
	}else if(solid == 2){
		pos3 = getPlane(position);
		normal = getPlaneNormal(position);
	}

	gl_Position = projection * view * vec4(pos3, 1.0);

	light = lightPosition - pos3;
	viewDirection = eyePosition - pos3;

	depthTextureCoord = lightVP * vec4(pos3,1.0); // získáváme pozici vrcholu tak, jak vrchol vidí světlo
	depthTextureCoord.xyz = depthTextureCoord.xyz / depthTextureCoord.w; // dehomogenizace
	// obrazovka je <-1;1>
	//texture je <0;1>
	depthTextureCoord.xyz = (depthTextureCoord.xyz + 1) / 2;
} 
