package dke.cbrm.persistence.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class FloraFileParserChangeHierarchyOfParameterTest
	extends BaseFileParserTest {

    @Test
    public void testChangeOfHierarchyNewParentHadNoCoversEntry() {

	Matcher unspecifiedAircraftMatcher = Pattern
		.compile(String.format(
			FloraFileParser.PARAMETER_VALUE_COVERS_RELATION,
			"aircraft", "unspecifiedAircraft"))
		.matcher(flParser
			.getDomainContextModelContent());

	Assert.assertTrue(
		unspecifiedAircraftMatcher.find());

	flParser.changeHierarchyOfParamterValues(
		"unspecifiedAircraft", "obstruction",
		"aircraft");

	unspecifiedAircraftMatcher.reset(flParser.getDomainContextModelContent());
	
	Assert.assertFalse(
		unspecifiedAircraftMatcher.find());

	Matcher unspecifiedAircraftObstructionMatcher = Pattern
		.compile(String.format(
			FloraFileParser.PARAMETER_VALUE_COVERS_RELATION,
			"obstruction",
			"unspecifiedAircraft"))
		.matcher(flParser
			.getDomainContextModelContent());

	Assert.assertTrue(
		unspecifiedAircraftObstructionMatcher.find());

	flParser.changeHierarchyOfParamterValues(
		"unspecifiedAircraft", "aircraft",
		"obstruction");

	unspecifiedAircraftMatcher.reset(flParser.getDomainContextModelContent());
	unspecifiedAircraftObstructionMatcher.reset(flParser.getDomainContextModelContent());

	Assert.assertTrue(
		unspecifiedAircraftMatcher.find());
	Assert.assertFalse(
		unspecifiedAircraftObstructionMatcher.find());
    }
}
