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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import chatte.msg.Friend;

public class FriendRenderer extends JLabel implements ListCellRenderer<Friend> {
	private static final long serialVersionUID = 1L;

	Color selectionBg, selectionFg, regularBg;
	Color []usrColors;
	
	public FriendRenderer() {
        setOpaque(true);
        selectionBg = new Color(0x66, 0x82, 0xff);
        selectionFg = Color.WHITE;
        regularBg = Color.WHITE;
        usrColors = new Color[UserColors.colors.length];
		for(int i = 0; i < UserColors.colors.length; i++) {
			usrColors[i] = new Color(Integer.parseInt(UserColors.colors[i], 16));
		}
    }

	@Override
	public Component getListCellRendererComponent(JList<? extends Friend> list, Friend value, int index,
			boolean isSelected, boolean cellHasFocus) {

        setText(value.getNick());
        setToolTipText(value.getNick()+"@"+value.getHost()+":"+value.getPort());

        if(isSelected) {
        	setForeground(selectionFg);
        	setBackground(selectionBg);
        } else {
        	setForeground(usrColors[index%usrColors.length]);
        	setBackground(regularBg);
        }
        return this;
	}
}
