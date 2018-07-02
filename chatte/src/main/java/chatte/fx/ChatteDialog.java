/*
 * MIT License
 * 
 * Copyright (c) 2018 OLopes
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */
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
		setupUI(parent, title, message, buttons, 0);
	}
	
	public ChatteDialog(Stage parent, String title, String message, String [] buttons, int cancelIndex) {
		setupUI(parent, title, message, buttons, cancelIndex);
	}
	
	void setupUI(Stage parent, String title, String message, String [] buttons, int cancelIndex) {
		// TODO use stylesheet??
	    HBox buttonsPanel = new HBox();
	    buttonsPanel.setAlignment( Pos.CENTER_RIGHT );
	    buttonsPanel.setSpacing( 10 );
	    
	    for(int i = 0; i < buttons.length; i++) {
		    Button button = new Button( buttons[i] );
		    button.setCancelButton(i==cancelIndex);
		    button.setOnAction(new ButtonAction(i));
		    button.getStyleClass().add("dialog-buttons");
		    button.setPrefWidth(100.0);
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
