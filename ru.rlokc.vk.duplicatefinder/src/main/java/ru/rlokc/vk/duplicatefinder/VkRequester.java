package ru.rlokc.vk.duplicatefinder;

import java.util.List;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.users.UserXtrCounters;

public class VkRequester {

	private final String clientSecret;
	private final int clientId;
	private final String host;
	private final VkApiClient vk;
	private OAuthToken token;
	
	public VkRequester(VkApiClient vk, int clientId, String clientSecret, String host) {
		this.vk = vk;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.host = host;
	}
	
	public void printInfo() {
		try {
			UserActor actor = new UserActor(token.user_id, token.access_token);
			List<UserXtrCounters> getUsersResponse = vk.users().get(actor).userIds(Integer.toString(token.user_id)).execute();
			UserXtrCounters user = getUsersResponse.get(0);
			System.out.println(getInfoPage(user));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//TODO: Move all the authorization routine methods to a separate class
	public String getRedirectUri() {
		return host;
	}
	
	public String getCodeUrl() {
		return "https://oauth.vk.com/authorize?client_id=" + clientId + "&display=page&redirect_uri=" + getRedirectUri() + "&scope=groups&response_type=code";
	}
	
	public String getTokenUrl() {
		return "https://oauth.vk.com/access_token?client_id=" + clientId + "&client_secret=" + clientSecret + "&redirect_uri=" + getRedirectUri() + "&code=" + token.code;
	}
	
	public String getTokenUrl(OAuthToken token) {
		this.token = token;
		return getTokenUrl();
	}
 	
	public String getInfoPage(UserXtrCounters user) {
		return "Hello <a href='https://vk.com/id" + user.getId() + "'>" + user.getFirstName() + "</a>";
	}
	
	public void setToken(OAuthToken token) {
		this.token = token;
	}
	
}
