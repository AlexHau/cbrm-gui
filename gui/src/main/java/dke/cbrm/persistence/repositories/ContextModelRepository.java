package dke.cbrm.persistence.repositories;

import org.springframework.data.repository.CrudRepository;

import dke.cbrm.persistence.model.ContextModel;

public interface ContextModelRepository
	extends CrudRepository<ContextModel, Long> {

}