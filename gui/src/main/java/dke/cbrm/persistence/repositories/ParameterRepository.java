package dke.cbrm.persistence.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import dke.cbrm.persistence.model.Parameter;

public interface ParameterRepository extends CrudRepository<Parameter, Long> {

    @Query("from Parameter param left join param.parent paramParent where paramParent.parameterId =:parameterId")
    Iterable<Object[]> getChildrenOfParent(
	    @Param("parameterId") Long contextId);

    @Query("from Parameter param where param.value = :paramName ")
    Parameter getParameterByName(@Param("paramName") String paramName);

    @Query("from Parameter param join param.belongsToContextModel ctxModel where ctxModel.name = :contextModel ")
    Iterable<Object[]> getAllParametersOfContextModel(
	    @Param("contextModel") String contextModel);
}
