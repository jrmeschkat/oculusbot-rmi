package oculusbot.basic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class PropertyLoader {
	private Properties props;
	private String propertyFile;
	private String defaultPropertyFile;
	private boolean usedDefault = false;

	public PropertyLoader(String propertyFile, String defaultPropertyFile) {
		this.propertyFile = propertyFile;
		this.defaultPropertyFile = defaultPropertyFile;
		props = loadProperties();
	}

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

	public int getPropertyAsInt(String key) {
		try {
			int result = Integer.parseInt(props.getProperty(key));
			return result;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public String getProperty(String key) {
		return props.getProperty(key);
	}

	public void updateValue(String key, String value) {
		OutputStream out = null;
		try {
			if (usedDefault) {
				System.err.println("Can't write to default file.");
				return;
			} else {
				out = new FileOutputStream(propertyFile);
			}

			if (getProperty(key) == null) {
				System.err.println("Key doesn't exist: " + key);
				out.close();
				return;
			}
			
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
