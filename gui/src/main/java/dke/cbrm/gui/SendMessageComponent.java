package dke.cbrm.gui;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.event.selection.SingleSelectionEvent;
import com.vaadin.event.selection.SingleSelectionListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;

import dke.cbrm.business.statemachine.CbrmStateService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendMessageComponent extends HorizontalLayout {

    private static final long serialVersionUID = 211616868612210525L;

    private final String MESSAGE_TO_ADMINS =
	    "Message to Repository Administrators";

    private final String MESSAGE_TO_RULE_DEVS =
	    "Message to Rule Developers of specific Context";

    private final CbrmStateService cbrmStateService;

    @Getter
    private ComboBox<String> comboBox;

    @Getter
    private Button okButton;

    @Getter
    private String messageMode;

    private TextArea messageText;

    private CbrmUI cbrmUi;

    public void initialize(CbrmUI cbrmUi) {
	this.cbrmUi = cbrmUi;
	
	comboBox = new ComboBox<String>();
	comboBox.setItems(
		Arrays.asList(MESSAGE_TO_ADMINS, MESSAGE_TO_RULE_DEVS));
	comboBox.setEnabled(!cbrmStateService.stateMachineIsRunning());
	comboBox.setSizeFull();
	comboBox.addSelectionListener(new SingleSelectionListener<String>() {

	    private static final long serialVersionUID = 8098193805045236959L;

	    @Override
	    public void selectionChange(SingleSelectionEvent<String> event) {
		Optional<String> opt = event.getSelectedItem();
		if (opt.isPresent()) {
		    messageMode = opt.get();

		    okButton.setEnabled(true);
		    messageText.setVisible(true);
		} else {
		    okButton.setEnabled(false);
		    messageText.setVisible(false);
		}
	    }

	});

	okButton = new Button("Send Message");
	okButton.setEnabled(!cbrmStateService.stateMachineIsRunning());
	okButton.addClickListener(new ClickListener() {

	    private static final long serialVersionUID = -6056890771904118502L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		if (messageMode.equals(MESSAGE_TO_RULE_DEVS)
			&& cbrmUi.getContextSelected() != null) {
		    Notification.show("Error sending User Message: ",
			    "A context from hierarchy has to be selected in order to refer to with your message.",
			    Notification.Type.ERROR_MESSAGE);
		} else if (messageMode.equals(MESSAGE_TO_ADMINS)) {

		}
	    }

	});

	this.addComponent(comboBox);
	this.addComponent(okButton);
	this.addComponent(messageText = new TextArea());
	messageText.setVisible(false);
	this.setSizeFull();
    }

}
