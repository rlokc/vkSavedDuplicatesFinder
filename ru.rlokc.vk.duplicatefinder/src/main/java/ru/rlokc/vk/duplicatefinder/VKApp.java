package ru.rlokc.vk.duplicatefinder;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;

import javafx.application.Platform;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.SynchronousQueue;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class VKApp {
	private final static String PROPERTIES_FILE = "config.properties";
	private static Server server;
	
	private static BrowserThread browserThreadobj;
	private static ServerThread serverThreadobj;
	
	public final static SynchronousQueue<OAuthToken> tokenQueue = new SynchronousQueue<OAuthToken>();
	
	public static void main(String[] args) throws Exception {
		
		browserThreadobj = new BrowserThread();
		Thread browserThread = new Thread(browserThreadobj);
		browserThread.start();
		
		synchronized(browserThreadobj) {
			browserThreadobj.wait();
		}
		
		Properties properties = readProperties();
		serverThreadobj = new ServerThread(properties);
		Thread serverThread = new Thread(serverThreadobj);
		serverThread.start();
		
		synchronized(serverThreadobj) {
			serverThreadobj.wait();
		}
		
		goToLoginPage();
		
		//Wait until we recieve the code from the authentication flow
		OAuthToken token = tokenQueue.take();
		serverThreadobj.getRequestHandler().setToken(token);
		goGetToken();
	}
	
	private static void initServer(Properties properties) throws Exception {
		Integer port = Integer.valueOf(properties.getProperty("server.port"));
		String host = properties.getProperty("server.host");
		
		Integer clientId = Integer.valueOf(properties.getProperty("client.id"));
		String clientSecret = properties.getProperty("client.secret");
		
		HandlerCollection handlers = new HandlerCollection();
		
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setWelcomeFiles(new String[] {"index.html"});
		resourceHandler.setResourceBase(VKApp.class.getResource("/static").getPath());
		

		VkApiClient vk = new VkApiClient(new HttpTransportClient());
		handlers.setHandlers(new Handler[] {resourceHandler, new RequestHandler(vk, clientId, clientSecret, host)});
		
		server = new Server(port);
		server.setHandler(handlers);
		
		//Initializing logging
		NCSARequestLog requestLog = new NCSARequestLog();
		requestLog.setFilename("/Users/rlokc/Programming/vkSavedDuplicateFinder/logs/yyyy_mm_dd.request.log");
		requestLog.setFilenameDateFormat("yyyy_MM_dd");
		requestLog.setRetainDays(15);
		requestLog.setAppend(true);
		requestLog.setExtended(true);
		requestLog.setLogCookies(true);
		requestLog.setLogTimeZone("GMT");
		RequestLogHandler requestLogHandler = new RequestLogHandler();
		requestLogHandler.setRequestLog(requestLog);
		handlers.addHandler(requestLogHandler);
		
		server.start();
		server.join();		
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
	
	private static void goToLoginPage() {
		final RequestHandler handler = serverThreadobj.getRequestHandler();
		final Browser browser = browserThreadobj.getBrowser();
		Platform.runLater(new Runnable() {
			public void run() {
				browser.loadURL(handler.getOAuthUrl());
			}
		});
		return;
	}
	
	private static void goGetToken() {
		final RequestHandler handler = serverThreadobj.getRequestHandler();
		final Browser browser = browserThreadobj.getBrowser();
		Platform.runLater(new Runnable() {
			public void run() {
				browser.loadURL(handler.getTokenUrl());
			}
		});
		return;
	}
}


