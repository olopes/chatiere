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

public enum SoEnv {
	generic(),
	windows("cmd.exe", "/c", "START", "%WINDIR%\\system32\\SnippingTool.exe"),
	kde("spectacle"),
	gnome("gnome-screenshot", "-i"),
	;
	final String [] cmd;
	SoEnv(String ... cmd) {
		this.cmd = cmd;
	}
	
	public String [] getCmd() {
		return cmd;
	}
	
	public static SoEnv getEnv() {
		if(System.getProperty("os.name").startsWith("Windows")) {
			return windows;
		} else if ("KDE".equals(System.getenv("XDG_CURRENT_DESKTOP"))){
			return kde;
		} else if ("GNOME".equals(System.getenv("XDG_CURRENT_DESKTOP"))){
			return gnome;
		} else {
			return generic;
		}
	}
}
