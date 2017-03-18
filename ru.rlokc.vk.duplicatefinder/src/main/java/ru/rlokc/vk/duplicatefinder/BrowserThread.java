package ru.rlokc.vk.duplicatefinder;

public class BrowserThread implements Runnable {
	
	private FXInterface fxWebView;

	public void run() {	
		fxWebView = new FXInterface();
		fxWebView.launchBrowserWindow(this);
	}
	
	public Browser getBrowser() {
		return fxWebView.getBrowser();
	}
	
	public void setInterface(FXInterface webView) {
		fxWebView = webView;
	}

}
