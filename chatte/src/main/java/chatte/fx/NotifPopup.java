package chatte.fx;

import javafx.animation.PauseTransition;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;

public class NotifPopup extends Popup {

	PauseTransition pause = new PauseTransition(Duration.seconds(10));
	Label titleLabel = new Label();
	Label messageLabel = new Label();
	double locx, locy;
	
	public NotifPopup() {
		setupUi();
	}

	private void setupUi() {
		final int w = 300, h = 70;
		titleLabel.getStyleClass().add("title-label");
		messageLabel.getStyleClass().add("message-label");
		VBox contents = new VBox(5.0);
        contents.getStylesheets().add(getClass().getResource("ChatteFX.css").toExternalForm());
		contents.getStyleClass().add("popup-notification");
		
		
		contents.getChildren().addAll(titleLabel, messageLabel);
		
	    setAutoFix(true);
	    setAutoHide(true);
	    setHideOnEscape(true);
	    contents.setMaxSize(w, h);
	    contents.setPrefSize(w, h);
	    contents.setMinSize(w, h);
		setWidth(w);
		setHeight(h);
	    
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		//set Stage boundaries to the lower right corner of the visible bounds of the main screen
		locx = (primaryScreenBounds.getMinX() + primaryScreenBounds.getWidth() - w - 10);
		locy = (primaryScreenBounds.getMinY() + primaryScreenBounds.getHeight() - h - 10);
		
		contents.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			public void handle(MouseEvent p0) {
				hide();
			};
		});
		pause.setOnFinished(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		        hide();
		    }
		});
		
		getContent().add(contents);
	}
	
	public void setTitle(String text) {
		titleLabel.setText(text);
	}
	
	public String getTitle() {
		return titleLabel.getText();
	}
	
	public StringProperty titleProperty() {
		return titleLabel.textProperty();
	}
	
	public void setMessage(String text) {
		messageLabel.setText(text);
	}
	
	public String getMessage() {
		return messageLabel.getText();
	}
	
	public StringProperty messageProperty() {
		return messageLabel.textProperty();
	}
	
	public void show(Window owner, String title, String message) {
		setTitle(title);
		setMessage(message);
		if(!owner.isFocused() && !isShowing()) {
			show(owner, locx, locy);
			pause.playFromStart();
		}
	}
}
