package oculusbot.opengl.renderable;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import java.io.FileNotFoundException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import oculusbot.opengl.Renderable;
import oculusbot.opengl.ShaderUtils;
import oculusbot.opengl.texture.MatTexture;
import oculusbot.video.Frame;
import oculusbot.video.ReceiveVideoThread;

/**
 * Class that creates a rectangle in OpenGL and renders a {@link oculusbot.opengl.texture.MatTexture MatTexture} to it.
 * @author Robert Meschkat
 *
 */
public class MatCanvas implements Renderable {
	private static final String VERTEX_NAME = "shaders/texture.vert";
	private static final String FRAGMENT_NAME = "shaders/texture.frag";
	private int buffer;
	private int program;
	private int texture;
	private float[] cords;
	private MatTexture matTexture;
	private Frame frame;
	
	public Frame getFrame(){
		return frame;
	}

	public MatCanvas(ReceiveVideoThread video) {
		matTexture = new MatTexture(video);
	}

	public void init() {
		//create shader program
		try {
			program = ShaderUtils.createShaderProgram(getClass().getClassLoader().getResourceAsStream(VERTEX_NAME),
					getClass().getClassLoader().getResourceAsStream(FRAGMENT_NAME));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		//create shape data (a rectangle used as a canvas)
		cords = new float[] { 1f, -1f, 1, 1, -1f, -1f, 0, 1, 1f, 1f, 1, 0, -1f, 1f, 0, 0 };
		FloatBuffer shape = BufferUtils.createFloatBuffer(cords.length);
		shape.put(cords).flip();

		//load shape data
		buffer = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, buffer);
		glBufferData(GL_ARRAY_BUFFER, shape, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	public void render() {
		frame = matTexture.getFrame();
		//get the texture
		texture = matTexture.grabTexture();
		glClear(GL_COLOR_BUFFER_BIT);
		glBindBuffer(GL_ARRAY_BUFFER, buffer);
		glUseProgram(program);

		//load the texture
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texture);
		int textureSampler = glGetUniformLocation(program, "textureSampler");
		glUniform1i(textureSampler, 0);

		//get position from buffer
		int position = glGetAttribLocation(program, "position");
		glEnableVertexAttribArray(position);
		glVertexAttribPointer(position, 2, GL_FLOAT, false, cords.length, 0);

		//get UV from buffer
		int vertexUV = glGetAttribLocation(program, "vertexUV");
		glEnableVertexAttribArray(vertexUV);
		glVertexAttribPointer(vertexUV, 2, GL_FLOAT, false, cords.length, 8);

		//draw canvas with texture
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		glDisableVertexAttribArray(0);
	}

	public void destroy() {

	}

}
