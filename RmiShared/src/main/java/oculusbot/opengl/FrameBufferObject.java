package oculusbot.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.ARBFramebufferObject.*;

/**
 * Implementation of frame buffer objects (FBO). FBO are used to render objects
 * to a texture so that a complete frame can be rendered before it is rendered
 * to the output.
 * 
 * @author Robert Meschkat
 *
 */
public class FrameBufferObject {
	private int texture;
	private int width;
	private int height;
	private int framebuffer;
	private int renderbuffer;

	/**
	 * Get the created texture.
	 * 
	 * @return
	 */
	public int getTexture() {
		return texture;
	}

	/**
	 * Get the width of the FBO.
	 * 
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Get the height of the FBO.
	 * 
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Creates a Frame Buffer Object (FBO). 
	 * @param width width of the FBO
	 * @param height height of the FBO
	 * @param texture OpenGL texture ID for the texture which will be used as output
	 */
	public FrameBufferObject(int width, int height, int texture) {
		this.width = width;
		this.height = height;
		this.texture = texture;

		//create the actual fbo
		framebuffer = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
		glBindTexture(GL_TEXTURE_2D, texture);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);

		//create a render buffer and attach it to the FBO.
		//the renderbuffer will contain the actual output
		renderbuffer = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, renderbuffer);
		//TODO check if it needs to be GL_DEPTH_COMPONENT24
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderbuffer);

		//unbind everything after configuration is complete
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	/**
	 * Call this method before rendering so that the output will be the texture
	 * of this FBO.
	 */
	public void bind() {
		glViewport(0, 0, width, height);
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
	}

	/**
	 * Call this method after everything was rendered to the texture of this FBO.
	 */
	public void unbind() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
}
