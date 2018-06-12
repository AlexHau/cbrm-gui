package dke.cbrm.gui.dto;

import com.vaadin.ui.Component;

import dke.cbrm.persistence.model.DetParamValue;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DetParamValueComponent {

    private DetParamValue detParamValue;

    private Component component;
}
