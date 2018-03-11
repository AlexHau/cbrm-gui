package dke.cbrm.persistence.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class FloraFileParserWriteNewParameterAndDeleteTest
	extends BaseFileParserTest {

    @Test
    public void writeNewParameterValueToFile() {
	String drone = "drone";
	Assert.assertFalse(
		flParser.getDomainContextModelContent()
			.contains(drone));

	flParser.writeNewParameterValueToFile(drone,
		"Interest", "aircraft");

	Assert.assertTrue(
		flParser.getDomainContextModelContent()
			.contains(drone));

	flParser.deleteParameterValue(drone, "aircraft");

	Assert.assertFalse(
		flParser.getDomainContextModelContent()
			.contains(drone));
    }

    @Test
    public void addNewParameterValueToFileWithNewParent() {
	String parameterValue = "drone";
	String parentParameterValue = "unspecifiedAircraft";
	String parentParameter = "Interest";

	Assert.assertFalse(
		flParser.getDomainContextModelContent()
			.contains(parameterValue));

	flParser.writeNewParameterValueToFile(
		parameterValue, parentParameter,
		parentParameterValue);

	Matcher parentParameterMatcher = Pattern
		.compile(String.format(
			FloraFileParser.PARAMETER_VALUES_OF_PARAMETER,
			parentParameter, parameterValue))
		.matcher(flParser
			.getDomainContextModelContent());

	Matcher parentCoversMatcher = Pattern
		.compile(String.format(
			FloraFileParser.PARAMETER_VALUE_COVERS_RELATION,
			parentParameterValue,
			parameterValue))
		.matcher(flParser
			.getDomainContextModelContent());

	Assert.assertTrue(parentParameterMatcher.find());
	Assert.assertTrue(parentCoversMatcher.find());

	flParser.deleteParameterValue(parameterValue,
		parentParameterValue);

	parentParameterMatcher.reset(
		flParser.getDomainContextModelContent());
	parentCoversMatcher.reset(
		flParser.getDomainContextModelContent());

	Assert.assertFalse(parentParameterMatcher.find());
	Assert.assertFalse(parentCoversMatcher.find());
    }

    @Test
    public void deleteParameterValueFromFile() {
	String parameterToDelete = "landplane";
	String parentParameterValue = "aircraft";

	Matcher parameterMatcher = Pattern
		.compile(String.format(
			FloraFileParser.PARAMETER_VALUES_OF_PARAMETER,
			"Interest", parameterToDelete))
		.matcher(flParser
			.getDomainContextModelContent());

	Matcher parameterValueMatcher = Pattern
		.compile(String.format(
			FloraFileParser.PARAMETER_VALUE_COVERS_RELATION,
			parentParameterValue,
			parameterToDelete))
		.matcher(flParser
			.getDomainContextModelContent());

	Assert.assertTrue(parameterValueMatcher.find());
	Assert.assertTrue(parameterMatcher.find());

	flParser.deleteParameterValue(parameterToDelete,
		"aircraft");

	parameterMatcher.reset(
		flParser.getDomainContextModelContent());
	parameterValueMatcher.reset(
		flParser.getDomainContextModelContent());

	Assert.assertFalse(parameterValueMatcher.find());
	Assert.assertFalse(parameterMatcher.find());

	flParser.writeNewParameterValueToFile(
		parameterToDelete, "Interest", "aircraft");

	parameterMatcher.reset(
		flParser.getDomainContextModelContent());
	parameterValueMatcher.reset(
		flParser.getDomainContextModelContent());

	Assert.assertTrue(parameterValueMatcher.find());
	Assert.assertTrue(parameterMatcher.find());
    }

    @Test
    public void addAndDeleteNewParameterToContextModel() {
	String newParameter = "Wheater";
	String newRootParameterValue = "allWheaterConditions";

	Assert.assertFalse(
		flParser.getDomainContextModelContent()
			.contains(newParameter));
	Assert.assertFalse(
		flParser.getDomainContextModelContent()
			.contains(newRootParameterValue));

	flParser.addNewParameterToContextModel(newParameter,
		newRootParameterValue);

	Matcher parameterValuesMatcher = Pattern
		.compile(String.format(
			FloraFileParser.PARAMETER_VALUES_OF_PARAMETER,
			newParameter,
			newRootParameterValue))
		.matcher(flParser
			.getDomainContextModelContent());

	Matcher parameterMatcher = Pattern
		.compile(String.format(
			FloraFileParser.PARAMETER,
			newParameter))
		.matcher(flParser
			.getDomainContextModelContent());

	Matcher contextModelParameterMatcher = Pattern
		.compile(String.format(
			FloraFileParser.CONTEXT_MODEL_PATTERN,
			newParameter))
		.matcher(flParser
			.getDomainContextModelContent());

	Assert.assertTrue(parameterValuesMatcher.find());
	Assert.assertTrue(parameterMatcher.find());
	Assert.assertTrue(
		contextModelParameterMatcher.find());

	flParser.deleteParameterValue(newRootParameterValue,
		"");
	flParser.deleteParameterFromContextModel(
		newParameter);

	parameterMatcher.reset(
		flParser.getDomainContextModelContent());
	parameterValuesMatcher.reset(
		flParser.getDomainContextModelContent());
	contextModelParameterMatcher.reset(
		flParser.getDomainContextModelContent());

	Assert.assertFalse(parameterValuesMatcher.find());
	Assert.assertFalse(parameterMatcher.find());
	Assert.assertFalse(
		contextModelParameterMatcher.find());
    }
}
