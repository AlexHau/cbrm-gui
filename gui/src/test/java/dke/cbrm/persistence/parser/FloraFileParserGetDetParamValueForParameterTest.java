package dke.cbrm.persistence.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class FloraFileParserGetDetParamValueForParameterTest
	extends BaseFileParserTest {

    private BufferedReader bufferedReader;

    @Before
    public void setUp() throws IOException {
	super.setUp();
    }

    @Test
    public void testGetDetParamValueForParameter()
	    throws IOException {
	setUpFilePath("detParamValueEventScenario");
	bufferedReader = new BufferedReader(new FileReader(
		new File(this.getFilePath())));

	List<String> values = flParser
		.getDetParamValueForParameter(
			"EventScenario");

	String line = null;
	int cnt = 0;
	String[] valueArray = new String[values.size()];
	valueArray = values.toArray(valueArray);
	while ((line = bufferedReader.readLine()) != null) {
	    Assert.assertEquals(valueArray[cnt], line);
	    cnt++;
	}
    }

    @Test
    public void testGetDetParamValueForParamaterForInterestWithExtraFacts()
	    throws IOException {
	setUpFilePath("detParamValueInterest");
	bufferedReader = new BufferedReader(new FileReader(
		new File(this.getFilePath())));

	List<String> values = flParser
		.getDetParamValueForParameter("Interest");

	String line = null;
	int cnt = 0;
	String[] valueArray = new String[values.size()];
	valueArray = values.toArray(valueArray);
	while ((line = bufferedReader.readLine()) != null) {
	    Assert.assertEquals(line, valueArray[cnt]);
	    cnt++;
	}
    }
}
