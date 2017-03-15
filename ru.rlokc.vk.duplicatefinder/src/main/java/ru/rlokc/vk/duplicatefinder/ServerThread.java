package ru.rlokc.vk.duplicatefinder;

import java.util.Properties;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;

public class ServerThread implements Runnable {
	
	private Server server;
	private Properties properties;
	private RequestHandler requestHandler;
	
	public ServerThread(Properties properties) {
		this.properties = properties;
	}

	public void run() {
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
		requestHandler = new RequestHandler(vk, clientId, clientSecret, host);

		handlers.setHandlers(new Handler[] {resourceHandler, requestHandler});
		
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
		
		synchronized(this) {
			this.notify();
		}
		
		try {
			server.start();
			server.join();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public RequestHandler getRequestHandler() {
		return requestHandler;
	}

}
