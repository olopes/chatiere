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

import java.util.concurrent.Callable;

import chatte.msg.Friend;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class FriendListCell extends ListCell<Friend> {
	Node connected, disconnected;
	
	public FriendListCell() {
		setTooltip(new Tooltip());
		connected = newRectangle(Color.GREEN);
		disconnected = newRectangle(Color.gray(0.9));
	}
	
	Rectangle newRectangle(Color fill) {
		//Drawing a Rectangle 
		Rectangle rectangle = new Rectangle();  
		rectangle.setFill(fill);
		rectangle.setStroke(Color.BLACK);

		//Setting the properties of the rectangle 
		rectangle.setWidth(10.0); 
		rectangle.setHeight(10.0); 

		//Setting the height and width of the arc 
		rectangle.setArcWidth(3.0); 
		rectangle.setArcHeight(3.0); 
		return rectangle;
	}
	
	protected void updateItem(Friend item, boolean empty) {
		super.updateItem(item, empty);

		// remove bindings
		textProperty().unbind();
		textFillProperty().unbind();
		graphicProperty().unbind();
		getTooltip().textProperty().unbind();
		
		if (empty || item == null) {
			setText(null);
			setGraphic(null);
			getTooltip().setText(null);
		} else {
			textProperty().bind(item.nickProperty());
			getTooltip().textProperty().bind(Bindings.concat(item.nickProperty(), "@", item.getHost()));
			final ReadOnlyBooleanProperty connectedProp = item.connectedProperty();
			graphicProperty().bind(Bindings.createObjectBinding(new Callable<Node>() {
				@Override
				public Node call() throws Exception {
					return connectedProp.get()?connected:disconnected;
				}
			}, connectedProp));
			final ReadOnlyStringProperty colorProp = item.colorProperty();
			textFillProperty().bind(Bindings.createObjectBinding(new Callable<Color>() {
				@Override
				public Color call() throws Exception {
					return connectedProp.get()?Color.web(colorProp.get()):Color.GRAY;
				}
				
			}, colorProp, connectedProp));

		}
	}
}
