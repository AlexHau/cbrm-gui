package dke.cbrm.business;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import dke.cbrm.CbrmConstants;
import dke.cbrm.persistence.model.Context;
import dke.cbrm.persistence.model.Role;
import dke.cbrm.persistence.model.User;
import dke.cbrm.persistence.repositories.RoleRepository;
import dke.cbrm.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PermissionService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    public Iterable<User> getAllExistingUsers() {
	return userRepository.findAll();
    }

    public boolean userAllowedToEditContextRules(Context context,
	    String userName) {
	Iterator<User> allowedUsersIter = userRepository
		.getAllowedUsersByContextId(context.getContextId()).iterator();
	while (allowedUsersIter.hasNext()) {
	    User u = allowedUsersIter.next();
	    if (u.getUserName().equals(userName)) {
		return true;
	    }
	}
	return false;
    }

    public Iterable<User> getAllowedRuleDevsForContext(Context ctx) {
	return userRepository.getAllowedUsersByContextId(ctx.getContextId());
    }

    public boolean userHasRoleSpecified(String userName, String roleName) {
	User user = userRepository.findByUserName(userName);

	if (user != null) {
	    Iterator<Role> roles =
		    roleRepository.getRolesByUserId(user.getId()).iterator();
	    while (roles.hasNext()) {
		Role role = roles.next();
		if (role.getName().equals(roleName)) {
		    return true;
		}
	    }
	}

	return false;
    }

    @SuppressWarnings("unchecked")
    public Collection<User> getNotAllowedUsersForContext(
	    Iterable<User> allowedUsersForContext) {
	Iterator<User> iter = userRepository
		.getUsersByRoleName(CbrmConstants.UserRoles.ROLE_RULE_DEV)
		.iterator();
	Set<User> allRuleDevUsers = new HashSet<User>();
	while (iter.hasNext()) {
	    User user = iter.next();
	    user.setRoles(Sets.newHashSet(
		    userRepository.loadRoleForUser(user.getId()).iterator()));
	    allRuleDevUsers.add(user);
	}

	return CollectionUtils.subtract(Sets.newHashSet(allRuleDevUsers),
		Sets.newHashSet(allowedUsersForContext));
    }

    public void loadAllowedUsersOfContext(Context ctx) {
	Iterator<User> iter = userRepository
		.getAllowedUsersByContextId(ctx.getContextId()).iterator();
	Set<User> loadedUsers = new HashSet<User>();
	while (iter.hasNext()) {
	    User user = iter.next();
	    user = loadRolesOfAllowedUsers(user);
	    loadedUsers.add(user);
	}
	ctx.setAllowedUsers(loadedUsers);
    }

    public User loadRolesOfAllowedUsers(User user) {
	user.setRoles(Sets.newHashSet(
		userRepository.loadRoleForUser(user.getId()).iterator()));
	return user;

    }

    public Iterable<Role> getRolesForUser(Long userId) {
	return roleRepository.getRolesByUserId(userId);
    }
    
    @SuppressWarnings("unchecked")
    public Collection<Role>  getRolesNotForUser(Long userId) {
	Set<Role> rolesForUser = Sets.newHashSet(getRolesForUser(userId));
	Set<Role> allRoles = Sets.newHashSet(getAllExistingRoles());
	
	return CollectionUtils.subtract(allRoles, rolesForUser);
    }

    public Iterable<Role> getAllExistingRoles() {
	return roleRepository.getAllRolesWithUsers();
    }

    public void updateUser(User userSelected) {
	userRepository.save(userSelected);
    }
    
    

}
