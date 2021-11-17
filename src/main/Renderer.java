package main;

import lwjglutils.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import transforms.*;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;


/**
 * @author PGRF FIM UHK
 * @version 2.0
 * @since 2019-09-02
 */
public class Renderer extends AbstractRenderer {

	private int shaderProgramViewer, shaderProgramLight;
	private OGLBuffers buffers;
	private OGLRenderTarget renderTarget;

	private Camera camera, cameraLight;
	private Mat4 projection;
	private int locView, locProjection, locSolid, locLightPosition, locEyePosition, locLightVP;
	private int  locViewLight, locProjectionLight, locSolidLight;

	private int locTime, locTimeLight;
	private float time = 0;

	private OGLTexture2D mosaictexture;
	private OGLTexture.Viewer viewer;


	@Override
	public void init() {

		OGLUtils.printOGLparameters();
		OGLUtils.printJAVAparameters();
		OGLUtils.printLWJLparameters();
		OGLUtils.shaderCheck();

		glClearColor(0.1f , 0.1f, 0.1f, 1.0f);
		shaderProgramViewer = ShaderUtils.loadProgram("/start");
		shaderProgramLight = ShaderUtils.loadProgram("/light");

		locView = glGetUniformLocation(shaderProgramViewer,"view");
		locProjection = glGetUniformLocation(shaderProgramViewer, "projection");
		locSolid = glGetUniformLocation(shaderProgramViewer,"solid");
		locLightPosition = glGetUniformLocation(shaderProgramViewer,"lightPosition");
		locEyePosition = glGetUniformLocation(shaderProgramViewer,"eyePosition");
		locLightVP = glGetUniformLocation(shaderProgramViewer,"lightVP");
		locTime = glGetUniformLocation(shaderProgramViewer, "time");

		locViewLight = glGetUniformLocation(shaderProgramLight, "view");
		locProjectionLight = glGetUniformLocation(shaderProgramLight,"projection");
		locSolidLight = glGetUniformLocation(shaderProgramLight,"solid");
		locTimeLight = glGetUniformLocation(shaderProgramLight, "time");

		camera = new Camera().
				withPosition(new Vec3D(-3,3,3)).
				withAzimuth(-1/4.0 * Math.PI).
				withZenith(-1.3/5.0 * Math.PI);

		projection = new Mat4PerspRH(Math.PI / 3, height / (float) width, 1.0, 20.0);
		//projection = new Mat4OrthoRH(,, 1.0, 20.0);

		buffers = GridFactory.createGrid(6, 6);
		renderTarget = new OGLRenderTarget(1024, 1024);
		viewer  = new OGLTexture2D.Viewer();

		cameraLight = new Camera().withPosition(new Vec3D(5,5,5)).
				withAzimuth(5/4f * Math.PI).
				withZenith(-1/5f * Math.PI);

		textRenderer = new OGLTextRenderer(width, height);

		try {
			mosaictexture = new OGLTexture2D("textures/mosaic.jpg");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void display() {
		glEnable(GL_DEPTH_TEST); //zapnout z-buffer (kvůli text rendereru)
		cameraLight.left(0.1);
		time += 0.01;

		renderFromLight();
		renderFromViewer();

		//glPolygonMode(GL_FRONT,GL_LINE);
		//glPolygonMode(GL_BACK,GL_FILL);

		viewer.view(renderTarget.getDepthTexture(), -1, -1, 0.7);
		viewer.view(renderTarget.getColorTexture(), -1, -0.3, 0.7);


		textRenderer.addStr2D(width-90, height - 5, "test");
	}

	private void renderFromLight() {
		glUseProgram(shaderProgramLight);
		renderTarget.bind();

		glClearColor(0.5f, 0,0,1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glUniformMatrix4fv(locViewLight, false, cameraLight.getViewMatrix().floatArray());
		glUniformMatrix4fv(locProjectionLight,false,projection.floatArray());
		glUniform1f(locTimeLight, time);

		glUniform1i(locSolidLight,1);
		buffers.draw(GL_TRIANGLES,shaderProgramLight);

		glUniform1i(locSolidLight,2);
		buffers.draw(GL_TRIANGLES,shaderProgramLight);
	}

	private void renderFromViewer() {
		glUseProgram(shaderProgramViewer);
		//výchozí framebuffer - render do obrazovky
		glBindFramebuffer(GL_FRAMEBUFFER,0);

		glPolygonMode(GL_FRONT_AND_BACK,GL_FILL);
		//nutno opravit viewport, prootže render target si nastavuje vlastní
		glViewport(0,0,width,height);

		glClearColor(0f, 0.5f,0,1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		renderTarget.getDepthTexture().bind(shaderProgramViewer, "depthTexture",1);
		mosaictexture.bind(shaderProgramViewer,"mosaic", 0);

		glUniform3fv(locLightPosition, ToFloatArray.convert(cameraLight.getPosition()));
		glUniform3fv(locEyePosition, ToFloatArray.convert(camera.getEye()));
		glUniform1f(locTime, time);

		glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
		glUniformMatrix4fv(locProjection, false, projection.floatArray());
		glUniformMatrix4fv(locLightVP,false, cameraLight.getViewMatrix().mul(projection).floatArray());

		glUniform1i(locSolid,1);
		buffers.draw(GL_TRIANGLES, shaderProgramViewer);

		glUniform1i(locSolid,2);
		buffers.draw(GL_TRIANGLES, shaderProgramViewer);
	}

	@Override
	public GLFWWindowSizeCallback getWsCallback() {
		return new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int w, int h) {
				if (w > 0 && h > 0) {
					Renderer.this.width = w;
					Renderer.this.height = h;
					System.out.println("Windows resize to [" + w + ", " + h + "]");
					if (textRenderer != null) {
						textRenderer.resize(Renderer.this.width, Renderer.this.height);
					}
					projection = new Mat4PerspRH(Math.PI / 3, h/ (float) w, 1.0, 20.0);

				}
			}
		};
	}

	@Override
	public GLFWCursorPosCallback getCursorCallback(){
		return new GLFWCursorPosCallback(){
			@Override
			public void invoke(long window, double xpos, double ypos){

			}
		};
	}
}