package chatte;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MakeEmojiTable {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		File outFile = new File("target/EmojiList.html");
		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8))) {
			String html = "<!DOCTYPE html>\n" + 
					"<!-- \n" + 
					"MIT License\n" + 
					"\n" + 
					"Copyright (c) 2018 OLopes\n" + 
					"\n" + 
					"Permission is hereby granted, free of charge, to any person obtaining a copy\n" + 
					"of this software and associated documentation files (the \"Software\"), to deal\n" + 
					"in the Software without restriction, including without limitation the rights\n" + 
					"to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" + 
					"copies of the Software, and to permit persons to whom the Software is\n" + 
					"furnished to do so, subject to the following conditions:\n" + 
					"\n" + 
					"The above copyright notice and this permission notice shall be included in all\n" + 
					"copies or substantial portions of the Software.\n" + 
					"\n" + 
					"THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" + 
					"IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" + 
					"FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" + 
					"AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" + 
					"LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" + 
					"OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" + 
					"SOFTWARE.\n" + 
					" -->\n" + 
					"<html>\n" + 
					"<head>\n" + 
					"<meta charset=\"utf-8\" />\n" + 
					"<title>ChatteFX - Emoji</title>\n" + 
					"<link rel=\"stylesheet\" href=\"ChatView.css\" />\n" + 
					"<script type=\"text/javascript\" src=\"ChatView.js\"></script>\n" + 
					"</head>\n" + 
					"<body>";
			out.println(html);
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(MakeEmojiTable.class.getResourceAsStream("emoji-ordering.txt"), StandardCharsets.UTF_8))) {
				String line = null;
				Pattern p = Pattern.compile("[^;]+; [^#]+# ([^ ]+) (.+)");
				while((line=reader.readLine())!=null) {
					 // ignore comments and empty lines
					if(line.trim().startsWith("#") || line.trim().equals("")) continue;
					Matcher m = p.matcher(line);
					if(m.matches()) {
						String emojiCode = m.group(1);
						String emojiDescr = m.group(2);
						emojiDescr = emojiDescr.replaceFirst("flag:", "flag").replaceFirst("keycap:", "keycap");
						// ignore constructs "person tipping hand: light color", etc
						if(emojiDescr.contains(":")) continue;
						out.printf("<span class=\"emoji\" title=\"%s\">%s</span>\n", emojiDescr, emojiCode);
					} else {
						System.out.println("Bad line:");
						System.out.println(line);
					}
				}
			}
			
			out.println();
			out.println("</body>\n</html>");
		}
	}

}
