package oculusbot.basic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertyLoader {
	private Properties props;
	private String propertyFile;
	private String defaultPropertyFile;
	
	public PropertyLoader(String propertyFile, String defaultPropertyFile) {
		this.propertyFile = propertyFile;
		this.defaultPropertyFile = defaultPropertyFile;
		props = loadProperties();
	}
	
	
	public Properties loadProperties(){
		Properties result = new Properties();
		try {
			result.load(new FileInputStream(propertyFile));
		} catch (FileNotFoundException e) {
			try {
				System.err.println(("Couldn't find \""+propertyFile+"\". Loading default property file."));
				result.load(getClass().getClassLoader().getResourceAsStream(defaultPropertyFile));
			} 
			catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public int getPropertyAsInt(String key){
		try{
			int result = Integer.parseInt(props.getProperty(key));
			return result;
		} catch(NumberFormatException e){
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public String getProperty(String key){
		return props.getProperty(key);
	}
}
