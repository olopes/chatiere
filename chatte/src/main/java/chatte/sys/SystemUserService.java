package chatte.sys;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;

public abstract class SystemUserService {

	public static SystemUserService getDefault() {
		try {
			return new NTUserService();
		} catch (SystemUserException e) {
			try {
				return new UnixUserService();
			} catch (SystemUserException e1) {
				return new SystemUserService() {
				};
			}
		}
	}

	public SystemUserService() {
	}

	public String getUsername() {
		return System.getProperty("user.name");
	}

	public String getUserId() {
		return null;
	}

	public String getGroupId() {
		return null;
	}

	public String[] getUserGroups() {
		return null;
	}

	public UserPrincipal getUserPrincipal() {
		try {
			return FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(getUsername());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// boolean or exception?
	public synchronized boolean createSecureFile(String pathName) {
		return createSecureFile(Paths.get(pathName));
	}

	public synchronized boolean createSecureFile(Path path) {
		try {
			path.toFile().createNewFile();
			FileOwnerAttributeView ownerView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
			ownerView.setOwner(getUserPrincipal());
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public synchronized boolean isFileSecure(String pathName) {
		return isFileSecure(Paths.get(pathName));
	}

	public synchronized boolean isFileSecure(Path path) {
		try {
			FileOwnerAttributeView userView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);

			UserPrincipal owner = userView.getOwner();
			return owner.getName().equals(getUsername());
		} catch (Exception e) {
			return false;
		}
	}

	public static void main(String[] args) throws IOException {
		SystemUserService sus = SystemUserService.getDefault();
		System.out.println("User: " + sus.getUsername());
		System.out.println("UID: " + sus.getUserId());
		System.out.println("GID: " + sus.getGroupId());
		System.out.println("groups: " + Arrays.toString(sus.getUserGroups()));
		System.out.println("Principal: " + sus.getUserPrincipal());

		System.out.println("Is secured: "+sus.isFileSecure("test.secu"));
		System.out.println("Create secure: "+sus.createSecureFile("test.secu"));
		System.out.println("Is secured: "+sus.isFileSecure("test.secu"));
		System.out.println("Not secured: "+sus.isFileSecure("pom.xml"));
		// Files.delete(Paths.get("test.secu"));
	}

}
