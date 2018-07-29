package chatte.sys;

import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

public class UnixUserService extends SystemUserService {

	private Object system;
	private Method mGetUsername;
	private Method mGetUserId;
	private Method mGetGroupId;
	private Method mGetUserGroups;

	public UnixUserService() throws SystemUserException {
		Class<?> cls;
		try {
			cls = Class.forName("com.sun.security.auth.module.UnixSystem");
		} catch (Exception uxEx) {
			try {
				cls = Class.forName("com.sun.security.auth.module.SolarisSystem");
			} catch (Exception slEx) {
				throw new SystemUserException(uxEx);
			}
		}
		try {
			mGetUsername = cls.getMethod("getUsername");
			mGetUserId = cls.getMethod("getUid");
			mGetGroupId = cls.getMethod("getGid");
			mGetUserGroups = cls.getMethod("getGroups");
			system = cls.newInstance();
		} catch (Exception ex) {
			throw new SystemUserException(ex);
		}
	}

	@Override
	public String getUsername() {
		try {
			return (String) mGetUsername.invoke(system);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String getUserId() {
		try {
			return String.valueOf(mGetUserId.invoke(system));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String getGroupId() {
		try {
			return String.valueOf(mGetGroupId.invoke(system));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public String[] getUserGroups() {
		try {
			long[] groups = (long[]) mGetUserGroups.invoke(system);
			String[] result = new String[groups.length];
			for (int i = 0; i < groups.length; i++)
				result[i] = String.valueOf(groups[i]);
			return result;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public synchronized boolean createSecureFile(Path path) {
		try {
			if (!super.createSecureFile(path))
				return false;

			PosixFileAttributeView posixView = Files.getFileAttributeView(path, PosixFileAttributeView.class);
			if (posixView == null)
				return false;

			Set<PosixFilePermission> permissions = EnumSet.of(OWNER_READ, OWNER_WRITE);
			posixView.setPermissions(permissions);

		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public synchronized boolean isFileSecure(Path path) {
		try {
			// must have same owner
			if (!super.isFileSecure(path))
				return false;

			PosixFileAttributeView posixView = Files.getFileAttributeView(path, PosixFileAttributeView.class);
			if (posixView == null)
				return false;

			// must have only user permissions and not Group and Others permissions
			PosixFileAttributes attribs = posixView.readAttributes();
			Set<PosixFilePermission> permissions = attribs.permissions();
			Set<PosixFilePermission> requiredPermissions = EnumSet.of(OWNER_READ, OWNER_WRITE);
			if (!permissions.containsAll(requiredPermissions))
				return false;
			// OWNER_EXECUTE might exist, I will ignore it for now...
			permissions.removeAll(requiredPermissions);
			permissions.remove(OWNER_EXECUTE);
			return permissions.isEmpty();
		} catch (Exception e) {
			return false;
		}
	}

}
