package chatte.fx;

import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ChatteDialog extends Stage {
	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	private int selected;

	public ChatteDialog(Stage parent, String title, String message, String [] buttons) {
		setupUI(parent, title, message, buttons);
	}
	
	void setupUI(Stage parent, String title, String message, String [] buttons) {
		// TODO use stylesheet??
	    HBox buttonsPanel = new HBox();
	    buttonsPanel.setAlignment( Pos.CENTER_RIGHT );
	    buttonsPanel.setSpacing( 10 );
	    
	    for(int i = 0; i < buttons.length; i++) {
		    Button button = new Button( buttons[i] );
		    button.setOnAction(new ButtonAction(i));
		    button.getStyleClass().add("dialog-buttons");
		    buttonsPanel.getChildren().add(button);
	    }
	    
		Text messageText = new Text(message);
		// messageText.setWrappingWidth(250.0); // u wot m8?
		messageText.getStyleClass().add("dialog-message");
		
		VBox vb = new VBox();
	    vb.setPadding( new Insets(10,10,10,10) );
	    vb.setSpacing( 10 );
	    vb.getChildren().addAll(messageText, buttonsPanel);

	    Scene scene = new Scene(vb);
		scene.getStylesheets().add(getClass().getResource("ChatteFX.css").toExternalForm());

		setScene(scene);
		setTitle(title);
		initModality(Modality.WINDOW_MODAL);
		initOwner( parent );
	}
	
	public int showDialog() {
		selected = -1;
		centerOnScreen();
		showAndWait();
		return selected;
	}
	
	private class ButtonAction implements EventHandler<ActionEvent> {
		private final int buttonIndex;
		ButtonAction(final int buttonIndex) {
			this.buttonIndex = buttonIndex;
		}
		
        @Override public void handle( ActionEvent e ) {
        	log.fine("Button clicked: "+buttonIndex);
            close();
            selected = buttonIndex;
        }

	}
}
