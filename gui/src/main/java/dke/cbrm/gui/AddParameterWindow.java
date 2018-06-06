package dke.cbrm.gui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.ui.Button;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import dke.cbrm.business.CbrmService;
import dke.cbrm.business.statemachine.CbrmStateService;
import dke.cbrm.gui.dto.ParameterComponent;
import dke.cbrm.persistence.model.ContextModel;
import dke.cbrm.persistence.model.ModificationOperation;
import dke.cbrm.persistence.model.ModificationOperationType;
import dke.cbrm.persistence.model.Parameter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddParameterWindow extends Window {

    private static final long serialVersionUID = 8785148920793108482L;

    private final CbrmStateService cbrmStateService;

    private final CbrmService cbrmService;

    @Getter
    private String parameterName;

    @Getter
    private Set<Parameter> parameterValues;

    private ContextModel currentContextModel;

    private TextArea parameterNameTextArea;

    private VerticalLayout vMainLayout;

    private List<ParameterComponent> parameterValueComponents;

    public void setUp(ContextModel currentContextModel) {
	this.currentContextModel = currentContextModel;

	this.vMainLayout = new VerticalLayout();

	parameterValueComponents = new ArrayList<ParameterComponent>();

	parameterNameTextArea = new TextArea();
	vMainLayout.addComponent(parameterNameTextArea);
	this.setContent(vMainLayout);
    }

    private void processUserInput(ModificationOperation modOp) {
	LocalDateTime now = LocalDateTime.now();
	Parameter parameter = new Parameter();
	parameter.setBelongsToContextModel(currentContextModel);
	parameter.setCreatedAt(now);
	parameter.setModifiedAt(now);
	parameter.setValue(parameterName);

	ModificationOperation initialModificationOperation =
		cbrmService.createModificationOperation(
			ModificationOperationType.ADD_PARAMETER, parameter,
			null, null, parameter);

    }
    
    private void addNewParameterComponent(ParameterComponent parent) {
	
    }

}
