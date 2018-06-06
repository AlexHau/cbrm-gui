package dke.cbrm.persistence.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import dke.cbrm.persistence.model.Context;
import dke.cbrm.persistence.model.ContextModel;

public interface ContextModelRepository
	extends CrudRepository<ContextModel, Long> {

    @Query("select ctx from ContextModel ctxModel join ctxModel.contexts ctx where ctxModel.id = :ctxModelId")
    Iterable<Context> getAllContextsOfContextModel(@Param("ctxModelId") Long ctxModelId);

}