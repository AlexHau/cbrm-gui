package dke.cbrm.persistence.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import dke.cbrm.persistence.model.Context;

@Component
public interface ContextRepository extends CrudRepository<Context, Long> {

    @Query("from Context ctx left join ctx.parent ctxParent where ctxParent.contextId =:contextId")
    Iterable<Object[]> getChildrenOfParent(@Param("contextId") Long contextId);

    @Query("from Context ctx join ctx.instantiatesContextModel ctxModel where ctx.value = :contextName and ctxModel.name = :contextModel")
    Context getContextByName(@Param("contextName") String contextName,
	    @Param("contextModel") String contextModel);

}
