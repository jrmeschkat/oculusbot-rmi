package oculusbot.opengl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.util.LinkedList;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

/**
 * Class to create an OpenGL- and GLFW-based window. Has a list for
 * {@link oculusbot.opengl.Renderable Renderable}-implementations which will be
 * rendered to this window.
 * 
 * @author Robert Meschkat
 *
 */
public class Window {
	public static final int DEFAULT_WIDTH = 800;
	public static final int DEFAULT_HEIGHT = 600;
	private static final String WINDOW_TITLE = "Hello!";

	private GLFWErrorCallback errorCallback;
	private GLFWWindowSizeCallback sizeCallback;
	private GLFWKeyCallback keyCallback;

	private long window;
	private int width = DEFAULT_WIDTH;
	private int height = DEFAULT_HEIGHT;
	private boolean glfwInitComplete = false;
	private boolean resized = false;
	private boolean shouldClose = false;
	private Callback callback;

	private LinkedList<Renderable> renderObjects = new LinkedList<Renderable>();

	/**
	 * Sets window width.
	 * @param width
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Returns window width.
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Set window height.
	 * @param height
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Get window height.
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Set a {@link oculusbot.opengl.Callback Callback}-implementation.
	 * @param callback
	 */
	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	/**
	 * Create a window and set a {@link oculusbot.opengl.Callback Callback}-implementation.
	 * @param callback
	 */
	public Window(Callback callback) {
		this.callback = callback;
	}

	/**
	 * Create a window without a callback.
	 */
	public Window() {
		this(null);
	}

	/**
	 * Create a window without a callback
	 * @param width Window width.
	 * @param height Window height.
	 */
	public Window(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Initialize the window and all registered Renderables.
	 * @throws IllegalStateException
	 * @throws RuntimeException
	 */
	public void init() throws IllegalStateException, RuntimeException {
		//set the error callback to System.err to allow OpenGL to print error messages
		glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));

		//initialize GLFW
		if (glfwInit() != true) {
			throw new IllegalStateException();
		}

		//set default GLFW window options
		glfwDefaultWindowHints();
		//make window invisible for now
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE);

		//create actual window and save a handler as a long
		window = glfwCreateWindow(width, height, WINDOW_TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
		if (window == MemoryUtil.NULL) {
			throw new RuntimeException("Failed to create window.");
		}

		sizeCallback = GLFWWindowSizeCallback.create(new GLFWWindowSizeCallbackI() {

			public void invoke(long window, int w, int h) {
				width = w;
				height = h;
				resized = true;
			}
		});

		keyCallback = GLFWKeyCallback.create(new GLFWKeyCallbackI() {

			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (action == GLFW_RELEASE) {
					if (key == GLFW_KEY_ESCAPE) {
						//quit program if escape is pressed
						glfwSetWindowShouldClose(window, true);
						shouldClose = true;
					} else {
						if (callback != null) {
							callback.keyPressed(window, key, scancode, action, mods);
						}
					}
				}
			}
		});

		//create window size callback to react to window resizes
		glfwSetWindowSizeCallback(window, sizeCallback);

		//create key callback to react to pressed keys
		glfwSetKeyCallback(window, keyCallback);

		//make some necessary method calls to complete GLFW initialization
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);

		GL.createCapabilities();

		for (Renderable r : renderObjects) {
			r.init();
		}

		glfwInitComplete = true;
	}

	/**
	 * Contains the main loop for this GLFW window.
	 * 
	 * @throws IllegalStateException
	 */
	public void render() throws IllegalStateException {

		//check if window should close
		if (glfwWindowShouldClose(window) == true) {
			shouldClose = true;
			return;
		}

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		//check if window should be resized
		if (resized) {
			glViewport(0, 0, width, height);
			resized = false;
		}

		//render all registered Renderables.
		for (Renderable r : renderObjects) {
			r.render();
		}

		glfwSwapBuffers(window);
		glfwPollEvents();

	}

	/**
	 * Destroys all registered Renderables and closes the window.
	 */
	public void destroy() {
		for (Renderable r : renderObjects) {
			r.destroy();
		}

		errorCallback.free();
		sizeCallback.free();
		keyCallback.free();
		glfwTerminate();
	}

	/**
	 * Call this method to initialize window and start draw loop.
	 */
	public void start() {
		try {
			init();
			if (!glfwInitComplete) {
				throw new IllegalStateException("GLFW initialzion not complete. Call \"glfwStart()\" first.");
			}

			while (glfwWindowShouldClose(window) == false) {
				render();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			destroy();
		}
	}

	/**
	 * <b>Must be called before start() at the moment.</b>
	 * 
	 * @param r
	 */
	public void register(Renderable r) {
		renderObjects.add(r);
		if (glfwInitComplete) {
			r.init();
		}
	}

	public boolean shouldClose() {
		return shouldClose;
	}

}
