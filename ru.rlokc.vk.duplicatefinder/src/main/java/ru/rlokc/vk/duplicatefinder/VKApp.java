package ru.rlokc.vk.duplicatefinder;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;

import javafx.application.Platform;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.Properties;
import java.util.concurrent.SynchronousQueue;


public class VKApp {
	
	private final static String PROPERTIES_FILE = "config.properties";	
	private static BrowserThread browserThreadobj;
	public static OAuthToken token;
	//Basically a blocking queue for mutexing the token, because I'm too lazy to make an actual mutex
	public final static SynchronousQueue<OAuthToken> tokenQueue = new SynchronousQueue<OAuthToken>();
	
	private static VkRequester vkRequester;
	
	public static void main(String[] args) throws Exception {
		
		//FIXME: Cookies init, doesn't work and makes the login invalid for some reason, 
//		MyCookieStore cookie_store = new MyCookieStore();
//		CookieManager cookie_manager = new CookieManager(cookie_store, new MyCookiePolicy());
//		CookieHandler.setDefault(cookie_manager);
		
		browserThreadobj = new BrowserThread();
		Thread browserThread = new Thread(browserThreadobj);
		browserThread.start();
		
		synchronized(browserThreadobj) {
			browserThreadobj.wait();
		}
		
		Properties properties = readProperties();
		vkRequester = initRequester(properties);
		
		authorize();

		vkRequester.printInfo();
	}

	private static Properties readProperties() throws FileNotFoundException {
		InputStream inputStream = VKApp.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
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
	
	private static VkRequester initRequester(Properties properties) {
		VkApiClient vk = new VkApiClient(new HttpTransportClient());
		Integer port = Integer.valueOf(properties.getProperty("server.port"));
		String host = properties.getProperty("server.host");
		Integer clientId = Integer.valueOf(properties.getProperty("client.id"));
		String clientSecret = properties.getProperty("client.secret");
		vkRequester = new VkRequester(vk, clientId, clientSecret, host);
		return vkRequester;
	}
	
	private static void authorize() {
		goToLoginPage();
		//Wait until we receive the code from the authentication flow
		try {
			token = tokenQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Something has gone horribly wrong, the flow is broken!");
		}
		goGetToken();
		try {
			token = tokenQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Something has gone horribly wrong, the flow is broken!");
		}
		vkRequester.setToken(token);
	}
	
	private static void goToLoginPage() {
		final Browser browser = browserThreadobj.getBrowser();
		Platform.runLater(new Runnable() {
			public void run() {
				browser.loadURL(vkRequester.getCodeUrl());
			}
		});
		return;
	}
	
	private static void goGetToken() {
		final Browser browser = browserThreadobj.getBrowser();
		Platform.runLater(new Runnable() {
			public void run() {
				browser.loadURL(vkRequester.getTokenUrl(token));
			}
		});
		return;
	}
}


