package dke.cbrm.persistence.repositories;

import javax.persistence.NonUniqueResultException;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import dke.cbrm.persistence.model.Context;

@Component
public interface ContextRepository extends CrudRepository<Context, Long> {

    @Query("from Context ctx left join ctx.parent ctxParent where ctxParent.contextId =:contextId"
	    + " and (ctx.validTo < current_date() or ctx.validTo is null)" 
	    + " and ctx.validFrom is not null")
    Iterable<Object[]> getChildrenOfParent(@Param("contextId") Long contextId);

    @Query("from Context ctx join ctx.instantiatesContextModel ctxModel"
	    + " where ctx.value = :contextName and ctxModel.id = :contextModelId"
	    + " and (ctx.validTo < current_date() or ctx.validTo is null)")
    Context getOnlyValidContextByName(@Param("contextName") String contextName,
	    @Param("contextModelId") Long contextModelId)
	    throws NonUniqueResultException;

    @Query("select ctx from Context ctx join ctx.instantiatesContextModel ctxModel"
	    + " where ctxModel.name = :contextModel and" 
	    + " (ctx.validTo < current_date() or ctx.validTo is null)"
	    + " and ctx.validFrom is not null")
    Iterable<Context> getContextsForContextModel(
	    @Param("contextModel") String contextModelName);

}
