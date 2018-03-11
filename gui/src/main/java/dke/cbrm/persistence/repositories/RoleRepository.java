package dke.cbrm.persistence.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import dke.cbrm.persistence.model.Role;

public interface RoleRepository
	extends CrudRepository<Role, Long> {

    @Query("from Role where name=:roleName")
    public Role findByName(
	    @Param("roleName") String roleName);

    @Query("from Role role left join role.users users where users.userName = :userName")
    public Iterable<Object[]> getRolesByUserName(
	    @Param("userName") String userName);

}
