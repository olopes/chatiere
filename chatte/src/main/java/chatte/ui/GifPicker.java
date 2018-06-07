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
package chatte.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URL;
import java.util.Formatter;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import chatte.resources.ResourceManager;

public class GifPicker extends JPanel {
	private static final long serialVersionUID = 1L;

	private ResourceManager resourceManager;
    private JDialog dialog = null;
	
	private HTMLEditorKit kit = new HTMLEditorKit();
	private HTMLDocument doc = new HTMLDocument();
	private JTextPane imageSelectionPane;
	private String selectedResource = null;
	private boolean success = false;
	
	public GifPicker(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
		setupUi();
	}
	
	
	void setupUi() {
		
		setLayout(new BorderLayout());
		
		imageSelectionPane = new JTextPane();
		
		
		JScrollPane scrollPane = new JScrollPane(imageSelectionPane);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);
		
		imageSelectionPane.setEditable(false);
		imageSelectionPane.setEditorKit(kit);
		imageSelectionPane.setDocument(doc);
		// imageSelectionPane.setLineWrap(true);
		imageSelectionPane.addHyperlinkListener(new HyperlinkListener() {
			
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
		        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		        	imageSelected(e.getURL());
		         }
			}
		});
		
		
		setPreferredSize(new Dimension(600, 400));
	}
	
	void imageSelected(URL url) {
		success = true;
		selectedResource = url.toString();
		dialog.setVisible(false);
	}
	
	public boolean showSelectionDialog(JFrame parent) {
		if(dialog != null) {
			return false;
		}
		
		// prepare contents
		try (Formatter formatter = new Formatter()) {
			String [] images = resourceManager.getResources();
			for(String image : images) {
				formatter.format("<a href=\"chato:%1$s\"><img border=\"0\" src=\"chato:%1$s\" /></a><br/>", image);
			}
			imageSelectionPane.setText(formatter.toString());
		}
		
		dialog = new JDialog(parent, "Emoticonas", true);
		// dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getContentPane().add(this);
		dialog.setSize(600, 400);
		
		selectedResource=null;
		success=false;
		dialog.setVisible(true);
		
		dialog.getContentPane().removeAll();
		dialog.dispose();
		dialog  = null;
		
		return success;
	}
	
	public String getSelectedResource() {
		return selectedResource;
	}

	
}
