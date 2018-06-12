package dke.cbrm.gui;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import dke.cbrm.business.CbrmService;
import dke.cbrm.persistence.model.ContextModel;
import lombok.RequiredArgsConstructor;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadWindow extends Window {

    private static final long serialVersionUID = -458085787653435646L;

    private TextArea addNewContextsFolderTextArea,
	    addNewContextModelNameTextArea, addNewContextModelModuleTextArea;

    private final CbrmFileUploadReceiver receiver;

    private final CbrmService cbrmService;

    private Button uploadButton;

    private VerticalLayout addNewContextModelVerticalLayout;

    private Upload upload;

    @Value("${cbrm.workspace}")
    private String workspace;

    public void setUp() {
	/** Add Upload-functionality **/
	upload = new Upload("Upload Here", receiver);
	receiver.setUpload(upload);
	upload.addSucceededListener(receiver);
	upload.setImmediateMode(false);
	upload.setWidthUndefined();
	upload.setButtonCaption(null);
	
	/** evaluate context model with all files given */
	upload.addFinishedListener(new FinishedListener() {

	    private static final long serialVersionUID = 9205434627955380881L;

	    @Override
	    public void uploadFinished(FinishedEvent event) {
		ContextModel ctxModel = new ContextModel();
		ctxModel.setContextModelFilePath(workspace
			+ ((CbrmFileUploadReceiver) upload.getReceiver())
				.getFileName());
		ctxModel.setContextsFolderPath(
			addNewContextsFolderTextArea.getValue());
		ctxModel.setContextModelModuleName(
			addNewContextModelModuleTextArea.getValue());
		ctxModel.setName(addNewContextModelNameTextArea.getValue());

		cbrmService.addOrUpdateContextModel(ctxModel);

		try {
		    cbrmService.processNewContextModel(ctxModel);
		} catch (IOException e) {
		    Notification.show(
			    "Something went wrong while uploading...: ",
			    e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}

		UploadWindow.this.close();

		Notification.show("Upload finished successfully: "
			+ ((CbrmFileUploadReceiver) upload.getReceiver())
				.getFileName(),
			Notification.Type.HUMANIZED_MESSAGE);
	    }
	});

	addNewContextModelVerticalLayout = new VerticalLayout();
	addNewContextModelVerticalLayout.addComponent(upload);

	addNewContextModelNameTextArea = new TextArea();
	addNewContextModelNameTextArea
		.setCaption("Specify new Context-Model´s name");
	addNewContextModelNameTextArea.setWidth(300.0f, Unit.PIXELS);
	addNewContextModelNameTextArea.setHeight(40.0f, Unit.PIXELS);

	addNewContextModelNameTextArea
		.addValueChangeListener(new ValueChangeListener<String>() {

		    private static final long serialVersionUID =
			    7877720147700708431L;

		    @Override
		    public void valueChange(ValueChangeEvent<String> event) {
			uploadButton.setEnabled(allInputFieldsHaveInput());
		    }
		});

	addNewContextModelVerticalLayout
		.addComponent(addNewContextModelNameTextArea);

	addNewContextsFolderTextArea = new TextArea();
	addNewContextsFolderTextArea
		.setCaption("Specify folder of new Contexts if available");
	addNewContextsFolderTextArea.setWidth(300.0f, Unit.PIXELS);
	addNewContextsFolderTextArea.setHeight(40.0f, Unit.PIXELS);
	addNewContextsFolderTextArea
		.addValueChangeListener(new ValueChangeListener<String>() {

		    private static final long serialVersionUID =
			    5119985057684681047L;

		    @Override
		    public void valueChange(ValueChangeEvent<String> event) {
			uploadButton.setEnabled(allInputFieldsHaveInput());
		    }
		});
	addNewContextModelVerticalLayout
		.addComponent(addNewContextsFolderTextArea);

	addNewContextModelModuleTextArea = new TextArea();
	addNewContextModelModuleTextArea.setCaption("Specify name of module");
	addNewContextModelModuleTextArea.setWidth(300.0f, Unit.PIXELS);
	addNewContextModelModuleTextArea.setHeight(40.0f, Unit.PIXELS);
	addNewContextModelModuleTextArea
		.addValueChangeListener(new ValueChangeListener<String>() {

		    private static final long serialVersionUID =
			    -4085940467295160836L;

		    @Override
		    public void valueChange(ValueChangeEvent<String> event) {
			uploadButton.setEnabled(allInputFieldsHaveInput());
		    }
		});

	addNewContextModelVerticalLayout
		.addComponent(addNewContextModelModuleTextArea);

	uploadButton = new Button("Upload and process new Context-Model");
	uploadButton.setEnabled(false);
	uploadButton.addClickListener(new ClickListener() {
	    private static final long serialVersionUID = 5345313770300521424L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		upload.submitUpload();
	    }
	});
	addNewContextModelVerticalLayout.addComponent(uploadButton);

	this.setContent(addNewContextModelVerticalLayout);
    }

    private boolean allInputFieldsHaveInput() {
	if (addNewContextModelModuleTextArea.isEmpty()
		|| addNewContextModelNameTextArea.isEmpty()
		|| addNewContextsFolderTextArea.isEmpty()) {
	    return false;
	} else
	    return true;
    }

}
