package dke.cbrm.gui.dto;

import dke.cbrm.persistence.model.Context;
import dke.cbrm.persistence.model.Parameter;
import lombok.Data;

/**
 * @author ahauer
 * 
 *         DTO-Class for the representation of @{link Context) through
 *         determining {@link Parameter}s in @{link TreeGrid}
 *
 */
@Data
public class ParameterGridDto {

    private Parameter[] parameters;

    private Context context;

}
