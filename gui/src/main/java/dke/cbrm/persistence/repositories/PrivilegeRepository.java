package dke.cbrm.persistence.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import dke.cbrm.persistence.model.Privilege;

public interface PrivilegeRepository extends CrudRepository<Privilege, Long> {

	@Query("from Privilege where name=:privilegeName")
	public Privilege findByName(@Param("privilegeName")String privilegeName);

}
