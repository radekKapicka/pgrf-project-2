package main;

import lwjglutils.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import transforms.*;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
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

	private double oldMx, oldMy;
	private boolean mousePressed,animation;
	private int myPolygonMode = GL_FILL;

	private int shaderProgramViewer, shaderProgramLight;
	private OGLBuffers buffers;
	private OGLRenderTarget renderTarget;

	private Camera camera, cameraLight;
	private Mat4 projection;
	private int locView, locProjection, locSolid, locLightPosition, locEyePosition, locLightVP;
	private int  locViewLight, locProjectionLight, locSolidLight;

	private int locTime, locTimeLight;
	private float time = 0;

	private int locVisMode;
	private int visMode = 0;

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
		locVisMode = glGetUniformLocation(shaderProgramViewer, "visMode");

		locViewLight = glGetUniformLocation(shaderProgramLight, "view");
		locProjectionLight = glGetUniformLocation(shaderProgramLight,"projection");
		locSolidLight = glGetUniformLocation(shaderProgramLight,"solid");
		locTimeLight = glGetUniformLocation(shaderProgramLight, "time");

		camera = new Camera().
				withPosition(new Vec3D(-3,3,3)).
				withAzimuth(-1/4.0 * Math.PI).
				withZenith(-1.3/5.0 * Math.PI);

		projection = new Mat4PerspRH(Math.PI / 3, height / (float) width, 1.0, 20.0);

		buffers = GridFactory.createGrid(30, 30);
		renderTarget = new OGLRenderTarget(1024, 1024);
		viewer  = new OGLTexture2D.Viewer();

		cameraLight = new Camera().withPosition(new Vec3D(7,7,7)).
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
		//cameraLight.left(time);
		if(animation){
			time += 0.01;
		}else{
			time += 0;
		}

		renderFromLight();
		renderFromViewer();

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

		glUniform1i(locSolidLight,3);
		buffers.draw(GL_TRIANGLES, shaderProgramLight);

		glUniform1i(locSolidLight,4);
		buffers.draw(GL_TRIANGLES, shaderProgramLight);

		glUniform1i(locSolidLight,5);
		buffers.draw(GL_TRIANGLES, shaderProgramLight);

		glUniform1i(locSolidLight,6);
		buffers.draw(GL_TRIANGLES, shaderProgramLight);
	}

	private void renderFromViewer() {
		glUseProgram(shaderProgramViewer);
		//výchozí framebuffer - render do obrazovky
		glBindFramebuffer(GL_FRAMEBUFFER,0);

		glPolygonMode(GL_FRONT_AND_BACK,myPolygonMode);
		//nutno opravit viewport, prootže render target si nastavuje vlastní
		glViewport(0,0,width,height);

		glClearColor(0f, 0.5f,0,1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		renderTarget.getDepthTexture().bind(shaderProgramViewer, "depthTexture",1);
		mosaictexture.bind(shaderProgramViewer,"mosaic", 0);

		glUniform3fv(locLightPosition, ToFloatArray.convert(cameraLight.getPosition()));
		glUniform3fv(locEyePosition, ToFloatArray.convert(camera.getEye()));
		glUniform1f(locTime, time);
		glUniform1i(locVisMode, visMode);


		glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
		glUniformMatrix4fv(locProjection, false, projection.floatArray());
		glUniformMatrix4fv(locLightVP,false, cameraLight.getViewMatrix().mul(projection).floatArray());

		glUniform1i(locSolid,1);
		buffers.draw(GL_TRIANGLES, shaderProgramViewer);

		glUniform1i(locSolid,2);
		buffers.draw(GL_TRIANGLES, shaderProgramViewer);

		glUniform1i(locSolid,3);
		buffers.draw(GL_TRIANGLES, shaderProgramViewer);

		glUniform1i(locSolid,4);
		buffers.draw(GL_TRIANGLES, shaderProgramViewer);

		glUniform1i(locSolid,5);
		buffers.draw(GL_TRIANGLES, shaderProgramViewer);

		glUniform1i(locSolid,6);
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
		return cursorPosCallback;
	}

	@Override
	public GLFWMouseButtonCallback getMouseCallback() {
		return mouseButtonCallback;
	}

	@Override
	public GLFWKeyCallback getKeyCallback() {
		return keyCallback;
	}

	private final GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
		@Override
		public void invoke(long window, double x, double y) {
			if (mousePressed) {
				camera = camera.addAzimuth(Math.PI * (oldMx - x) / LwjglWindow.WIDTH);
				camera = camera.addZenith(Math.PI * (oldMy - y) / LwjglWindow.HEIGHT);
				oldMx = x;
				oldMy = y;
			}
		}
	};

	private final GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
		@Override
		public void invoke(long window, int button, int action, int mods) {
			if (button == GLFW_MOUSE_BUTTON_LEFT) {
				double[] xPos = new double[1];
				double[] yPos = new double[1];
				glfwGetCursorPos(window, xPos, yPos);
				oldMx = xPos[0];
				oldMy = yPos[0];
				mousePressed = (action == GLFW_PRESS);
			}
		}
	};

	private final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if (action == GLFW_PRESS || action == GLFW_REPEAT) {
				switch (key) {
					case GLFW_KEY_A -> camera = camera.left(0.1);
					case GLFW_KEY_D -> camera = camera.right(0.1);
					case GLFW_KEY_W -> camera = camera.forward(0.1);
					case GLFW_KEY_S -> camera = camera.backward(0.1);
					case GLFW_KEY_LEFT_CONTROL -> camera = camera.up(0.1);
					case GLFW_KEY_LEFT_SHIFT -> camera = camera.down(0.1);
					case GLFW_KEY_O -> projection = new Mat4OrthoRH(15,15, 1.0, 200.0);
					case GLFW_KEY_P -> projection = new Mat4PerspRH(Math.PI / 3, height / (float) width, 1.0, 20.0);
					case GLFW_KEY_L -> myPolygonMode = GL_LINE;
					case GLFW_KEY_F -> myPolygonMode = GL_FILL;
					case GLFW_KEY_B -> myPolygonMode = GL_POINT;
					case GLFW_KEY_X -> animation = true;
					case GLFW_KEY_C -> animation = false;
					case GLFW_KEY_1 -> visMode = 0;
					case GLFW_KEY_2 -> visMode = 1;
					case GLFW_KEY_3 -> visMode = 2;
					case GLFW_KEY_4 -> visMode = 3;
					case GLFW_KEY_5 -> visMode = 4;

				}
			}
		}
	};


}