package dke.cbrm.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * General wrapper for the FLora2 command line interface
 * 
 * @author fburgstaller
 */
@Service
public class Flora2CLI {

    private boolean debug = true;

    /**
     * object for communicating with flora2 shell
     */
    private Process process;

    /**
     * Output stream from flora2 shell
     */
    private InputStream is;

    /**
     * Input stream to flora2 shell
     */
    private PrintWriter pw;

    private final String INPUT_REQUEST = "flora2 ?-";
    private final Pattern REGEX_VAR = Pattern
	    .compile("(?<=\\?\\w{1,20}\\s=\\s).*");

    /**
     * Flora2 located at /opt/Flora-2/flora2/runflora
     * 
     * @throws IOException
     */
    @Autowired
    public Flora2CLI(
	    @Value("${cbrm.flora.run.path}") String floraRunPath)
	    throws IOException {
	ProcessBuilder builder = new ProcessBuilder(floraRunPath);

	builder.redirectErrorStream(true);

	process = builder.start();
	is = process.getInputStream();
	pw = new PrintWriter(process.getOutputStream());

	getOutput();
    }

    /**
     * Get Output from FLora2 console. Reads until input request is found.
     * 
     * @return Output as text
     * @throws IOException
     */
    private String getOutput() throws IOException {
	StringBuilder output = new StringBuilder(600);
	StringBuilder line = new StringBuilder(80);
	int i = -1;
	while ((i = is.read()) != -1) {

	    if (i != '\n') {
		if (INPUT_REQUEST.equals(line.toString()))
		    break;
		line.append((char) i);
	    } else {
		if (line.length() != 0
			&& !line.toString().matches("\\d+ solution.*")
			&& !line.toString().matches("^Times.*")) {
		    output.append(line);
		    output.append("\n");
		}
		line.delete(0, line.length());
	    }
	}
	return output.toString();
    }

    /**
     * Passes the cmd to the flora2 shell
     * 
     * @param cmd
     * @return the output of the command
     * @throws IOException
     */
    public String issueCommand(String cmd) throws IOException {
	pw.println(cmd);
	pw.flush();
	String result = getOutput();
	if (debug)
	    System.out.println(result);
	return result;
    }

    /**
     * Load a flora2 file to the specified module
     * 
     * @param f
     * @param module
     * @return whether it worked or not
     * @throws IOException
     */
    public boolean loadFile(String f, String module)
	    throws IOException {
	String cmd = "['" + f + "'>>" + module + "].";
	return issueCommand(cmd).contains("Yes");
    }

    /**
     * Load a flora2 file to the main module
     * 
     * @param f
     * @return whether it worked or not
     * @throws IOException
     */
    public boolean loadFile(String f) throws IOException {
	String cmd = "['" + f + "'].";
	return issueCommand(cmd).contains("Yes");
    }

    /**
     * Parses a query result containing cnt variables
     * 
     * @param result
     * @param varCnt
     * @return
     */
    protected List<String[]> parseMultipleVars(String result,
	    int varCnt) {
	List<String[]> parsed = new LinkedList<String[]>();

	String[] lines = result.split("\n");
	int i = 0;
	String[] set = new String[varCnt];
	for (String line : lines) {
	    if (!line.matches("Yes")) {
		if (i == varCnt) {
		    i = 0;
		    parsed.add(set);
		    set = new String[varCnt];
		}

		Matcher matcher = REGEX_VAR.matcher(line);
		if (matcher.find())
		    set[i++] = matcher.group();
	    } else if (i == varCnt) {
		parsed.add(set);
	    }
	}
	return parsed;
    }

    /**
     * Parses a query result with a single variable
     * 
     * @param result
     * @return
     */
    protected List<String> parseSingleVar(String result) {
	List<String> parsed = new LinkedList<String>();

	String[] lines = result.split("\n");
	for (String line : lines) {
	    if (!line.matches("Yes")) {
		Matcher matcher = REGEX_VAR.matcher(line);
		if (matcher.find())
		    parsed.add(matcher.group());
	    }
	}
	return parsed;
    }

    /**
     * Whether the flora2 shell output is redirected to the console or not
     * 
     * @param debug
     */
    public void setDebug(boolean debug) {
	this.debug = debug;
    }

    /**
     * Closes the Flora2 shell
     * 
     * @return
     * @throws IOException
     */
    public boolean close() throws IOException {
	issueCommand("\\halt.");
	if (getOutput().startsWith("\nEnd XSB"))
	    return true;
	return false;
    }
}
