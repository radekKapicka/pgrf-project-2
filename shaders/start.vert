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

vec3 getLightSource(vec2 pos){
	float az = pos.x * PI;
	float ze = pos.y * PI / 2;
	float r = 1;

	float x = r * cos(az) * cos(ze);
	float y = r * sin(az) * cos(ze);
	float z = r * sin(ze);

	return vec3((x/4)+7.5,(y/4)+7.5,(z/4)+7.5);
}

//sfericke teleso 1
vec3 getSphere(vec2 pos){
	float az = pos.x * PI;
	float ze = pos.y * PI / 2;
	float r = 1;

	float x = r * cos(az) * cos(ze);
	float y = r * sin(az) * cos(ze);
	float z = r * sin(ze);

	return vec3(x-1,y-1,z);
}
//sfericke teleso 2
vec3 getElephant(vec2 pos){
	float ze = ((pos.x + 1)/2) * PI;
	float az = ((pos.y + 1)/2) * (2 * PI);
	float r = 3 + cos(4*az);

	float x = r * sin(ze + time) * cos(az + time);
	float y = r * sin(ze + time) * sin(az + time);
	float z = r * cos(ze);

	return vec3((x/3)+1,(y/3)+1,(z/3)+0.5);
}
//cylyndricke teleso 1
vec3 getSombrero(vec2 pos){
	float r = ((pos.x + 1)/2) * (2 * PI);
	float az = ((pos.y + 1)/2) * (2 * PI);
	float v = 2 * sin(r);

	float x = r * cos(az);
	float y = r * sin(az);
	float z = v;

	return vec3((x/6)-2,(y/6)+2,(z/4)+0.5);
}

//cylyndricke teleso 2
vec3 getCylinder(vec2 pos){
	float r = ((pos.x + 1)/2) * (2 * PI);
	float az = ((pos.y + 1)/2) * (2 * PI);

	float x = cos(r) * (3+1*cos(az));
	float y = sin(r) * (3+1*cos(az));
	float z = 1 * sin(az);

	return vec3((x/5)+3,(y/5)-2,(z/5));
}

//kartezke teleso 1
vec3 getPlane(vec2 pos){
	return vec3(pos * 5, -1);
}

//kartezke teleso 2
vec3 getCurve(vec2 pos){
	float x = 2 * cos(pos.x + time);
	float y = 2 * sin(pos.x + time);
	float z = pos.y;
	return vec3((x/2)+4,(y/2)+3,(z/4));
}

vec3 getCurveNormal(vec2 pos){
	vec3 u = getCurve(pos + vec2(0.001,0)) - getCurve(pos - vec2(0.001,0));
	vec3 v = getCurve(pos + vec2(0,0.001)) - getCurve(pos - vec2(0,0.001));

	return cross(u,v);
}

vec3 getSphereNormal(vec2 pos){
	vec3 u = getSphere(pos + vec2(0.001,0)) - getSphere(pos - vec2(0.001,0));
	vec3 v = getSphere(pos + vec2(0,0.001)) - getSphere(pos - vec2(0,0.001));

	return cross(u,v);
}
vec3 getElephantNormal(vec2 pos){
	vec3 u = getElephant(pos + vec2(0.001,0)) - getElephant(pos - vec2(0.001,0));
	vec3 v = getElephant(pos + vec2(0,0.001)) - getElephant(pos - vec2(0,0.001));

	return cross(u,v);
}

vec3 getSombreroNormal(vec2 pos){
	vec3 u = getSombrero(pos + vec2(0.001,0)) - getSombrero(pos - vec2(0.001,0));
	vec3 v = getSombrero(pos + vec2(0,0.001)) - getSombrero(pos - vec2(0,0.001));

	return cross(u,v);
}

vec3 getCylinderNormal(vec2 pos){
	vec3 u = getCylinder(pos + vec2(0.001,0)) - getCylinder(pos - vec2(0.001,0));
	vec3 v = getCylinder(pos + vec2(0,0.001)) - getCylinder(pos - vec2(0,0.001));

	return cross(u,v);
}
vec3 getLightSourceNormal(vec2 pos){
	vec3 u = getLightSource(pos + vec2(0.001,0)) - getLightSource(pos - vec2(0.001,0));
	vec3 v = getLightSource(pos + vec2(0,0.001)) - getLightSource(pos - vec2(0,0.001));

	return cross(u,v);
}

vec3 getSphereNormalByDerivate(vec2 pos){
	float az = pos.x * PI;
	float ze = pos.y * PI /2;

	vec3 u = vec3(-sin(az) * cos(ze) * PI, cos(az) * cos(ze) * PI,0);
	vec3 v = vec3(cos(az) * -sin(ze) * PI/2, sin(az) * -sin(ze) * PI/2, cos(ze) * PI/2);

	return cross(u,v);
}

float getZ(vec2 pos){
	return sin(pos.x * 5);
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
	}else if(solid == 3){
		pos3 = getElephant(position);
		normal = getElephantNormal(position);
	}else if(solid == 4){
		pos3 = getSombrero(position);
		normal = getSombreroNormal(position);
	}else if(solid == 5){
		pos3 = getCurve(position);
		normal = getCurveNormal(position);
	}else if(solid == 6){
		pos3 = getCylinder(position);
		normal = getCylinderNormal(position);
	}else if(solid == 7){
		pos3 = getLightSource(position);
		normal = getLightSourceNormal(position);
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
