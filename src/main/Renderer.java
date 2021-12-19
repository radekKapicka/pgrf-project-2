package main;

import lwjglutils.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import transforms.*;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static lwjglutils.ShaderUtils.TESSELATION_SUPPORT_VERSION;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30C.glBindFramebuffer;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL40.GL_PATCHES;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL40.GL_MAX_PATCH_VERTICES;
import static org.lwjgl.opengl.GL40.GL_PATCH_VERTICES;
import static org.lwjgl.opengl.GL40.glPatchParameteri;


/**
 * @author PGRF FIM UHK
 * @version 2.0
 * @since 2019-09-02
 */
public class Renderer extends AbstractRenderer {

	private double oldMx, oldMy;
	private boolean mousePressed;
	private int myPolygonMode = GL_LINE;

	private int shaderProgramViewer;
	private OGLBuffers buffers, buffersLine;
	private OGLRenderTarget renderTarget;

	private Camera camera;
	private Mat4 projection;

	private int locView, locProjection, locEyePosition, locDemoType;

	private int locIter;
	private int iter = 1;

	int demoType = 0;
	boolean demoTypeChanged = true;

	List<Integer> indexBufferDataLine;
	List<Vec2D> vertexBufferDataPosLine;
	List<Vec3D> vertexBufferDataColLine;
	boolean update = true, mode = false;

	//zde vytvarim a plnim buffery pro grid, ten je z pocatku definovan ctyrmi body a tvori ho 2 trojuhelniky
	void createBuffers() {
		int[] indexBufferData = { 0,1,2,0,3,2};

		float[] vertexBufferDataPos = {
				-0.8f, -0.6f,
				-0.8f, 0.6F,
				0.8f, 0.6f,
				0.8f, -0.6F,
		};

		float[] vertexBufferDataCol = {
				1, 0, 1,
				0, 1, 1,
				1, 0, 0,
				1,1,0
		};

		OGLBuffers.Attrib[] attributesPos = {
				new OGLBuffers.Attrib("inPosition", 2),
		};
		OGLBuffers.Attrib[] attributesCol = {
				new OGLBuffers.Attrib("inColor", 3)
		};
		buffers = new OGLBuffers(vertexBufferDataPos, attributesPos,
				indexBufferData);
		buffers.addVertexBuffer(vertexBufferDataCol, attributesCol);
	}

	//zde tvorim buffery pro linku
	void initBuffers() {
		indexBufferDataLine = new ArrayList<>();
		vertexBufferDataPosLine = new ArrayList<>();
		vertexBufferDataColLine = new ArrayList<>();

		vertexBufferDataPosLine.add(new Vec2D(-0.5f, 0.0f));
		vertexBufferDataPosLine.add(new Vec2D(0.0f, 0.5));
		vertexBufferDataPosLine.add(new Vec2D(0.0f, -0.5f));
		vertexBufferDataPosLine.add(new Vec2D(0.5f, 0.0f));
		vertexBufferDataPosLine.add(new Vec2D(0.7f, 0.5f));
		vertexBufferDataPosLine.add(new Vec2D(0.9f, -0.7f));

		Random r = new Random();
		for(int i = 0; i < vertexBufferDataPosLine.size(); i++){
			indexBufferDataLine.add(i);
			vertexBufferDataColLine.add(new Vec3D(r.nextDouble(),r.nextDouble(),r.nextDouble()));
		}
	}

	//zde probíhá update bufferů linky v pripade, ze generuji dalsi vrcholy
	void updateBuffers() {
		OGLBuffers.Attrib[] attributesPos = {
				new OGLBuffers.Attrib("inPosition", 2), };
		OGLBuffers.Attrib[] attributesCol = {
				new OGLBuffers.Attrib("inColor", 3)
		};

		buffersLine = new OGLBuffers(ToFloatArray.convert(vertexBufferDataPosLine), attributesPos,
				ToIntArray.convert(indexBufferDataLine));
		buffersLine.addVertexBuffer(ToFloatArray.convert(vertexBufferDataColLine), attributesCol);
	}

	private int init(int demoType){
		int newShaderProgram = 0;
		switch (demoType){
			case 0: //v tomto modu se zobrazuje grid pomoci VS, FS a tess
				if (OGLUtils.getVersionGLSL() >= TESSELATION_SUPPORT_VERSION) {
					newShaderProgram = ShaderUtils.loadProgram(
							"/start",
							"/start",
							null,
							"/start",
							"/start",
							null);
				}
				else
					System.out.println("Tesselation is not supported");
				break;
			case 1: //v tomto modu se zobrazuji body gridu, ktere maji posunute hranice,aby byly lepe videt (tvori je 2 trpjuhelniky), pomoci VS, FS a GS
				if (OGLUtils.getVersionGLSL() >= TESSELATION_SUPPORT_VERSION) {
					newShaderProgram = ShaderUtils.loadProgram(
							"/start");
				}
				else
					System.out.println("Tesselation is not supported");
				break;
			case 2: //v tomto modu se zobrazuje krivka pomoci VS, FS, a GS pro krivku
				if (OGLUtils.getVersionGLSL() >= TESSELATION_SUPPORT_VERSION) {
					newShaderProgram = ShaderUtils.loadProgram(
							"/start","/start","/startLine",null,null,null);
				}
				else
					System.out.println("Tesselation is not supported");
				break;
			default: //defaultne se zobrazuji vsechny shadery krome startLine.geom
				if (OGLUtils.getVersionGLSL() >= TESSELATION_SUPPORT_VERSION) {
					newShaderProgram = ShaderUtils.loadProgram(
							"/start");
				}
				else
					System.out.println("Tesselation is not supported");
		}

		return newShaderProgram;

	}


