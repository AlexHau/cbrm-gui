package dke.cbrm.persistence.repositories;

import org.springframework.data.repository.CrudRepository;

import dke.cbrm.persistence.model.ModificationApproval;

public interface ModificationApprovalRepository
	extends CrudRepository<ModificationApproval, Long> {

}
