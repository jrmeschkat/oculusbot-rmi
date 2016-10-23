package oculusbot.opengl;

/**
 * Implementations of this interface can be used to expand functionality of
 * {@link oculusbot.opengl.Window Window}-class.
 * 
 * @author Robert Meschkat
 *
 */
public interface Callback {
	void keyPressed(long window, int key, int scancode, int action, int mods);
}
