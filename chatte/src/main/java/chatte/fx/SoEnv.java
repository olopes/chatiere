package chatte.fx;

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
