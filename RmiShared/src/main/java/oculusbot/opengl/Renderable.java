package oculusbot.opengl;

/**
 * Interface which needs to be implemented by classes if they want to render
 * something to the Rift or a window.
 * 
 * @author Robert Meschkat
 *
 */
public interface Renderable {
	void init();

	void render();

	void destroy();
}
