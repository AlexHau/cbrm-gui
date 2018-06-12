package dke.cbrm.persistence.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import dke.cbrm.persistence.model.Rule;

public interface RuleRepository extends CrudRepository<Rule, Long> {

    @Query("from Rule rule join rule.relatesTo ctx join ctx.instantiatesContextModel ctxModel where ctx.value=:contextName and ctxModel.name = :contextModelName order by rule.ruleName")
    public Iterable<Object[]> findByContextName(
	    @Param("contextName") String contextName,
	    @Param("contextModelName") String currentContextModel);
}
