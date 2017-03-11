package ru.rlokc.vk.duplicatefinder;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.users.UserXtrCounters;

public class RequestHandler extends AbstractHandler {

	private final String clientSecret;
	private final int clientId;
	private final String host;
	private final VkApiClient vk;
	
	public RequestHandler(VkApiClient vk, int clientId, String clientSecret, String host) {
		this.vk = vk;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.host = host;
	}
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		System.out.println("target");
		if (target.equals("/info")) {
			try {
				UserActor actor = new UserActor(Integer.parseInt(baseRequest.getParameter("user")), baseRequest.getParameter("token"));
				List<UserXtrCounters> getUsersResponse = vk.users().get(actor).userIds(baseRequest.getParameter("user")).execute();
				UserXtrCounters user = getUsersResponse.get(0);
				
				response.setContentType("text/html;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println(getInfoPage(user));
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				response.getWriter().println("error");
				response.setContentType("text/html;charset=utf-8");
				e.printStackTrace();
			}
			
			baseRequest.setHandled(true);
			return;
		} else if (target.equals("/callback")) {
			try {
				UserAuthResponse authResponse = vk.oauth().userAuthorizationCodeFlow(clientId, clientSecret, getRedirectUri(), baseRequest.getParameter("code")).execute();
				response.sendRedirect("/info?token=" + authResponse.getAccessToken() + "&user=" + authResponse.getUserId());
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				response.getWriter().println("error");
				response.setContentType("text/html;charset=utf-8");
				e.printStackTrace();
			}
			
			baseRequest.setHandled(true);
			return;
		} else if (target.equals("/login")) {
			response.sendRedirect(getOAuthUrl());
			baseRequest.setHandled(true);
			return;
		}
	}
	
	private String getRedirectUri() {
		return "https://oauth.vk.com/blank.html";
	}
	
	private String getOAuthUrl() {
		return "https://oauth.vk.com/authorize?client_id=" + clientId + "&display=page&redirect_uri=" + getRedirectUri() + "&scope=groups&response_type=code";
	}
 	
	private String getInfoPage(UserXtrCounters user) {
		return "Hello <a href='https://vk.com/id'>" + user.getId() + "'>" + user.getFirstName() + "</a>";
	}
	
}
