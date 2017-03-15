package ru.rlokc.vk.duplicatefinder;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class Browser extends Region {
	
	final private WebView browser = new WebView();
	final private WebEngine webEngine = browser.getEngine();
	
	public Browser() {
		getStyleClass().add("browser");
		getChildren().add(browser);
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
