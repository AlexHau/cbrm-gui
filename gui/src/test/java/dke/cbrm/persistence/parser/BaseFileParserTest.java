package dke.cbrm.persistence.parser;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.Before;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ResourceUtils;

import dke.cbrm.persistence.model.ContextModel;
import lombok.Getter;

public abstract class BaseFileParserTest {

    protected FloraFileParser flParser;

    @Getter
    private String filePath;

    @Before
    public void setUp() throws IOException {
	setUpFilePath("ctxModelAIM.flr");

	ContextModel ctxModel = new ContextModel();
	ctxModel.setContextModelFilePath(filePath);
	ctxModel.setContextsFolderPath(
		"/home/ahauer/Flora-2/flora2/OO/Contexts/");
	ctxModel.setName("AIMCtx");

	flParser = new FloraFileParser(ctxModel);

	ReflectionTestUtils.setField(flParser, null,
		"domainContextModelContent",
		FloraFileParser.readFileContent(Paths.get(filePath)),
		String.class);

    }

    protected void setUpFilePath(String filePath) throws IOException {

	URL url = this.getClass().getClassLoader().getResource(filePath);
	this.filePath = (ResourceUtils.getFile(url).getPath());

    }
}
