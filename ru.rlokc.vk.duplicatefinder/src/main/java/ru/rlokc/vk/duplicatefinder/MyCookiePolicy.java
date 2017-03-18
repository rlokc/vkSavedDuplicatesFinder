package ru.rlokc.vk.duplicatefinder;

import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;

public class MyCookiePolicy implements CookiePolicy {

	public boolean shouldAccept(URI uri, HttpCookie cookie) {
		//TODO: make it accept only cookies from vk
		return true;
	}

}
