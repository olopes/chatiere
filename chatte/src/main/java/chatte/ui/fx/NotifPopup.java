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
package chatte.ui.fx;

import javafx.animation.PauseTransition;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;

public class NotifPopup extends Popup implements  EventHandler<ActionEvent> {

	PauseTransition pause = new PauseTransition(Duration.seconds(10));
	Label titleLabel = new Label();
	Label messageLabel = new Label();
	double locx, locy;
	
	public NotifPopup(Parent root) {
		setupUi((Region)root);
	}

	private void setupUi(Region root) {
		final double w = root.getWidth(), h = root.getHeight();

	    setAutoFix(true);
	    setAutoHide(true);
	    setHideOnEscape(true);
		setWidth(w);
		setHeight(h);
	    
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		//set Stage boundaries to the lower right corner of the visible bounds of the main screen
		locx = (primaryScreenBounds.getMinX() + primaryScreenBounds.getWidth() - w - 10);
		locy = (primaryScreenBounds.getMinY() + primaryScreenBounds.getHeight() - h - 10);
		
		pause.setOnFinished(this);
		
		getContent().add(root);
	}
	
    @Override
    public void handle(ActionEvent event) {
        hide();
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
