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

import javafx.application.Platform;
import javafx.stage.Window;

public class JavascritpAdapter {
	Window stage;
	ChatteMainController controller;

	public JavascritpAdapter(Window stage, ChatteMainController controller) {
		this.stage=stage;
		this.controller=controller;
	}
	
	public void selectResource(final String resource) {
		System.out.println("Escolher a resource");
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				controller.appendInputImage(resource.replaceFirst("chato:", "")); //$NON-NLS-1$ //$NON-NLS-2$
				//stage.close();
			}
		});
	}

	public void selectEmoji(final String emoji) {
		System.out.println("Escolher o emoji");
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				controller.appendEmoji(emoji);
				//stage.close();
			}
		});
	}

}
