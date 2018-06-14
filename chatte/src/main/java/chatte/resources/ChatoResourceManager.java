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
package chatte.resources;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class ChatoResourceManager implements ResourceManager {

	File resourceFolder;
	private Logger log = getLogger();
	Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}

	public ChatoResourceManager() {
		resourceFolder = new File("toybox");
		resourceFolder.mkdirs();
	}

	public File getResourceFile(String resourceCode) {
		if(resourceCode == null) return null;
		final String filePrefix = resourceCode.replaceFirst("chato:", "")+".";
		File [] flist = resourceFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(filePrefix);
			}
		});

		return flist.length == 0?null:flist[0];
	}

	public boolean resourceExist(String resourceCode) {
		return getResourceFile(resourceCode) != null;
	}

	public URL getResourceURL(String resourceCode) {
		File f = getResourceFile(resourceCode);
		if(f == null) return null;
		try {
			return f.toURI().toURL();
		} catch (MalformedURLException e) {
		}
		return null;
	}

	String [] filesToStrings(File [] files) {
		if(null == files) return new String[0];
		String [] resources = new String[files.length];
		for(int i = 0; i < resources.length; i++) {
			resources[i] = files[i].getName();
			if(files[i].isFile())
				resources[i] = resources[i].replaceFirst("\\.\\w+$", "");
		}
		return resources;
	}

	public String[] getResources() {
		return filesToStrings(resourceFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				// accept only files of type gif, jpg and png
				String name = pathname.getName().toLowerCase();
				return pathname.isFile() && 
						name.length() == 44  /* SHA1+extension */ && (
						name.endsWith(".gif") ||
						name.endsWith(".png") ||
						name.endsWith(".jpg") );
			}
		}));
	}

	public String addResource(File toAdd) {
		try {
			// compute sha1sum
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			byte[] b = new byte[8192];
			int r = 0;
			try (FileInputStream in = new FileInputStream(toAdd)) {
				while((r = in.read(b))>=0)
					crypt.update(b, 0, r);
			}
			String sha1 = byteToHex(crypt.digest());

			// filename = sha1+extension
			String name = toAdd.getName().toLowerCase();
			name = name.substring(name.lastIndexOf('.'));
			File dest = new File(resourceFolder, sha1+name);

			Files.copy(toAdd.toPath(), dest.toPath());
			return sha1;
		} catch(Exception e) {
			log.log(Level.SEVERE, "Error adding new resource", e);
		}
		return null;
	}


	String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

	public String addResource(Image img) {
		String resource = null;

		if(img instanceof RenderedImage) {
			File f = null;
			try {
				f = File.createTempFile("chato-", ".jpg");
				ImageIO.write((RenderedImage)img, "jpg", f);
				resource = addResource(f);
			} catch (IOException e) {
				log.log(Level.SEVERE, "Error saving new image", e);
			} finally {
				if(f != null) f.delete();
			}
		}
		return resource;
	}

	public String addResource(byte [] data, String type) {
		File f = null;
		String resource = null;
		try {
			f = File.createTempFile("recv-", "."+type);
			try (FileOutputStream ff = new FileOutputStream(f)) {
				ff.write(data);
			}

			resource = addResource(f);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error saving received resource", e);
		} finally {
			if(f!=null) f.delete();
		}

		return resource;

	}

	public byte [] getResourceData(String resourceCode) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		URL resource = getResourceURL(resourceCode);
		if(resource == null) return null;
		try (InputStream in = resource.openStream()) {
			byte [] b = new byte[8192];
			int r;
			while((r = in.read(b)) >= 0)
				out.write(b, 0, r);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error reading resource data", e);
		}
		return out.toByteArray();
	}
	
}
