package utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

	private static Properties properties;
	private static final String CONFIG_FILE_PATH = "src/test/resources/config/config.properties";

	static {
		try {
			FileInputStream fileInputStream = new FileInputStream(CONFIG_FILE_PATH);
			properties = new Properties();
			properties.load(fileInputStream);
			fileInputStream.close();
		} catch (IOException e) {
			throw new RuntimeException("Failed to load configuration file: " + CONFIG_FILE_PATH, e);
		}
	}
	//headless code
	 public String getBrowser() {
	        return properties.getProperty("browser", "chrome"); // Default to Chrome
	    }

	    public boolean isHeadless() {
	        return Boolean.parseBoolean(properties.getProperty("headless", "false")); // Default to false
	    }

	public static String getProperty(String key) {
		return properties.getProperty(key);
	}

	public String getApplicationUrl() {
		String url = properties.getProperty("baseUrl");
		if (url != null)
			return url;
		else
			throw new RuntimeException("url not specified in the Config.properties file.");
	}

	public long getImplicitlyWait() {
		String implicitlyWait = properties.getProperty("implicitlyWait");
		if (implicitlyWait != null)
			return Long.parseLong(implicitlyWait);
		else
			throw new RuntimeException("implicitlyWait not specified in the Configuration.properties file.");
	}
}