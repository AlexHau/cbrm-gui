package dke.cbrm.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ahauer
 *
 *         Class is responsible for handling upload-activity: 1. write
 *         to the workspace-folder on file system 2. show
 *         success-Notification in case everything was ok
 */
@Service
@Scope("prototype")
public class CbrmFileUploadReceiver implements Receiver, SucceededListener {

    private static final long serialVersionUID = 5379195666584541870L;

    @Setter
    private Upload upload;

    @Getter
    private String fileName;

    @Override
    public void uploadSucceeded(SucceededEvent event) {
	Notification.show("Upload finished successfully: ", fileName,
		Notification.Type.HUMANIZED_MESSAGE);
    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
	this.fileName = filename;
	FileOutputStream fos = null;
	try {
	    fos = new FileOutputStream(new File(filename));
	} catch (final java.io.FileNotFoundException e) {
	    return null;
	}
	return fos;
    }

}
