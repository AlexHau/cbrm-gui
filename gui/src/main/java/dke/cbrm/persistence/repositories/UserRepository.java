package dke.cbrm.persistence.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import dke.cbrm.persistence.model.Role;
import dke.cbrm.persistence.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

    @Query("from User where email = :email")
    public User findByEmail(@Param("email") String email);

    @Query("from User where userName = :userName")
    public User findByUserName(@Param("userName") String userName);

    @Query("select user from Context ctx join ctx.allowedUsers user join user.roles role"
	    + " where ctx.contextId = :contextId and role.name = :roleName")
    public Iterable<User> getAllowedUsersByContextIdAndRole(
	    @Param("contextId") Long contextId,
	    @Param("roleName") String roleName);

    @Query("select role from User user join user.roles role where user.id = :userId ")
    public Iterable<Role> loadRoleForUser(@Param("userId") Long userId);

    @Query("select user from User user join user.roles role where role.name = :roleName ")
    public Iterable<User> getUsersByRoleName(
	    @Param("roleName") String roleRuleDev);

}
