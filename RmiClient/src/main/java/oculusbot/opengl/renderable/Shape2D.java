package oculusbot.opengl.renderable;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

import oculusbot.opengl.Renderable;
import oculusbot.opengl.ShaderUtils;

public class Shape2D implements Renderable{
	private static final int FLOAT_SIZE = 4;
	private int program;
	private int buffer;
	private float[] data;
	private int vertexCount;
	
	public Shape2D(float[] data, int vertexCount){
		this.data = data;
		this.vertexCount = vertexCount;
	}
	
	public void init() {
		
		FloatBuffer shape = BufferUtils.createFloatBuffer(data.length);
		shape.put(data).flip();
		
		buffer = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, buffer);
		glBufferData(GL_ARRAY_BUFFER, shape, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		try{
			program = ShaderUtils.createShaderProgram("color.vert", "color.frag");
		} catch (FileNotFoundException e){
			e.printStackTrace();
		}
	}

	public void render() {
		glBindBuffer(GL_ARRAY_BUFFER, buffer);
		glUseProgram(program);
		
		int position = glGetAttribLocation(program, "position");
		glEnableVertexAttribArray(position);
		glVertexAttribPointer(position, 2, GL_FLOAT, false, FLOAT_SIZE*5, 0); // 20 (stride) = 4 (bytes per float) * 5 (floats per vertex)
		
		int color = glGetAttribLocation(program, "color");
		glEnableVertexAttribArray(color);
		glVertexAttribPointer(color, 3, GL_FLOAT, false, FLOAT_SIZE*5, FLOAT_SIZE*2);
		
		glDrawArrays(GL_TRIANGLE_STRIP, 0, vertexCount);
		glDisableVertexAttribArray(position);
		glDisableVertexAttribArray(color);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
