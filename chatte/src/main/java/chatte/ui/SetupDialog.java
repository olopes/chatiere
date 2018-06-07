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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class SetupDialog extends JPanel {
	private static final long serialVersionUID = 1L;
	
	JDialog dialog;
	JTextField rhost;
	JSpinner rport;
	JSpinner lport;
	JTextField nick;
	
	boolean result = false;
	
	JButton ok;
	JButton cancel;
	
	public SetupDialog() {

		rhost = new JTextField();
		rport = new JSpinner(new SpinnerNumberModel(6666, 1, 65535, 1));
		lport = new JSpinner(new SpinnerNumberModel(6666, 1, 65535, 1));
		nick = new JTextField();
		ok = new JButton("OK");
		cancel = new JButton("Cancel");
		
		setupUI();
	}
	
	void setupUI() {
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.weightx=1.0;
		gc.gridx=0;
		gc.gridy=0;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridwidth=2;
		gc.insets = new Insets(2, 2, 2, 2);
		add(new JLabel("Nickname"), gc);
		gc.gridy=1;
		add(nick, gc);
		gc.gridy=2;
		gc.gridx=1;
		gc.gridwidth=1;
		gc.weightx=0.3;
		add(new JLabel("Local port"), gc);
		gc.gridy=3;
		gc.gridx=1;
		add(lport, gc);
		gc.gridy=4;
		gc.gridx=0;
		gc.weightx=0.7;
		add(new JLabel("Remote host"), gc);
		gc.gridy=4;
		gc.gridx=1;
		gc.weightx=0.3;
		add(new JLabel("Remote port"), gc);
		gc.gridy=5;
		gc.gridx=0;
		gc.weightx=0.7;
		add(rhost, gc);
		gc.gridy=5;
		gc.gridx=1;
		gc.weightx=0.3;
		add(rport, gc);
		
		JPanel jp = new JPanel();
		jp.setLayout(new FlowLayout(FlowLayout.CENTER));
		jp.add(ok);
		jp.add(cancel);
		
		gc.gridwidth=2;
		gc.gridy=6;
		gc.gridx=0;
		gc.weightx=1;
		gc.weighty=1;
		gc.fill = GridBagConstraints.VERTICAL;
		gc.anchor = GridBagConstraints.SOUTH;
		add(jp, gc);
		
		setPreferredSize(new Dimension(300, 200));
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okButtonClick();
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelButtonClick();
			}
		});
	}
	
	void okButtonClick() {
		result = true;
		dialog.setVisible(false);
	}

	void cancelButtonClick() {
		result = false;
		dialog.setVisible(false);
	}

	public boolean openDialog(JFrame owner) {
		if(dialog != null) {
			return false;
		}
		
		dialog = new JDialog(owner, "Preferences", true);
		dialog.getContentPane().add(this);
		dialog.setResizable(false);
		dialog.setSize(300, 200);
		
		result = false;
		dialog.setVisible(true);
		
		dialog.getContentPane().removeAll();
		dialog.dispose();
		dialog = null;
		
		return result;
	}
	
	public String getRhost() {
		return rhost.getText().trim();
	}

	public void setRhost(String rhost) {
		this.rhost.setText(rhost);
	}

	public int getRport() {
		return ((Number)rport.getValue()).intValue();
	}

	public void setRport(int rport) {
		this.rport.setValue(rport);
	}

	public int getLport() {
		return ((Number)lport.getValue()).intValue();
	}

	public void setLport(int lport) {
		this.lport.setValue(lport);
	}

	public String getNick() {
		return nick.getText().trim();
	}

	public void setNick(String nick) {
		this.nick.setText(nick);
	}

}
