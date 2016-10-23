package oculusbot.opengl;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Utility-class to create an OpenGL-shader program. 
 * @author Robert Meschkat
 *
 */
public class ShaderUtils {

	/**
	 * Creates a shader program which can be used by OpenGL.
	 * 
	 * @param vertexShaderFilename
	 * @param fragmentShaderFilename
	 * @return handler for the shader program as int
	 */
	public static int createShaderProgram(String vertexShaderFilename, String fragmentShaderFilename)
			throws FileNotFoundException {
		//load file as a String
		String vertexShaderSrc;
		String fragmentShaderSrc;
		vertexShaderSrc = loadFile(vertexShaderFilename);
		fragmentShaderSrc = loadFile(fragmentShaderFilename);

		//create program which will consist of the vertex and fragment shader
		int program = glCreateProgram();
		//create the shaders and attach them to the shader program
		int vertexShader = createShader(GL_VERTEX_SHADER, vertexShaderSrc);
		int fragmentShader = createShader(GL_FRAGMENT_SHADER, fragmentShaderSrc);
		glAttachShader(program, vertexShader);
		glAttachShader(program, fragmentShader);
		glLinkProgram(program);

		//check if shaders were linked correctly to the shader program
		int status = glGetShaderi(program, GL_LINK_STATUS);
		if (status == GL_FALSE) {
			System.err.println("Linker failure: " + glGetProgramInfoLog(program));
		}

		glDetachShader(program, vertexShader);
		glDetachShader(program, fragmentShader);

		return program;
	}
	
	/**
	 * Creates a shader program which can be used by OpenGL.
	 * 
	 * @param vertexShaderStream
	 * @param fragmentShaderStream
	 * @return handler for the shader program as int
	 */
	public static int createShaderProgram(InputStream vertexShaderStream, InputStream fragmentShaderStream)
			throws FileNotFoundException {
		String vertexShaderSrc;
		String fragmentShaderSrc;
		vertexShaderSrc = loadFile(vertexShaderStream);
		fragmentShaderSrc = loadFile(fragmentShaderStream);

		int program = glCreateProgram();
		int vertexShader = createShader(GL_VERTEX_SHADER, vertexShaderSrc);
		int fragmentShader = createShader(GL_FRAGMENT_SHADER, fragmentShaderSrc);
		glAttachShader(program, vertexShader);
		glAttachShader(program, fragmentShader);
		glLinkProgram(program);

		int status = glGetShaderi(program, GL_LINK_STATUS);
		if (status == GL_FALSE) {
			System.err.println("Linker failure: " + glGetProgramInfoLog(program));
		}

		glDetachShader(program, vertexShader);
		glDetachShader(program, fragmentShader);

		return program;
	}

	/**
	 * Tries to create a shader from the source code which can be linked to a
	 * shader program.
	 * 
	 * @param shaderType
	 *            - type of the shader
	 * @param shaderSrc
	 *            - source code of the shader
	 * @return shader handler as int
	 */
	private static int createShader(int shaderType, String shaderSrc) {
		//create shader and compile the shader source
		int shader = glCreateShader(shaderType);
		glShaderSource(shader, shaderSrc);
		glCompileShader(shader);

		//check if shader compiled correctly
		int status = glGetShaderi(shader, GL_COMPILE_STATUS);
		if (status == GL_FALSE) {
			System.err.println("Error in shader: " + glGetShaderInfoLog(shader));
			System.err.println("\nSource:\n" + shaderSrc);
		}

		return shader;
	}

	/**
	 * Loads a text file.
	 * 
	 * @param filename
	 *            - name/path for the file
	 * @return Content of the file as string with newlines
	 * @throws FileNotFoundException
	 */
	private static String loadFile(String filename) throws FileNotFoundException {
		return loadFile(new FileInputStream(filename));
	}
	
	/**
	 * Loads a text file.
	 * 
	 * @param filename
	 *            - name/path for the file
	 * @return Content of the file as string with newlines
	 * @throws FileNotFoundException
	 */
	private static String loadFile(InputStream stream) throws FileNotFoundException {
		StringBuffer buffer = new StringBuffer();
		Scanner in = new Scanner(stream);
		while (in.hasNextLine()) {
			buffer.append(in.nextLine() + "\n");
		}
		in.close();
		return buffer.toString();
	}

}
