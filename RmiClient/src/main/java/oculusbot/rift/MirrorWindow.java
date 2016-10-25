package oculusbot.rift;

import oculusbot.opengl.Renderable;

import static org.lwjgl.opengl.ARBFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Puts the mirror frame buffer created by the {@link oculusbot.rift.Rift Rift}
 * in a context so it can be rendered by the mirror window.
 * 
 * @author Robert Meschkat
 *
 */
public class MirrorWindow implements Renderable {
	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;
	private int framebuffer;
	private int width;
	private int height;

	/**
	 * Creates a Renderable for the mirror window.
	 * 
	 * @param framebuffer
	 *            The mirror frame buffer from the Rift.
	 */
	public MirrorWindow(int framebuffer) {
		this(framebuffer, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	/**
	 * @see #MirrorWindow(int)
	 * @param framebuffer
	 * @param width
	 *            Width of the frame buffer
	 * @param height
	 *            Height of the frame buffer
	 */
	public MirrorWindow(int framebuffer, int width, int height) {
		this.framebuffer = framebuffer;
		this.width = width;
		this.height = height;
	}

	public void init() {
	}

	public void render() {
		glBindFramebuffer(GL_READ_FRAMEBUFFER, framebuffer);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
		glBlitFramebuffer(0, height, width, 0, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
	}

	public void destroy() {
	}

}
