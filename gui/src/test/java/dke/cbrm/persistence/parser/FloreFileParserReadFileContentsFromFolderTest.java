package dke.cbrm.persistence.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dke.cbrm.business.CbrmService;
import dke.cbrm.persistence.model.Context;

@RunWith(SpringJUnit4ClassRunner.class)
public class FloreFileParserReadFileContentsFromFolderTest extends BaseFileParserTest {

    @Mock
    CbrmService cbrmService;

    @Test
    public void extractEntitiesTest() {
	Context ctx = new Context();
	Mockito.doAnswer(new Answer<Void>() {
	    public Void answer(
		    InvocationOnMock invocation) {
		String method = invocation.getMethod().getName();
		System.out.println("called method: "
			+ method);
		return null;
	    }
	}).when(cbrmService).addOrUpdateContext(ctx);
	flParser.readFileContentsFromFolder();
    }
}
