#version 150
in vec2 txCoord;
in vec3 normal;
in vec3 light;
in vec3 viewDirection;

uniform sampler2D depthTexture;
uniform sampler2D mosaic;

out vec4 outColor; // output from the fragment shader
void main() {
	vec3 ambient = vec3(0.5, 0.0, 0.0);

	float NdotL = max(0,dot(normalize(normal), normalize(light)));
	vec3 difuse = NdotL * vec3(0, 0.5,0);

	vec3 halfVector = normalize(light + viewDirection);
	float NdotH = max(0, dot(normalize(normal), halfVector));
	vec3 specular = vec3(pow(NdotH, 16.0));

	vec3 colorIntensity = ambient + difuse + specular;
	vec3 textureColor = texture(mosaic, txCoord).rgb;

	float zlight = texture(depthTexture, depthTextureCoord).r; // z hodnota ke světlu nejbližšího pixelu na této pozici

	float zActual = depthTextureCoord.z;

	bool shadow = zActual > zlight + 0.001;

	if(shadow){
		outColor = vec4(ambient * textureColor, 1.0);
	} else {
		outColor = vec4(colorIntensity * textureColor, 1.0);
	}


	//outColor = vec4(colorIntensity * textureColor, 1.0);

	//outColor = vec4(normalize(normal), 1.0); zobrazeni barvy podle normaly

	//outColor = vec4(1.0, 0.0, 0.0, 1.0);
	//outColor = texture(mosaic, txCoord);
} 
