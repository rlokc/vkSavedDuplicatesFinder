package ru.rlokc.vk.duplicatefinder;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Application {
	private final static String PROPERTIES_FILE = "config.properties";
	
	public static void main(String[] args) throws Exception {
		Properties properties = readProperties();
		
		HttpTransportClient client = new HttpTransportClient();
		VkApiClient apiClient = new VkApiClient(client);
		
		
	}
	
	private static Properties readProperties() throws FileNotFoundException {
		InputStream inputStream = Application.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
		if (inputStream == null)
			throw new FileNotFoundException("Property file " + PROPERTIES_FILE + " not found in classpath");
		
		try {
			Properties properties = new Properties();
			properties.load(inputStream);
			return properties;
		} catch (IOException e) {
			throw new RuntimeException("Incorrect properties file");
		}
	}
}


