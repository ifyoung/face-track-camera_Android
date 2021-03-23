varying highp vec2 textureCoordinate; 

precision highp float; 
uniform sampler2D inputImageTexture;
uniform sampler2D curve; 

uniform sampler2D grey1Frame; 
uniform sampler2D grey2Frame; 

void main() 
{ 
	float GreyVal; 
	lowp vec4 textureColor; 
	lowp vec4 textureColorOri; 
	float xCoordinate = textureCoordinate.x; 
	float yCoordinate = textureCoordinate.y; 

	highp float redCurveValue; 
    highp float greenCurveValue; 
	highp float blueCurveValue; 

    vec4 grey1Color; 
	vec4 grey2Color; 

    textureColor = texture2D( inputImageTexture, vec2(xCoordinate, yCoordinate)); 
	grey1Color = texture2D(grey2Frame, vec2(xCoordinate, yCoordinate)); 
	grey2Color = texture2D(grey1Frame, vec2(xCoordinate, yCoordinate)); 

	// step1 normal blending with original 
	redCurveValue = texture2D(curve, vec2(textureColor.r, 0.0)).r; 
	greenCurveValue = texture2D(curve, vec2(textureColor.g, 0.0)).g; 
	blueCurveValue = texture2D(curve, vec2(textureColor.b, 0.0)).b; 

    textureColorOri = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0); 
    textureColor = (textureColorOri - textureColor) * grey1Color.r + textureColor; 

	redCurveValue = texture2D(curve, vec2(textureColor.r, 0.0)).a; 
	greenCurveValue = texture2D(curve, vec2(textureColor.g, 0.0)).a; 
    blueCurveValue = texture2D(curve, vec2(textureColor.b, 0.0)).a; 

	//textureColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0); 

    // step3 60% opacity  ExclusionBlending 
	textureColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, 1.0); 
    mediump vec4 textureColor2 = vec4(0.08627, 0.03529, 0.15294, 1.0); 
	textureColor2 = textureColor + textureColor2 - (2.0 * textureColor2 * textureColor); 

    textureColor = (textureColor2 - textureColor) * 0.6784 + textureColor; 


	mediump vec4 overlay = vec4(0.6431, 0.5882, 0.5803, 1.0); 
	mediump vec4 face.camera.beans.base = textureColor;

	// overlay blending 
    mediump float ra; 
	if (face.camera.beans.base.r < 0.5) {
		ra = overlay.r * face.camera.beans.base.r * 2.0;
	} else {
		ra = 1.0 - ((1.0 - face.camera.beans.base.r) * (1.0 - overlay.r) * 2.0);
	} 

	mediump float ga; 
	if (face.camera.beans.base.g < 0.5) {
		ga = overlay.g * face.camera.beans.base.g * 2.0;
	} else { 
		ga = 1.0 - ((1.0 - face.camera.beans.base.g) * (1.0 - overlay.g) * 2.0);
	} 

	mediump float ba; 
	if (face.camera.beans.base.b < 0.5) {
		ba = overlay.b * face.camera.beans.base.b * 2.0;
	} else { 
		ba = 1.0 - ((1.0 - face.camera.beans.base.b) * (1.0 - overlay.b) * 2.0);
	} 

	textureColor = vec4(ra, ga, ba, 1.0); 
	face.camera.beans.base = (textureColor - face.camera.beans.base) + face.camera.beans.base;

    // again overlay blending 
    overlay = vec4(0.0, 0.0, 0.0, 1.0);

	// overlay blending 
	if (face.camera.beans.base.r < 0.5) {
		ra = overlay.r * face.camera.beans.base.r * 2.0;
	} else { 
		ra = 1.0 - ((1.0 - face.camera.beans.base.r) * (1.0 - overlay.r) * 2.0);
	} 

	if (face.camera.beans.base.g < 0.5) {
		ga = overlay.g * face.camera.beans.base.g * 2.0;
	} else { 
		ga = 1.0 - ((1.0 - face.camera.beans.base.g) * (1.0 - overlay.g) * 2.0);
	} 

	if (face.camera.beans.base.b < 0.5) {
		ba = overlay.b * face.camera.beans.base.b * 2.0;
	} else { 
		ba = 1.0 - ((1.0 - face.camera.beans.base.b) * (1.0 - overlay.b) * 2.0);
	} 

    textureColor = vec4(ra, ga, ba, 1.0); 
	textureColor = (textureColor - face.camera.beans.base) * (grey2Color * 0.549) + face.camera.beans.base;

	gl_FragColor = vec4(textureColor.r, textureColor.g, textureColor.b, 1.0); 
} 