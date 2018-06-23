package chatte.fx;

import javafx.application.Platform;
import javafx.stage.Stage;

public class JavascritpAdapter {
	Stage stage;
	ChatteController controller;

	public JavascritpAdapter(Stage stage, ChatteController controller) {
		this.stage=stage;
		this.controller=controller;
	}
	
	public void select(final String resource) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				controller.appendInputImage(resource.replaceFirst("chato:", ""));
				//stage.close();
			}
		});
	}

}
