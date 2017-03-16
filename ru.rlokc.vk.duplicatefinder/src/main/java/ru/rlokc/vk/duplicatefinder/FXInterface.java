package ru.rlokc.vk.duplicatefinder;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FXInterface extends Application {

	private Scene scene;
	private Browser browser;
	
	private static BrowserThread thread;
	
	
	@Override
	public void start(Stage stage) {
		stage.setTitle("Henlo");
		browser = new Browser(thread);
		thread.setInterface(this);
		// After we've initialized the browser, get ready to recieve stuff
		synchronized(thread) {
			thread.notify();
		}
		scene = new Scene(browser, 750, 500, Color.web("#B4D455"));
		stage.setScene(scene);
		stage.show();
	}
	
	public void setBrowser(Browser browser) {
		this.browser = browser;
	}
	
	public Browser getBrowser() {
		return browser;
	}
	
	public void launchBrowserWindow(BrowserThread thread) {
		this.thread = thread;
		launch();
	}
	
}
