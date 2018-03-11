package dke.cbrm.persistence.repositories;

import org.springframework.data.repository.CrudRepository;

import dke.cbrm.persistence.model.ModificationOperation;

public interface ModificationOperationRepository extends CrudRepository<ModificationOperation, Long> {

}
