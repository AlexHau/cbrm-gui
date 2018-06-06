package dke.cbrm.gui.dto;

import com.vaadin.ui.Component;

import dke.cbrm.persistence.model.Parameter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParameterComponent {

    private Component component;

    private Parameter parameter;
    
    private ParameterComponent parent;
}
