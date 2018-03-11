package dke.cbrm.persistence.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import dke.cbrm.persistence.model.DetParamValue;

public interface DetParamValueRepository
	extends CrudRepository<DetParamValue, Long> {

    @Query("from DetParamValue p join p.parameter param where param.parameterId=:parameterId order by p.content")
    public Iterable<Object[]> findByParameterId(
	    @Param("parameterId") Long parameterId);
}
