#version 150
in vec2 texCoord;
in vec3 normal;
in vec3 light;
in vec3 viewDirection;
in vec4 depthTextureCoord;

uniform sampler2D depthTexture;
uniform sampler2D mosaic;
uniform int visMode;
uniform int lightFragment;

out vec4 outColor; // output from the fragment shader
void main() {
	vec3 ambient = vec3(0.2);

	float NdotL = max(0.0,dot(normalize(normal), normalize(light)));
	vec3 difuse = NdotL * vec3(0.7);

	vec3 halfVector = normalize(light + viewDirection);
	float NdotH = max(0.0, dot(normalize(normal), halfVector));
	vec3 specular = vec3(pow(NdotH, 16.0));

	if(lightFragment == 1){
		ambient = vec3(0,0,0);
		difuse = vec3(0,0,0);
	}else if(lightFragment == 2){
		specular = vec3(0,0,0);
		difuse = vec3(0,0,0);
	}else if(lightFragment == 3){
		ambient = vec3(0,0,0);
		specular = vec3(0,0,0);
	}else if(lightFragment == 4){
		vec3 colorIntensity = ambient + difuse + specular;
	}

	vec3 colorIntensity = ambient + difuse + specular;
	vec3 textureColor = texture(mosaic, texCoord).rgb;

	float zlight = texture(depthTexture, depthTextureCoord.xy).r; // z hodnota ke světlu nejbližšího pixelu na této pozici

	float zActual = depthTextureCoord.z;

	bool shadow = zActual > zlight + 0.001;

	if(visMode == 0){ //zobrazeni textury
		if(shadow){
			outColor = vec4(ambient * textureColor, 1.0);
		} else {
			outColor = vec4(colorIntensity * textureColor, 1.0);
		}
	}else if(visMode == 1){  //zobrazeni barvy podle normaly
		outColor = vec4(normalize(normal), 1.0);
	}else if(visMode == 2){  //osvětlení bez textur
		outColor = vec4(colorIntensity,1.0);
	}else if(visMode == 3){  //zobrazeni barvy podle souradnic do textury
		outColor = vec4(texCoord.xy,1,1.0);
	}else if(visMode == 4){  //zobrazeni barvy podle vzdalenosti - hloubky
		outColor = vec4(depthTextureCoord.xyzw);
	}

}
