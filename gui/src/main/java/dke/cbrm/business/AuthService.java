package dke.cbrm.business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import dke.cbrm.persistence.model.Privilege;
import dke.cbrm.persistence.model.Role;
import dke.cbrm.persistence.model.User;
import dke.cbrm.persistence.repositories.RoleRepository;
import dke.cbrm.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service("authService")
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String userName)
	    throws UsernameNotFoundException {

	User user = userRepository.findByUserName(userName);
	if (user == null) {
	    return new org.springframework.security.core.userdetails.User(
		    " ", " ", true, true, true, true,
		    getAuthorities(Arrays.asList(
			    roleRepository.findByName("ROLE_USER"))));
	}

	return new org.springframework.security.core.userdetails.User(
		user.getUserName(), user.getPassword(),
		user.isEnabled(), true, true, true,
		getAuthorities(user.getRoles()));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(
	    Collection<Role> roles) {

	return getGrantedAuthorities(getPrivileges(roles));
    }

    private List<String> getPrivileges(Collection<Role> roles) {

	List<String> privileges = new ArrayList<>();
	List<Privilege> collection = new ArrayList<>();
	for (Role role : roles) {
	    collection.addAll(role.getPrivileges());
	}
	for (Privilege item : collection) {
	    privileges.add(item.getName());
	}
	return privileges;
    }

    private List<GrantedAuthority> getGrantedAuthorities(
	    List<String> privileges) {
	List<GrantedAuthority> authorities = new ArrayList<>();
	for (String privilege : privileges) {
	    authorities.add(new SimpleGrantedAuthority(privilege));
	}
	return authorities;
    }

}