	@Override
	public void init() {

		OGLUtils.printOGLparameters();
		OGLUtils.printJAVAparameters();
		OGLUtils.printLWJLparameters();
		OGLUtils.shaderCheck();

		glClearColor(0.1f , 0.1f, 0.1f, 1.0f);

		shaderProgramViewer = ShaderUtils.loadProgram("/start");

		locView = glGetUniformLocation(shaderProgramViewer,"view");
		locProjection = glGetUniformLocation(shaderProgramViewer, "projection");
		locEyePosition = glGetUniformLocation(shaderProgramViewer,"eyePosition");

		if (OGLUtils.getVersionGLSL() >= TESSELATION_SUPPORT_VERSION) {
			int[] maxPatchVertices = new int[1];
			glGetIntegerv(GL_MAX_PATCH_VERTICES, maxPatchVertices);
			System.out.println("Max supported patch vertices "	+ maxPatchVertices[0]);
		}
		initBuffers();
		createBuffers();

		camera = new Camera().
				withPosition(new Vec3D(-2,2,2.5)).
				withAzimuth(-1/4.0 * Math.PI).
				withZenith(-1.3/5.0 * Math.PI);

		projection = new Mat4PerspRH(Math.PI / 3, height / (float) width, 1.0, 20.0);

		renderTarget = new OGLRenderTarget(1024, 1024);

		textRenderer = new OGLTextRenderer(width, height);

	}

	@Override
	public void display() {
		glEnable(GL_DEPTH_TEST); //zapnout z-buffer (kvůli text rendereru)

		//podminka pro vyber shaderu u gridu
		if (demoTypeChanged) {
			int oldShaderProgram = shaderProgramViewer;
			shaderProgramViewer = init(demoType);
			if (shaderProgramViewer>0) {
				glDeleteProgram(oldShaderProgram);
			} else {
				shaderProgramViewer = oldShaderProgram;
			}
			locIter = glGetUniformLocation(shaderProgramViewer, "iter");
			locDemoType = glGetUniformLocation(shaderProgramViewer, "demoType");
			demoTypeChanged = false;
		}

		//podminka pro update bufferu u krivky
		if (update) {
			updateBuffers();
			update = false;
		}

		renderFromViewer();
		textRenderer.addStr2D(width-90, height - 5, "test");
	}


	private void renderFromViewer() {
		glUseProgram(shaderProgramViewer);
		//výchozí framebuffer - render do obrazovky
		glBindFramebuffer(GL_FRAMEBUFFER,0);

		glPolygonMode(GL_FRONT_AND_BACK,myPolygonMode);
		glViewport(0,0,width,height);

		glClearColor(0f, 0.f,0,0.7f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glUniform1i(locIter, iter);
		glUniform1i(locDemoType, demoType);

		glUniform3fv(locEyePosition, ToFloatArray.convert(camera.getEye()));
		glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
		glUniformMatrix4fv(locProjection, false, projection.floatArray());

		buffersLine.draw(GL_LINE_STRIP_ADJACENCY, shaderProgramViewer, indexBufferDataLine.size());

		//zde se urcuje k jakemu modu zobrazeni se prida jaka metoda draw a jeji atributy
		switch (demoType){
			case 0: //case 0 a 1 je tvoren pomoci GL_Patches
			case 1:
				if (OGLUtils.getVersionGLSL() >= 400){
					glPatchParameteri(GL_PATCH_VERTICES, 3);
					buffers.draw(GL_PATCHES, shaderProgramViewer);
				}
				break;
			case 2:
				break;
			default: //defaultne je tvoren pomoci GL_Patches
				buffers.draw(GL_TRIANGLES, shaderProgramViewer);
				break;
		}

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
			if (button==GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS){
				DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
				DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
				glfwGetCursorPos(window, xBuffer, yBuffer);
				double mouseX = (xBuffer.get(0) / (double) width) * 2 - 1;
				double mouseY = ((height - yBuffer.get(0)) / (double) height) * 2 - 1;
				indexBufferDataLine.add(indexBufferDataLine.size());
				vertexBufferDataPosLine.add(new Vec2D(mouseX, mouseY));
				vertexBufferDataColLine.add(new Vec3D(mouseX / 2 + 0.5, mouseY / 2 + 0.5, 1));
				update = true;
			}
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
					case GLFW_KEY_L -> myPolygonMode = GL_LINE;
					case GLFW_KEY_F -> myPolygonMode = GL_FILL;
					case GLFW_KEY_B -> myPolygonMode = GL_POINT;
					case GLFW_KEY_UP -> iter=iter + 1;
					case GLFW_KEY_DOWN -> iter= iter-1;
					case GLFW_KEY_M -> {
						demoType = (demoType+1) % 3;
						demoTypeChanged = true;
					}


				}
			}
		}
	};


}