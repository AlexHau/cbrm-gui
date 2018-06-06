package dke.cbrm.gui.dto;

import com.vaadin.ui.Component;

import dke.cbrm.persistence.model.DetParamValue;
import dke.cbrm.persistence.model.Rule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class RuleComponent {

    private Rule rule;
    
    private Component component;
}
