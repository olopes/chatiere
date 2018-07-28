package chatte.sys;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;

public class SystemUserService {

	private Object system;

	public SystemUserService() {
		try {
			system = Class.forName("com.sun.security.auth.module.NTSystem").newInstance();
		} catch(Exception ntEx) {
			try {
				system = Class.forName("com.sun.security.auth.module.UnixSystem").newInstance();
			} catch(Exception uxEx) {
				try {
					system = Class.forName("com.sun.security.auth.module.SolarisSystem").newInstance();
				} catch(Exception slEx) {
					// throw new RuntimeException("System not found");
					system = null;
				}
			}
		}
	}
	
	public String getUsername() {
		if(system != null) {
			try {
				return (String) system.getClass().getMethod("getName").invoke(system);
			} catch (Exception e) {
				try {
					return (String) system.getClass().getMethod("getUsername").invoke(system);
				} catch (Exception e1) {

				}
			}
		}
		return System.getProperty("user.name");
	}

	public String getUserId() {
		if(system != null) {
			try {
				return (String) system.getClass().getMethod("getUserSID").invoke(system);
			} catch (Exception e) {
				try {
					return String.valueOf(system.getClass().getMethod("getUid").invoke(system));
				} catch (Exception e1) {

				}
			}
		}
		return null;
	}

	public String getGroupId() {
		if(system != null) {
			try {
				return (String) system.getClass().getMethod("getPrimaryGroupID").invoke(system);
			} catch (Exception e) {
				try {
					return String.valueOf(system.getClass().getMethod("getGid").invoke(system));
				} catch (Exception e1) {

				}
			}
		}
		return null;
	}

	public String [] getUserGroups() {
		if(system != null) {
			try {
				return (String[]) system.getClass().getMethod("getGroupIDs").invoke(system);
			} catch (Exception e) {
				try {
					long [] groups = (long[]) system.getClass().getMethod("getGroups").invoke(system);
					String [] result = new String[groups.length];
					for(int i = 0; i<groups.length; i++)
						result[i]=String.valueOf(groups[i]);
					return result;
				} catch (Exception e1) {

				}
			}
		}
		return null;
	}
	
	public UserPrincipal getUserPrincipal() {
		try {
			return FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(getUsername());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) {
		SystemUserService sus = new SystemUserService();
		System.out.println("User: "+sus.getUsername());
		System.out.println("UID: "+sus.getUserId());
		System.out.println("GID: "+sus.getGroupId());
		System.out.println("groups: "+Arrays.toString(sus.getUserGroups()));
		System.out.println("Principal: "+sus.getUserPrincipal());
	}

}
