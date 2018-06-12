package dke.cbrm.persistence.repositories;

import java.util.Collection;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import dke.cbrm.persistence.model.Context;
import dke.cbrm.persistence.model.Parameter;

public interface ParameterRepository extends CrudRepository<Parameter, Long> {

    @Query("select param from Parameter param left join param.parent paramParent"
	    + " where paramParent.parameterId =:parameterId")
    Iterable<Parameter> getChildrenOfParent(
	    @Param("parameterId") Long parameterId);

    @Query("select param from Parameter param join param.belongsToContextModel ctxModel"
	    + " where param.value = :paramName and ctxModel.name = :contextModel")
    Parameter getParameterByName(@Param("paramName") String paramName,
	    @Param("contextModel") String contextModelName);

    @Query("select param from Parameter param join param.belongsToContextModel ctxModel"
	    + " where ctxModel.name = :contextModel ")
    Iterable<Parameter> getAllParametersOfContextModel(
	    @Param("contextModel") String contextModel);

    @Query(
	    nativeQuery = true,
	    value = "with recursive parent_param as ("
		    + " select p.* from Parameter p where p.parent_parameter_id is null"
		    + " union select p2.*_id from Parameter p2"
		    + " join parent_param pp"
		    + " on pp.parameter_id = p2.parent_parameter_id"
		    + " where p2.parameter_id = :parameterId)"
		    + ", children_param as (select p.* from Parameter p"
		    + " where p.parameter_id = :parameterId union"
		    + " select p2.* from Parameter p2"
		    + " join children_param  jp"
		    + " on jp.parent_parameter_id = p2.parameter_id)"
		    + " select * from parent_param"
		    + " where parameter_id IN (Select parameter_id from children_param)"
		    + " and parameter_id <> :parameterId")
    Parameter getParentParameter(@Param("parameterId") Long parameterId);

    @Query("select param from Context ctx join ctx.constitutingParameterValues param"
	    + " where ctx.contextId = :contextId")
    Iterable<Parameter> getParametersOfContext(
	    @Param("contextId") Long contextId);

    @Query("select param from Parameter param join param.belongsToContextModel ctxModel"
	    + " where ctxModel.id = :contextModelId and param.parent is null")
    Iterable<Parameter> getRootParentParameters(
	    @Param("contextModelId") Long contextModelId);

    @Query(
	    nativeQuery = true,
	    value = "with recursive parent_param as ("
		    + " select p.* from Parameter p where p.parameter_id = :paramId"
		    + " union" + " select p2.* from Parameter p2"
		    + " join parent_param pp"
		    + " on pp.parameter_id = p2.parent_parameter_id)"
		    + " select * from parent_param where parameter_id <> :paramId")
    Iterable<Parameter> getParameterValuesBelow(@Param("paramId") Long paramId);

    @Query("select ctx from Context ctx"
	    + " join ctx.instantiatesContextModel ctxModel"
	    + " join ctx.constitutingParameterValues params"
	    + " where ctxModel.id = :ctxModelId and params IN :parameters")
    Iterable<Context> findContextWithParameters(
	    @Param("ctxModelId") Long ctxModelId,
	    @Param("parameters") Collection<Parameter> params);
}
