package oculusbot.basic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Properties;

/**
 * Helper class for simple access to a property file
 * 
 * @author Robert Meschkat
 *
 */
public class PropertyLoader {
	private Properties props;
	private String propertyFile;
	private String defaultPropertyFile;
	private boolean usedDefault = false;

	/**
	 * Creates a object to simplify property access.
	 * 
	 * @param propertyFile
	 *            Path and filename of the property file.
	 * @param defaultPropertyFile
	 *            Path and filename to a default file, which is used when normal
	 *            file isn't found.
	 */
	public PropertyLoader(String propertyFile, String defaultPropertyFile) {
		this.propertyFile = propertyFile;
		this.defaultPropertyFile = defaultPropertyFile;
		props = loadProperties();
	}

	/**
	 * Tries to load normal file first. If unsuccessful tries to load default
	 * file.
	 * 
	 * @return The properties object with the loaded information or an empty
	 *         object if neither file was found.
	 */
	public Properties loadProperties() {
		Properties result = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(propertyFile);
			result.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			try {
				System.err.println(("Couldn't find \"" + propertyFile + "\". Loading default property file."));
				in = getClass().getClassLoader().getResourceAsStream(defaultPropertyFile);
				result.load(in);
				in.close();
				usedDefault = true;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Looks up property and converts the value to an integer before returning
	 * it.
	 * 
	 * @param key
	 *            Key to find the correct property.
	 * @return Property value as integer.
	 */
	public int getPropertyAsInt(String key) {
		try {
			int result = Integer.parseInt(props.getProperty(key));
			return result;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * Returns needed property value.
	 * 
	 * @param key
	 *            Key to find the correct property.
	 * @return Property value.
	 */
	public String getProperty(String key) {
		return props.getProperty(key);
	}

	/**
	 * Changes the value of one of the loaded properties and writes the changes
	 * in the file. The default file can't be changed with this method.
	 * Non-existing properties will be ignored.
	 * 
	 * @param key
	 *            Key that identifies the property
	 * @param value
	 *            The new value for the property
	 */
	public void updateValue(String key, String value) {
		OutputStream out = null;
		try {
			//check if default file wasn't used
			if (usedDefault) {
				System.err.println("Can't write to default file.");
				return;
			} else {
				out = new FileOutputStream(propertyFile);
			}

			//check if property exists
			if (getProperty(key) == null) {
				System.err.println("Key doesn't exist: " + key);
				out.close();
				return;
			}

			//change the value and write it to the file
			props.setProperty(key, value);
			props.store(out, "");
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
