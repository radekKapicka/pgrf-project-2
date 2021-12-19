#version 450
layout(location = 2) in vec3 inColor;
layout(location = 1) in vec2 inPosition;

layout(location = 1) out vec3 vsColor;
layout(location = 2) out flat int elementID;

uniform vec3 eyePosition;
uniform mat4 view;
uniform mat4 projection;
uniform int demoType;

void main() {
	vec3 pos3;
	vec2 position = inPosition;

	//zde měním zobrazovací mod
	if(demoType == 2){
		//jelikoz jsou krivky pouze dvourozmerne, necham pro ne zobrazit jakoby "platno", kde jde krivku kreslit, tedy s fixni pozici pohledu
		gl_Position = vec4(position,0, 1.0);
	}
	else{
		//plocha je jiz zobrazena klasicky v prostoru a lze se zde pohybovat pomoci WASD a mysi
		gl_Position = projection * view * vec4(position,0, 1.0);
	}
	vsColor = inColor;


} 
