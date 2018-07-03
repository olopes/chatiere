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
package chatte;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class Chatte {
	
	static boolean hasJfxrt(URLClassLoader cl) {
		for(URL entry : cl.getURLs()) {
			File f = new File(entry.getFile());
			System.out.println("   => "+f.getName());
			if(f.exists() && f.isFile() && "jfxrt.jar".equals(f.getName())) {
				System.out.println("Found it!! Nothing to be done.");
				return true;
			}
		}
		return false;
	}
	
	static class ClasspathFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			String lowerName = pathname.getName().toLowerCase();
			return pathname.isDirectory() || lowerName.endsWith(".jar") || lowerName.endsWith(".zip");
		}
	}

	public static void main(String[] args) throws Exception {
		/*
		 * This is why we can't have nice things.
		 */
		ClassLoader cl = Chatte.class.getClassLoader();
		if(cl instanceof URLClassLoader) {
			System.out.println("Launcher is URLClassLoader!");
			URL [] oldClassPath = ((URLClassLoader)cl).getURLs();
			System.out.println(Arrays.asList(oldClassPath));
			Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			addURL.setAccessible(true);

			// append "ext" folder contents
			File ext = new File("ext");
			if(ext.exists() && ext.isDirectory()) {
				for(File dependency : ext.listFiles(new ClasspathFileFilter())) {
					addURL.invoke(cl, dependency.toURI().toURL());
				}
			}
			
			// check if jfxrt.jar is already in the classpath
			boolean jfxrtFound = hasJfxrt((URLClassLoader)cl);
			if(!jfxrtFound) {
				// java8 will add jfxrt.jar to ext classpath
				System.out.println("Checking ext classpath...");
				jfxrtFound = hasJfxrt((URLClassLoader)cl.getParent());
			}
			
			if(!jfxrtFound) {
				System.out.println("jfxrt.jar not in classpath. hacking... :-(");
				// find jfxrt location
				String javaHomePath = System.getProperty("java.home");
				File javaHome = new File(javaHomePath);
				File jfxrt = new File(new File(javaHome, "lib"), "jfxrt.jar");
				System.out.println("jfxrt.jar location: "+jfxrt);

				addURL.invoke(cl, jfxrt.toURI().toURL());
				URL [] newClassPath = ((URLClassLoader)cl).getURLs();
				System.out.println("Fixed classpath:");
				System.out.println(Arrays.asList(newClassPath));

				cl.loadClass("javafx.application.Application");
			}
		}
		
		System.out.println("Loading ChatteFX class");
		Class<?> cc = cl.loadClass("chatte.fx.ChatteFX");
		Method main = cc.getDeclaredMethod("main", String[].class);
		System.out.println("Starting ChatteFX");
		main.invoke(null, (Object)args);

	}

}
