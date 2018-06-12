package dke.cbrm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dke.cbrm.persistence.model.Context;
import dke.cbrm.persistence.model.Privilege;
import dke.cbrm.persistence.model.Role;
import dke.cbrm.persistence.model.User;
import dke.cbrm.persistence.repositories.ContextRepository;
import dke.cbrm.persistence.repositories.PrivilegeRepository;
import dke.cbrm.persistence.repositories.RoleRepository;
import dke.cbrm.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * This class is responsible for setting up @{link User}, @{link Role}
 * and @{link Privilege}-Objects according to CBRM´s permission model
 * on @{link ContextRefreshedEvent}, which is triggered whenever the
 * ApplicationContext is reloaded or instantiated newly
 * 
 * Example taken from
 * http://www.baeldung.com/role-and-privilege-for-spring-security-registration
 * [29.11.2017]
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserRolePrivilegeInitializer {
    boolean alreadySetup = false;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PrivilegeRepository privilegeRepository;

    private final ContextRepository contextRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void initializeUsers() {

	if (alreadySetup)
	    return;
	Privilege readContextPrivilege = createPrivilegeIfNotFound(
		CbrmConstants.UserPrivileges.READ_CONTEXT_PRIVILEGE);
	Privilege writeContextPrivilege = createPrivilegeIfNotFound(
		CbrmConstants.UserPrivileges.WRITE_CONTEXT_PRIVILEGE);

	Privilege readContextClassPrivilege = createPrivilegeIfNotFound(
		CbrmConstants.UserPrivileges.READ_CONTEXT_CLASS_PRIVILEGE);
	Privilege writeContextClassPrivilege = createPrivilegeIfNotFound(
		CbrmConstants.UserPrivileges.WRITE_CONTEXT_CLASS_PRIVILEGE);

	Privilege readParameterPrivilege = createPrivilegeIfNotFound(
		CbrmConstants.UserPrivileges.READ_PARAMETER_PRIVILEGE);
	Privilege writeParameterPrivilege = createPrivilegeIfNotFound(
		CbrmConstants.UserPrivileges.WRITE_PARAMETER_PRIVILEGE);

	Privilege readRulePrivilege = createPrivilegeIfNotFound(
		CbrmConstants.UserPrivileges.READ_RULE_PRIVILEGE);
	Privilege writeRulePrivilege = createPrivilegeIfNotFound(
		CbrmConstants.UserPrivileges.WRITE_RULE_PRIVILEGE);

	Privilege readBusinessCasePrivilege = createPrivilegeIfNotFound(
		CbrmConstants.UserPrivileges.READ_BUSINESS_CASE_PRIVILEGE);
	Privilege writeBusinessCasePrivilege = createPrivilegeIfNotFound(
		CbrmConstants.UserPrivileges.WRITE_BUSINESS_CASE_PRIVILEGE);

	Privilege readBusinessCaseClassPrivilege = createPrivilegeIfNotFound(
		CbrmConstants.UserPrivileges.READ_BUSINESS_CASE_CLASS_PRIVILEGE);
	Privilege writeBusinessCaseClassPrivilege = createPrivilegeIfNotFound(
		CbrmConstants.UserPrivileges.WRITE_BUSINESS_CASE_CLASS_PRIVILEGE);

	List<Privilege> allReadPrivileges = Arrays.asList(
		readBusinessCaseClassPrivilege, readBusinessCasePrivilege,
		readContextClassPrivilege, readContextPrivilege,
		readParameterPrivilege, readRulePrivilege);

	List<Privilege> adminPrivileges = new ArrayList<Privilege>();
	adminPrivileges.addAll(allReadPrivileges);
	adminPrivileges.addAll(Arrays.asList(writeBusinessCaseClassPrivilege,
		writeBusinessCasePrivilege, writeContextClassPrivilege,
		writeContextPrivilege, writeParameterPrivilege,
		writeRulePrivilege));

	List<Privilege> domainExpertPrivileges = new ArrayList<Privilege>();
	domainExpertPrivileges.addAll(allReadPrivileges);
	domainExpertPrivileges.add(writeBusinessCaseClassPrivilege);

	List<Privilege> ruleDev1eloperPrivileges = new ArrayList<Privilege>();
	ruleDev1eloperPrivileges.addAll(allReadPrivileges);
	ruleDev1eloperPrivileges.add(writeRulePrivilege);

	createRoleIfNotFound(CbrmConstants.UserRoles.ROLE_REPO_ADMIN,
		adminPrivileges);
	createRoleIfNotFound(CbrmConstants.UserRoles.ROLE_USER,
		allReadPrivileges);
	createRoleIfNotFound(CbrmConstants.UserRoles.ROLE_DOMAIN_EXPERT,
		domainExpertPrivileges);
	createRoleIfNotFound(CbrmConstants.UserRoles.ROLE_RULE_DEV,
		ruleDev1eloperPrivileges);

	Role adminRole = roleRepository
		.findByName(CbrmConstants.UserRoles.ROLE_REPO_ADMIN);
	User adminUser = new User();
	adminUser.setFirstName("admin");
	adminUser.setLastName("admin");
	adminUser.setUserName("admin");
	adminUser.setPassword(passwordEncoder.encode("admin"));
	adminUser.setEmail("admin@test.com");
	adminUser.setRoles(Collections.singleton(adminRole));
	adminUser.setEnabled(true);
	userRepository.save(adminUser);

	Role userRole =
		roleRepository.findByName(CbrmConstants.UserRoles.ROLE_USER);
	User user = new User();
	user.setFirstName("user");
	user.setLastName("user");
	user.setUserName("user");
	user.setPassword(passwordEncoder.encode("user"));
	user.setEmail("user@test.com");
	user.setRoles(Collections.singleton(userRole));
	user.setEnabled(true);
	userRepository.save(user);

	// -------------------------------------------//
	// Initializing Rule-Developer Users and relating Contexts
	Role ruleDev1Role = roleRepository
		.findByName(CbrmConstants.UserRoles.ROLE_RULE_DEV);

	Iterator<Context> foundContexts =
		contextRepository.findAll().iterator();

	User ruleDev1 = new User();
	ruleDev1.setFirstName("ruleDev1");
	ruleDev1.setLastName("ruleDev1");
	ruleDev1.setUserName("ruleDev1");
	ruleDev1.setPassword(passwordEncoder.encode("ruleDev1"));
	ruleDev1.setEmail("ruleDev1@test.com");
	ruleDev1.setRoles(Collections.singleton(ruleDev1Role));
	ruleDev1.setEnabled(true);
	userRepository.save(ruleDev1);

	int cnt = 0;

	while (cnt < 5 && foundContexts.hasNext()) {
	    Context ctx = foundContexts.next();
	    ctx.getAllowedUsers().add(ruleDev1);
	    contextRepository.save(ctx);
	    cnt++;
	}

	User ruleDev2 = new User();
	ruleDev2.setFirstName("ruleDev2");
	ruleDev2.setLastName("ruleDev2");
	ruleDev2.setUserName("ruleDev2");
	ruleDev2.setPassword(passwordEncoder.encode("ruleDev2"));
	ruleDev2.setEmail("ruleDev2@test.com");
	ruleDev2.setRoles(Collections.singleton(ruleDev1Role));
	ruleDev2.setEnabled(true);
	userRepository.save(ruleDev2);

	while (cnt < 10 && foundContexts.hasNext()) {
	    Context ctx = foundContexts.next();
	    ctx.getAllowedUsers().add(ruleDev2);
	    contextRepository.save(ctx);
	    cnt++;
	}

	Role domainExpertRole = roleRepository
		.findByName(CbrmConstants.UserRoles.ROLE_DOMAIN_EXPERT);
	User domainExpertUser = new User();
	domainExpertUser.setFirstName("domainExpert");
	domainExpertUser.setLastName("domainExpert");
	domainExpertUser.setUserName("domainExpert");
	domainExpertUser.setPassword(passwordEncoder.encode("domainExpert"));
	domainExpertUser.setEmail("domainExpert@test.com");
	domainExpertUser.setRoles(Collections.singleton(domainExpertRole));
	domainExpertUser.setEnabled(true);
	userRepository.save(domainExpertUser);

	alreadySetup = true;
    }

    @Transactional
    private Privilege createPrivilegeIfNotFound(String name) {

	Privilege privilege = privilegeRepository.findByName(name);
	if (privilege == null) {
	    privilege = new Privilege(name);
	    privilegeRepository.save(privilege);
	}
	return privilege;
    }

    @Transactional
    private Role createRoleIfNotFound(String name,
	    Collection<Privilege> privileges) {

	Role role = roleRepository.findByName(name);
	if (role == null) {
	    role = new Role(name);
	    role.setPrivileges(privileges);
	    roleRepository.save(role);
	}
	return role;
    }
}
