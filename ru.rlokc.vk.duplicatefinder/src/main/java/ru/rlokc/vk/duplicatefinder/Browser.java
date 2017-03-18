package ru.rlokc.vk.duplicatefinder;

import com.google.gson.Gson;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class Browser extends Region {
	
	final private WebView browser = new WebView();
	final private WebEngine webEngine = browser.getEngine();
	
	static private BrowserThread thread;
	
	public Browser(BrowserThread thread) {
		getStyleClass().add("browser");
		getChildren().add(browser);
		
		this.thread = thread;
		
		webEngine.getLoadWorker().stateProperty().addListener(
			new ChangeListener<State>() {
				public void changed(ObservableValue ov, State oldState, State newState) {
					if (newState == State.SUCCEEDED) {
						
						AuthorizationStep authStep = null;
						String url = webEngine.getLocation();
						System.out.println(url);
						if (url.indexOf("blank.html#code") != -1) {
							authStep = AuthorizationStep.CODE;
						} else if (url.indexOf("access_token") != -1) {
							authStep = AuthorizationStep.TOKEN;
						}

						// If we're on the first step of Authorization code flow, get the code from the address
						if (authStep == AuthorizationStep.CODE) {
							int codeIndex = url.indexOf("code=");
							if (codeIndex != -1) {
								String code = url.substring(codeIndex + "code=".length());
								System.out.println("CODE: " + code);
								OAuthToken token = new OAuthToken();
								token.code = code;
								try {
									VKApp.tokenQueue.put(token);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						// If we're on the second step (getting the token), fetch the <pre> element from the html
						} else if (authStep == AuthorizationStep.TOKEN) {
							try {
								OAuthToken token = VKApp.tokenQueue.take();
								System.out.println(token);
								String oldCode = token.code;
								JSObject pre = (JSObject) webEngine.executeScript("document.getElementsByTagName('pre')[0]");
								System.out.println(pre.getMember("innerHTML"));
								token = new Gson().fromJson(pre.getMember("innerHTML").toString(), OAuthToken.class);
								token.code = oldCode;
								VKApp.tokenQueue.put(token);
							} catch (Exception e) {e.printStackTrace();}
						}
					}
				}
			});
	}
	
	public void loadURL(String URL) {
		webEngine.load(URL);
	}
	
	
	private Node createSpacer() {
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		return spacer;
	}
	
	@Override
	protected void layoutChildren() {
		double w = getWidth();
		double h = getHeight();
		layoutInArea(browser,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
	}
	
	@Override
	protected double computePrefWidth(double height) {
		return 750;
	}
	
	@Override
	protected double computePrefHeight(double width) {
		return 500;
	}

}


