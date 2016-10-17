package oculusbot.opengl.texture;

import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ROW_LENGTH;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import oculusbot.video.ReceiveVideoThread;

public class MatTexture {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private Mat frame;
	private ByteBuffer buffer;
	private ReceiveVideoThread video;
	private int texture;

	public MatTexture(ReceiveVideoThread video) {
		this.video = video;
	}

	public int grabTexture() {
		frame = video.getFrame();

		int size = frame.rows() * frame.cols() * 3;
		buffer = BufferUtils.createByteBuffer(size);
		byte[] data = new byte[size];
		frame.get(0, 0, data);
		buffer.put(data).flip();

		glDeleteTextures(texture);
		
		texture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texture);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
		glPixelStorei(GL_UNPACK_ROW_LENGTH, (int) (frame.step1() / frame.elemSize()));
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, frame.cols(), frame.rows(), 0, GL12.GL_BGR, GL_UNSIGNED_BYTE, buffer);

		return texture;
	}
	
}
