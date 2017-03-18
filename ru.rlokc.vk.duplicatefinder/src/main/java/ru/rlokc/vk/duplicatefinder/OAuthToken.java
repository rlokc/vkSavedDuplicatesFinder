package ru.rlokc.vk.duplicatefinder;

public class OAuthToken {
	public String code;
	public String access_token;
	public int expires_in;
	public int user_id;
	
	@Override
	public String toString() {
		return "code: " + code + " access_token: " + access_token + " expires_in: " + " user_id: " + user_id;
	}
}
