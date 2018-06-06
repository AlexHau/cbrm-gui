package dke.cbrm.persistence.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import dke.cbrm.persistence.model.ModificationOperation;

public interface ModificationOperationRepository
	extends CrudRepository<ModificationOperation, Long> {

    @Query("select modOp from ModificationOperation modOp join fetch modOp.approvals apps"
	    + " join fetch modOp.contextAffected"// join fetch modOp.before before" 
	    + " where apps.approvedAt is null and modOp.before is null")
    Iterable<ModificationOperation> getNotApprovedAndInitialModOps();
    
    @Query("select modOp from ModificationOperation modOp join modOp.before before"
	    + " where before.id = :modOpId")
    ModificationOperation getModOpFollowedBy(@Param("modOpId") Long modOpId);

}
