package chatte.sys;

import static java.nio.file.attribute.AclEntryPermission.READ_DATA;
import static java.nio.file.attribute.AclEntryPermission.WRITE_DATA;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class NTUserService extends SystemUserService {

	private Object system;
	private Method mGetUsername;
	private Method mGetUserId;
	private Method mGetGroupId;
	private Method mGetUserGroups;

	public NTUserService() throws SystemUserException {
		try {
			Class<?> cls = Class.forName("com.sun.security.auth.module.NTSystem");
			mGetUsername = cls.getMethod("getName");
			mGetUserId = cls.getMethod("getUserSID");
			mGetGroupId = cls.getMethod("getPrimaryGroupID");
			mGetUserGroups = cls.getMethod("getGroupIDs");
			system = cls.newInstance();
		} catch (Exception e) {
			throw new SystemUserException(e);
		}
	}

	@Override
	public String getUsername() {
		try {
			return (String) mGetUsername.invoke(system);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getUserId() {
		try {
			return (String) mGetUserId.invoke(system);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getGroupId() {
		try {
			return (String) mGetGroupId.invoke(system);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String[] getUserGroups() {
		try {
			return (String[]) mGetUserGroups.invoke(system);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized boolean createSecureFile(Path path) {
		try {
			if (!super.createSecureFile(path))
				return false;

			AclFileAttributeView aclView = Files.getFileAttributeView(path, AclFileAttributeView.class);
			if (aclView == null)
				return false;

			UserPrincipal principal = getUserPrincipal();

			Set<AclEntryPermission> permissions = EnumSet.of(READ_DATA, WRITE_DATA);

			AclEntry.Builder builder = AclEntry.newBuilder();
			builder.setPrincipal(principal);
			builder.setType(AclEntryType.ALLOW);
			builder.setPermissions(permissions);
			AclEntry newEntry = builder.build();

			List<AclEntry> aclEntries = Arrays.asList(newEntry);

			aclView.setAcl(aclEntries);
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

			AclFileAttributeView aclView = Files.getFileAttributeView(path, AclFileAttributeView.class);
			if (aclView == null)
				return false;

			// must preserve same ACL
			List<AclEntry> aclEntries = aclView.getAcl();
			if (aclEntries.size() != 1)
				return false;

			AclEntry entry = aclEntries.get(0);

			// check entry principal
			if (!entry.principal().getName().equals(getUsername()))
				return false;

			// must be read/write only - not sure about this. what if the user changes other
			// kind of permission?
			Set<AclEntryPermission> permissions = entry.permissions();
			Set<AclEntryPermission> requiredPermissions = EnumSet.of(READ_DATA, WRITE_DATA);
			if (!permissions.containsAll(requiredPermissions))
				return false;
			permissions.removeAll(requiredPermissions);
			return permissions.isEmpty();
		} catch (Exception e) {
			return false;
		}
	}
}
