/* Copyright (C) 2010 Alexandre Riazanov (Alexander Ryazanov)
 *
 * The copyright owner licenses this file to You under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package logic.is.power.alabai;



import com.martiansoftware.jsap.*;

import java.io.*;

import java.util.Date;

import java.util.Locale;

import java.util.LinkedList;

import java.util.TimerTask;

import java.text.DateFormat;

import logic.is.power.cushion.*;

import logic.is.power.logic_warehouse.*;



/** Interactive command-line shell of the Alabai prover. */
public class Shell implements sun.misc.SignalHandler {

    public static void main(String[] args) throws Exception, Throwable {

	Shell shellObject = new Shell(Thread.currentThread());

	shellObject.handleSignal("INT"); 
	shellObject.handleSignal("XCPU");

	_timer.reset();
	_timer.start();

	SwitcheableAbortFlag.makeCurrent(new SwitcheableAbortFlag());


	//                  Parse the command line:

	describeOptions();
	
	parseOptions(args);
	
	if (_commandLineParameters.help)
	{
	    System.out.println(help());
	    System.exit(1);
	};



	//            When and where the process is running:


	if (_commandLineParameters.printInfo != null &&
	    _commandLineParameters.printInfo)
	    {
		Date startTime = new Date();
		
		String host;
		
		try 
		    {
			host = java.net.InetAddress.getLocalHost().toString();
		    }
		catch (java.net.UnknownHostException ex)
		    {
			host = "unknown";
		    };
		
		String workDir = System.getProperty("user.dir");
		
		String command = "";
		for (int n = 0; n < args.length; ++n)
		    command += args[n] + " ";
		
		
		
		DateFormat dateFormat = 
		    DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
						   DateFormat.LONG,
						   Locale.CANADA_FRENCH);
		
		
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println("% Alabai JE (" + _version + ")");
		System.out.println("% Started:      " + dateFormat.format(startTime));
		System.out.println("% Host:         " + host);
		System.out.println("% Work. dir.:   " + workDir);
		System.out.println("% Arguments:    " + command);
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	    }; // if (_commandLineParameters.printInput)



	//                 Set the alarm:


	// Will expire after the time limit:
	java.util.Timer alarm = new java.util.Timer();

	class AlarmTask extends TimerTask
	{
	    public void run() {
		// Shut down the current kernel session, if any
		if (_kernelSession != null)
		    _kernelSession.abort();
		SwitcheableAbortFlag.current().set();
	    }
	};
		
	AlarmTask alarmTask = new AlarmTask();

	alarm.schedule(alarmTask,
		       _commandLineParameters.timeLimit * 1000); // milliseconds

	try 
	{

	    KernelOptions kernelOptions = new KernelOptions();


	    // Read the plug-ins:

	    MultiPlugIn allPlugIns = new MultiPlugIn();
	    
	    for (String plugInName : _commandLineParameters.plugIns)
		{
		    if (_commandLineParameters.printInfo != null &&
			_commandLineParameters.printInfo)
			System.out.println("% Loading plug-in " + plugInName);


		    PlugIn loadedPlugIn = null;

		    try
			{
			    Class loadedClass = 
				ClassLoader.
				getSystemClassLoader().
				loadClass(plugInName);
			    loadedPlugIn = (PlugIn)loadedClass.newInstance();
			}
		    catch (Exception ex) 
			{
			    
			    System.err.println("% Error: cannot load plug-in " +
					       plugInName);
			    System.err.println("% There may be a problem with reading the file or it contains\n" + 
					       "% something that is not a valid model of logic.is.power.alabai.PlugIn.");
			    
			    System.err.println("% <<ERROR>> " + _commandLineParameters.problemId);
			    System.exit(1);
			};
		      
		    allPlugIns.addLast(loadedPlugIn);

		}; // for (String plugInName : _commandLineParameters.plugIns)


	    // Adjust the values in  kernelOptions according
	    // to the results of config file interpretation.
	    
	    logic.is.power.alabai.xml_config.Parser configParser = 
		new logic.is.power.alabai.xml_config.Parser();

	    for (String configFileName : 
		     _commandLineParameters.configFiles)
		{
		    File configFile = new File(configFileName);
		    
		    if (!configFile.isFile() || !configFile.canRead())
			{
			    System.err.println("% Error: cannot read config file " + 
					       configFileName);
			    
			    System.err.println("% <<ERROR>> " + _commandLineParameters.problemId);
			    System.exit(1);
					       
			};

		    if (_commandLineParameters.printInfo != null &&
			_commandLineParameters.printInfo)
			System.out.println("% Reading configuration from file " +
					   configFileName);		    

		    configParser.parse(configFile,kernelOptions);

		}; // for (String configFileName : 

    
	    

	    // Adjust some values in  kernelOptions according
	    // to the command line settings (these override 
	    // the settings read from the config files).
  
	    if (_commandLineParameters.printInput != null)
		kernelOptions.printInput = 
		    _commandLineParameters.printInput.booleanValue();
	    if (_commandLineParameters.printNew != null)
		kernelOptions.printNew = 
		    _commandLineParameters.printNew.booleanValue();
	    if (_commandLineParameters.printKept != null)
		kernelOptions.printKept = 
		    _commandLineParameters.printKept.booleanValue();
	    if (_commandLineParameters.printDiscarded != null)
		kernelOptions.printDiscarded = 
		    _commandLineParameters.printDiscarded.booleanValue();
	    if (_commandLineParameters.printAssembled != null)
		kernelOptions.printAssembled = 
		    _commandLineParameters.printAssembled.booleanValue();
	    if (_commandLineParameters.printSelectedClauses != null)
		kernelOptions.printSelectedClauses = 
		    _commandLineParameters.printSelectedClauses.booleanValue();
	    if (_commandLineParameters.printActivatedClauses != null)
		kernelOptions.printActivatedClauses = 
		    _commandLineParameters.printActivatedClauses.booleanValue();
	    if (_commandLineParameters.markUnselectedLiterals != null)
		kernelOptions.markUnselectedLiterals = 
		    _commandLineParameters.markUnselectedLiterals.booleanValue();
	    if (_commandLineParameters.printPromoted != null)
		kernelOptions.printPromoted = 
		    _commandLineParameters.printPromoted.booleanValue();
	    if (_commandLineParameters.printSelectionUnits != null)
		kernelOptions.printSelectionUnits = 
		    _commandLineParameters.printSelectionUnits.booleanValue();
	    if (_commandLineParameters.printUnifiability != null)
		kernelOptions.printUnifiability = 
		    _commandLineParameters.printUnifiability.booleanValue();
	    if (_commandLineParameters.printPenaltyComputation != null)
		kernelOptions.printPenaltyComputation = 
		    _commandLineParameters.printPenaltyComputation.booleanValue();
	

	    
	    //        Resolve references to plug-ins:
	    
	    try
		{
		    PlugInReference.resolve(kernelOptions,allPlugIns);
		}
	    catch (PlugInReference.UnresolvedReferenceException ex) 
		{
		    System.err.println("% Error: " + ex);
		    
		    System.err.println("% <<ERROR>> " + _commandLineParameters.problemId);
		    System.exit(1);
		};


	    //      Initialise the kernel session:

	    TerminalClauseProcessor terminalClauseProcessor = 
		new TerminalClauseProcessor(_commandLineParameters.
					    maxNumOfTerminalClauses);

	    _kernelSession = new Kernel(kernelOptions,
					terminalClauseProcessor);
	    
	    // _kernelSession will be informed when the limit on the number
	    // of terminal clauses has been exceeded
	    terminalClauseProcessor.setKernel(_kernelSession);


	    try
		{

		    // Parse the input file in the TPTP format and submit the formulas
		    // to the kernel session after converting the formulas to 
		    // the logic.is.power.logic_warehouse.Input representation:

		    logic.is.power.logic_warehouse.Input intermediateRepresentation = 
			new logic.is.power.logic_warehouse.Input();

		    String includeDir = _commandLineParameters.includeDirectory;

		    String mainInputFileName = _commandLineParameters.mainInputFileName;
		    
		    if (_commandLineParameters.printInfo != null &&
			_commandLineParameters.printInfo)
			{
			    System.out.println("% Reading main input file: " +  mainInputFileName);
			    System.out.println("% Include directory: " +  includeDir);
			};

		    ParserOutputToInput parseInput = 
			new ParserOutputToInput(intermediateRepresentation, 
						mainInputFileName,
						includeDir,
						_commandLineParameters.printInfo != null &&
						_commandLineParameters.printInfo.booleanValue());
	
		    Ref<InputSyntax.Formula> formula =
			new Ref<InputSyntax.Formula>();
		    Ref<LinkedList<InputSyntax.Literal>> clause = 
			new Ref<LinkedList<InputSyntax.Literal>>();


		    for (ParserOutputToInput.InputKind out =
			     parseInput.send(formula, clause);
			 out != ParserOutputToInput.InputKind.END_OF_FILE;
			 out = parseInput.send(formula, clause))
			{
			    SwitcheableAbortFlag.current().check();

			    switch (out) {
			    case FORMULA_AXIOM:
				_kernelSession.addAxiom(formula.content);
				break;
			    case FORMULA_HYPOTHESIS:
				_kernelSession.addHypothesis(formula.content);
				break;    
			    case FORMULA_NEGATED_CONJECTURE:
				_kernelSession.addNegatedConjecture(formula.content);
				break;    
			    case CLAUSE_AXIOM:
				_kernelSession.addAxiom(clause.content);
				break;    
			    case CLAUSE_HYPOTHESIS:
				_kernelSession.addHypothesis(clause.content);
				break;    
			    case CLAUSE_NEGATED_CONJECTURE:
				_kernelSession.addNegatedConjecture(clause.content);
				break;    
			    case END_OF_FILE:
				assert false;
				break;
			    case ERROR_IN_INPUT:
				System.out.println("\nError during parsing: " +
						   parseInput.getErrorMessage());
				System.exit(1);
				break;    
			    }; /*switch */

			    SwitcheableAbortFlag.current().check();

			}; // for (ParserOutputToInput.InputKind out = ..

		    if (_commandLineParameters.printInfo != null &&
			_commandLineParameters.printInfo)
			System.out.println("% End of main input file " +  mainInputFileName);

		}
	    catch (SwitcheableAbortFlag.Exception e)
		{
		    alarm.cancel();	   
		    if (_commandLineParameters.printInfo != null &&
			_commandLineParameters.printInfo)
			{
			    System.out.println("% Input aborted, possibly on some limit.");
			    
			    System.out.println("% <<INPUT ABORTED>> " + _commandLineParameters.problemId);
			};
		    return;
		}
	    



	    // Input has been fully submitted. Start saturation.

	    // This requires no more input be submitted. However, this is optional.
	    _kernelSession.endOfInput(); 

	    Kernel.TerminationCode terminationCode;
	
	    try 
	    {
		do 
		{
		    if (_commandLineParameters.maxNumOfTerminalClauses >= 0 &&
			terminalClauseProcessor.totalNumberOfTerminalClauses() >=
			_commandLineParameters.maxNumOfTerminalClauses)
			{
			    _kernelSession.abort();
			}
		    else
			_kernelSession.doSomething();
		}
		while (!_kernelSession.terminated());

		terminationCode = _kernelSession.terminationCode();
	    }
	    catch (SwitcheableAbortFlag.Exception e)
	    {
		if (_commandLineParameters.printInfo != null &&
		    _commandLineParameters.printInfo)
		    {
			printStatistics();	
			System.out.println("% Saturation aborted, possibly on some limit.");
			System.out.println("% " + 
					   terminalClauseProcessor.totalNumberOfTerminalClauses() +
					   " terminal clause(s) = " + 
					   terminalClauseProcessor.numberOfContradictions() +
					   " contradiction(s) + " + 
					   terminalClauseProcessor.numberOfConditionalContradictions() +
					   " cond. contr. + " + 
					   terminalClauseProcessor.numberOfAnswers() +
					   " answer(s).");
		    };
				   
		alarm.cancel();

		if (_commandLineParameters.printInfo != null &&
		    _commandLineParameters.printInfo)
		    if (terminalClauseProcessor.totalNumberOfTerminalClauses() == 0)
			{
			    System.out.println("% <<ABORTED>> " + _commandLineParameters.problemId);
			}
		    else 
			{
			    System.out.println("% <<SUCCESS>> " + _commandLineParameters.problemId);
			};
		    
		
		return;
	    };

	    if (_commandLineParameters.printInfo != null &&
		_commandLineParameters.printInfo)
		{
		    if (terminationCode == Kernel.TerminationCode.Saturation)
			{
			    printStatistics();
			    System.out.println("% Saturation terminated " +
					       ((terminalClauseProcessor.totalNumberOfTerminalClauses() == 0)?
						"without terminal clauses "
						:
						"") +
					       "(possibly with an incomplete procedure).");
			    System.out.println("% " + 
					       terminalClauseProcessor.totalNumberOfTerminalClauses() +
					       " terminal clause(s) = " + 
					       terminalClauseProcessor.numberOfContradictions() +
					       " contradiction(s) + " + 
					       terminalClauseProcessor.numberOfConditionalContradictions() +
					       " cond. contr. + " + 
					       terminalClauseProcessor.numberOfAnswers() +
					       " answer(s).");
			    if (terminalClauseProcessor.totalNumberOfTerminalClauses() == 0)
				{
				    System.out.println("% <<TERMINATED>> " + _commandLineParameters.problemId);
				}
			    else 
				System.out.println("% <<SUCCESS>> " + _commandLineParameters.problemId);
       	    
			}
		    else if (terminationCode == Kernel.TerminationCode.Abortion) {
			printStatistics();
			System.out.println("% Saturation aborted, possibly on some limit.");
			System.out.println("% " + 
					   terminalClauseProcessor.totalNumberOfTerminalClauses() +
					   " terminal clause(s) = " + 
					   terminalClauseProcessor.numberOfContradictions() +
					   " contradiction(s) + " + 
					   terminalClauseProcessor.numberOfConditionalContradictions() +
					   " cond. contr. + " + 
					   terminalClauseProcessor.numberOfAnswers() +
					   " answer(s).");

			if (terminalClauseProcessor.totalNumberOfTerminalClauses() == 0)
			    {
				System.out.println("% <<ABORTED>> " + _commandLineParameters.problemId);
			    }
			else 
			    System.out.println("% <<SUCCESS>> " + _commandLineParameters.problemId);		    
			
		    };
		}; // if (_commandLineParameters.printInfo != null &&
	
	    alarm.cancel();
	}
	catch (Throwable e) 
	{
	    System.err.println("% <<ERROR>> " + _commandLineParameters.problemId);
	    System.err.println("% Error: Unexpected exception caught. ");
	    System.err.println("% Stopping the time limit thread.");
	    alarm.cancel();
	    System.err.println("% Rethrowing the exception:");
	    throw e;
	};

    } // main(String [] args)


    //            Methods prescribed by sun.misc.SignalHandler:

    /** Can be used to make the specified signal handled by this object;
     *  use the standard signal names without "SIG" at the start,
     *  ie, "INT", "XCPU", etc.
     */
    public void handleSignal(final String name) {

	sun.misc.Signal sig = new sun.misc.Signal(name);
	
	sun.misc.Signal.handle(sig,this);
    }

    /** This method is called by {@link sun.misc.Signal} to notify
     *  this object of the corresponding signal.
     */
    public void handle(final sun.misc.Signal sig) {
	
	if (sig.getName().equals("INT"))
	{
	    _mainThread.suspend();
	    printStatistics();
	    System.out.print("% (a)bort, (c)ontinue or (i)nteractive? ");
	    
	    int answer = 0;
 
	    while (true)
	    {
		try 
		{
		    answer = System.in.read();
		    
		    System.in.skip(System.in.available());

		    if (answer == 'a' || answer == 'A')
		    {
			System.out.println("\n% Alabai run aborted by the user.");
			
			System.exit(1);
		    }
		    else if (answer == 'c' || answer == 'C') 
		    {
			System.out.println("\n% Resuming Alabai run...");
			_mainThread.resume();
			return;
		    }
		    else if (answer == 'i' || answer == 'I') 
		    {
			System.out.println("\n% INTERACTIVE MODE WILL BE HERE LATER");
			_mainThread.resume();
			return;
		    }
		    else
			System.out.print("\n% (a)bort, (c)ontinue or (i)nteractive? ");
		    
		}
		catch (java.io.IOException ex) 
		{
		    System.out.print("\n% (a)bort, (c)ontinue or (i)nteractive? ");
		};
		
	    }
	}
	else if (sig.getName().equals("XCPU"))
	{
	    System.out.println("% Alabai run aborted by the user.");

	    System.exit(1);	    
	};
	
    } // handle(final sun.misc.Signal sig)





    private static void printStatistics() {

	if (_kernelSession == null) {
	    System.out.println("\n% No statistics available: no kernel session is currently active.");
	}
	else {
	    
	    System.out.println("\n%%%%%%%%%%%%%%%%%  Statistics: %%%%%%%%%%%%%%%%%%%%%%%");
	    System.out.println("%  CPU time " +
			       ((float)(_kernelSession.CPUTimeInMilliseconds()/10)/100) +
			       " sec");
	    System.out.print(_kernelSession.statistics().toString());
	    System.out.println("%%%%%%%%%%%%%%  End of statistics. %%%%%%%%%%%%%%%%%%%\n");
	    if (_commandLineParameters.printIndexPerformanceSummary != null &&
		_commandLineParameters.printIndexPerformanceSummary)
		{
		    System.out.println("%%%%%%%%%%%%%% Index performance summary: %%%%%%%%%%%%%");
		    System.out.println(_kernelSession.indexPerformanceSummary());
		    System.out.println("%%%%%%%%% End of index performance summary. %%%%%%%%");		    
		};
	};

    } // printStatistics()



    //                Private types:


    private static class CommandLineParameters {
	
	CommandLineParameters() {

	    // Set default values:
	    
	    help = false;
	    
	    configFiles = new String[0];
	    
	    plugIns = new String[0];

	    timeLimit = 1000000000;

	    maxNumOfTerminalClauses = -1;

	    mainInputFileName = "-";
	    
	    includeDirectory = ".";

	    
	    printInfo = new Boolean(true); // default
	    printIndexPerformanceSummary = null;
	    printAnswers = null;
	    answerFormat = null;
	    printProofs = null;
	    proofFormat = null;
	    printInput = null;
	    printNew = null;	
	    printKept = null;
	    printDiscarded = null;	
	    printAssembled = null;
	    printSelectedClauses = null;
	    printActivatedClauses = null;
	    markUnselectedLiterals = null;	
	    printPromoted = null;	
	    printSelectionUnits = null;	 
	    printUnifiability = null;	 
	    printPenaltyComputation = null;	 

	} // CommandLineParameters()

	public boolean help;
	public String problemId;
	public String[] configFiles;
	public String[] plugIns;
	public int timeLimit;
	public int maxNumOfTerminalClauses;
	public String mainInputFileName;
	public String includeDirectory;	
	public Boolean printInfo;
	public Boolean printIndexPerformanceSummary;
	public Boolean printAnswers;
	public String answerFormat;
	public Boolean printProofs;
	public String proofFormat;
	public Boolean printInput;
	public Boolean printNew;	
	public Boolean printKept;	
	public Boolean printDiscarded;	
	public Boolean printAssembled;
	public Boolean printSelectedClauses;
	public Boolean printActivatedClauses;
	public Boolean markUnselectedLiterals;	
	public Boolean printPromoted;
	public Boolean printSelectionUnits;
	public Boolean printUnifiability;
	public Boolean printPenaltyComputation;

    } // class CommandLineParameters

	

    /** Reports solutions. */
    private static class TerminalClauseProcessor 
	implements SimpleReceiver<Clause> {

	/** @param limit on the number of solutions that can be accepted;
	 *         negative if there is no limit
	 */
	public TerminalClauseProcessor(int limit) {
	    _numberOfContradictions = 0;
	    _numberOfConditionalContradictions = 0;
	    _numberOfAnswers = 0;
	    _limit = limit;
	    try {
		_answerXMLRenderer = 
		    new logic.is.power.alabai.schematic_answer_xml.Renderer();
	    } catch (java.lang.Exception ex) {
		throw new RuntimeException("Cannot initialise answer XML renderer: " + ex);
	    }
	}
	
	public void setKernel(Kernel kernel) {
	    _kernel = kernel;
	}

	/** @return false if the limit on the number of solutions to accept
	 *          has been exceeded
	 */
	public boolean receive(Clause val) {
	    
	    if (_limit == 0) 
		{
		    _kernel.abort();
		    return false;
		};
		    
	    if (_limit > 0) --_limit;

	    boolean printAnswers = 
		Shell._commandLineParameters.printAnswers != null &&
		Shell._commandLineParameters.printAnswers;
	    
	    String answerFormat = "native";
	    if (Shell._commandLineParameters.answerFormat != null)
		answerFormat = Shell._commandLineParameters.answerFormat;


	    if (val.isContradiction())
		{
		    if (printAnswers) 
			if (answerFormat.equals("native"))
			    {
				System.out.println("% Contradiction: " + val);
			    }
			else if (answerFormat.equals("xml"))
			    {
				System.out.println("<!-- Contradiction: " + val + " -->");
				try {
				    _answerXMLRenderer.render(val,System.out);
				} catch (java.lang.Exception ex) {
				    System.err.println("% Error: cannot render clause in XML: " + ex);
				};
				System.out.println("\n");
			    };
		    ++_numberOfContradictions; 
		}
	    else if (val.isConditionalContradiction())
		{
		    if (printAnswers) 
			if (answerFormat.equals("native"))
		 	    {
				System.out.println("% Cond. contr.: " + val);
			    }
			else if (answerFormat.equals("xml"))
			    {
				System.out.println("<!-- Cond. contr.: " + val + " -->");
				try {
				    _answerXMLRenderer.render(val,System.out);
				} catch (java.lang.Exception ex) {
				    System.err.println("% Error: cannot render clause in XML: " + ex);
				};
				System.out.println("\n");
			    };
		    ++_numberOfConditionalContradictions;
		}
	    else
		{
		    if (printAnswers)
			if (answerFormat.equals("native"))
			    {
				System.out.println("% Answer clause: " + val);
			    }
			else if (answerFormat.equals("xml"))
			    {
				System.out.println("<!-- Answer clause: " + val + " -->");
				try {
				    _answerXMLRenderer.render(val,System.out);
				} catch (java.lang.Exception ex) {
				    System.err.println("% Error: cannot render clause in XML: " + ex);
				};
				System.out.println("\n");
			    }; 
		    ++_numberOfAnswers;
		};

	    if (Shell._commandLineParameters.printProofs != null &&
		Shell._commandLineParameters.printProofs)
		{
		    System.out.println("% PROOF: ");

		    if (Shell._commandLineParameters.
			proofFormat.equalsIgnoreCase("tptp"))
			{
			    
			    for (Clause ancestor : val.ancestors())
				System.out.println(TPTPPrinter.print(ancestor));
			    System.out.println(TPTPPrinter.print(val));
			}
		    else
			{	
			    assert Shell._commandLineParameters.
				proofFormat.equalsIgnoreCase("native");

			    for (Clause ancestor : val.ancestors())
				{
				    System.out.println("%   " + ancestor); 
				};
			    System.out.println("%   " + val);
			};

		    System.out.println("% END OF PROOF. ");

		}; // if (Shell._commandLineParameters.printProofs != null &&

	    if (_limit == 0) _kernel.abort();

	    return true;

	} // receive(Clause val)
	

	/** @return numberOfContradictions() + numberOfAnswers() */
	public int totalNumberOfTerminalClauses() {
	    return numberOfContradictions() +
		numberOfConditionalContradictions() +
		numberOfAnswers();
	}

	public int numberOfContradictions() { 
	    return _numberOfContradictions;
	}

	public int numberOfConditionalContradictions() { 
	    return _numberOfConditionalContradictions;
	}

	public int numberOfAnswers() { 
	    return _numberOfAnswers;
	}

	
	private int _numberOfContradictions;

	private int _numberOfConditionalContradictions;
 
	private int _numberOfAnswers;
	
	/** Limit on the number of clauses to accept; negative if there is no limit. */
	private int _limit;

	private Kernel _kernel;

	private logic.is.power.alabai.schematic_answer_xml.Renderer _answerXMLRenderer;
	
    } // class TerminalClauseProcessor





    //                Private methods:

    private Shell(Thread mainThread) {
	_mainThread = mainThread;
    }

    private static String help() {
	
	String indent = "     ";

	String res = "Alabai (JE) command line usage:\n";
	
        res += indent + "alabai [-options] <main input file in TPTP format>\n" +  
	    "where options include:\n" +
	    indent + "--help                    print this help and exit\n" +
	    indent + "-h                        same as --help\n\n" +

	    
	    indent + "--id <problem id>         string to be used as the problem id\n" +
	    indent + "                          in grep-codes; default = main input file name\n" +
	    indent + "--config-file <file>      read configuration from <file>\n" +
	    indent + "--plug-in <class name>    read a compiled plug-in class with\n" +
	    indent + "                          the specified full name. The class must\n" + 
	    indent + "                          implement logic.is.power.alabai.PlugIn. It will be\n" + 
	    indent + "                          loaded with the system loader, so the convention\n" + 
	    indent + "                          for the class name is the same as for regular\n" +
	    indent + "                          dependency classes. In particular, the classpath\n" + 
	    indent + "                          may need to be adjusted\n" + 
	    indent + "--time-limit <sec>        time limit in seconds\n" +
	    indent + "-t <sec>                  same as --time-limit <sec>\n\n" +

	    indent + "--max-num-of-terminal-clauses <num>\n" +
	    indent + "                          limit on the total number of terminal\n" +
	    indent + "                          clauses of all kinds, such as contradiction,\n" +
	    indent + "                          conditional contradiction and answer clauses;\n" +
	    indent + "                          a negative value means there is no limit;\n" +
	    indent + "                          default = -1\n" +
	    indent + "--include-dir <path>      use <path> to look for files that have\n" +
	    indent + "                          to be included\n" +
	    indent + "-I <path>                 same as --include-dir <path>\n\n" +
	    

	    indent + "--print-info <flag>       print various kinds of info about\n" +
	    indent + "                          the session, such as statistics;\n" +
	    indent + "--print-index-performance-summary <flag>\n" +
	    indent + "                          print performance indicator values\n" +
	    indent + "                          for various indexes;\n" +

	    indent + "--print-answers <flag>    print answer clauses;\n" +
	    indent + "--answer-format <format>  format for printing answers.\n" +
	    indent + "                          Possible values are 'native' (default) and 'xml';\n" +
	    indent + "--print-proofs <flag>     print refutations/answer proofs;\n" +
	    indent + "--proof-format <format>   format for refutations/answer proofs.\n" +
	    indent + "                          Possible values are 'tptp' and 'native' (default);\n" +
	    indent + "--print-input <flag>      print input clauses;\n" +
	    indent + "                          0/1 or false/true can be used as flag values\n" +
	    indent + "--print-new <flag>        print freshly generated clauses\n" +
	    indent + "--print-kept <flag>       print retained clauses\n" +
	    indent + "--print-discarded <flag>  print discarded clauses\n" +
	    indent + "--print-assembled <flag>  print clauses assembled for long-term storage\n" +
	    indent + "--print-selected-clauses <flag>\n" + 
	    indent + "                          print clauses being selected for main inferences\n" +
	    indent + "--print-activated-clauses <flag>\n" + 
	    indent + "                          print clauses being activated for main inferences\n" +
	    indent + "                          by extracting their selection units\n" +
	    indent + "--mark-unselected-literals <flag>\n" +
	    indent + "                          mark unselected literals in printed selected clauses\n" +
	    indent + "--print-promoted <flag>   print fine selection units being promoted\n" +
	    indent + "                          for more inferences\n" +
	    indent + "--print-selection-units <flag>\n" +
	    indent + "                          print selection units freshly extracted\n" +
	    indent + "                          from the corresponding clauses\n" +
	    indent + "--print-unifiability <flag>\n" +
	    indent + "                          display term unifiability estimations\n" +
	    indent + "--print-penalty-computation <flag>\n" +
	    indent + "                          display selection unit penalty computation\n";
	    
	
	return res;

    } // help()

    private static void describeOptions() throws JSAPException {

	Switch helpSwitch = 
	    new Switch("help")
	    .setShortFlag('h')
	    .setLongFlag("help");

	_commandLineParser.registerParameter(helpSwitch);

	FlaggedOption configFileOption = 
	    new FlaggedOption("config-file") 
	    .setShortFlag('c')
	    .setLongFlag("config-file")         
	    .setStringParser(JSAP.STRING_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(configFileOption);

	FlaggedOption idOption = 
	    new FlaggedOption("id") 
	    .setLongFlag("id")         
	    .setStringParser(JSAP.STRING_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(idOption);

	FlaggedOption plugInOption = 
	    new FlaggedOption("plug-in") 
	    .setShortFlag('p')
	    .setLongFlag("plug-in")         
	    .setStringParser(JSAP.STRING_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(plugInOption);

	
	FlaggedOption timeLimitOption = 
	    new FlaggedOption("time-limit") 
	    .setShortFlag('t')
	    .setLongFlag("time-limit")         
	    .setStringParser(JSAP.INTEGER_PARSER)
	    .setDefault("1000000")
	    .setRequired(false); 

	_commandLineParser.registerParameter(timeLimitOption);

	
	FlaggedOption maxNumOfTerminalClausesOption = 
	    new FlaggedOption("max-num-of-terminal-clauses") 
	    .setLongFlag("max-num-of-terminal-clauses")         
	    .setStringParser(JSAP.INTEGER_PARSER)
	    .setDefault("-1")
	    .setRequired(false); 

	_commandLineParser.registerParameter(maxNumOfTerminalClausesOption);

	

	UnflaggedOption mainInputFileOption = 
	    new UnflaggedOption("main-input-file")
	    .setStringParser(JSAP.STRING_PARSER)
	    .setDefault("-")
	    .setRequired(true);

	_commandLineParser.registerParameter(mainInputFileOption);


	FlaggedOption includeDirectoryOption = 
	    new FlaggedOption("include-dir") 
	    .setShortFlag('I')
	    .setLongFlag("include-dir")         
	    .setStringParser(JSAP.STRING_PARSER)
	    .setDefault(".")
	    .setRequired(false); 

	_commandLineParser.registerParameter(includeDirectoryOption);

		
	FlaggedOption printInfoOption = 
	    new FlaggedOption("print-info") 
	    .setLongFlag("print-info")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(printInfoOption);


	FlaggedOption printIndexPerformanceSummaryOption = 
	    new FlaggedOption("print-index-performance-summary") 
	    .setLongFlag("print-index-performance-summary")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.
	    registerParameter(printIndexPerformanceSummaryOption);


	FlaggedOption printAnswersOption = 
	    new FlaggedOption("print-answers") 
	    .setLongFlag("print-answers")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(printAnswersOption);


	
	FlaggedOption answerFormatOption = 
	    new FlaggedOption("answer-format") 
	    .setLongFlag("answer-format")         
	    .setStringParser(JSAP.STRING_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(answerFormatOption);	



	FlaggedOption printProofsOption = 
	    new FlaggedOption("print-proofs") 
	    .setLongFlag("print-proofs")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(printProofsOption);



	
	FlaggedOption proofFormatOption = 
	    new FlaggedOption("proof-format") 
	    .setLongFlag("proof-format")         
	    .setStringParser(JSAP.STRING_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(proofFormatOption);




	
	FlaggedOption printInputOption = 
	    new FlaggedOption("print-input") 
	    .setLongFlag("print-input")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setDefault("false")
	    .setRequired(false); 

	_commandLineParser.registerParameter(printInputOption);

	
	
	FlaggedOption printNewOption = 
	    new FlaggedOption("print-new") 
	    .setLongFlag("print-new")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(printNewOption);

	
	
	FlaggedOption printKeptOption = 
	    new FlaggedOption("print-kept") 
	    .setLongFlag("print-kept")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(printKeptOption);


	FlaggedOption printDiscardedOption = 
	    new FlaggedOption("print-discarded") 
	    .setLongFlag("print-discarded")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(printDiscardedOption);

	
	
	FlaggedOption printAssembledOption = 
	    new FlaggedOption("print-assembled") 
	    .setLongFlag("print-assembled")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(printAssembledOption);

	
	
	FlaggedOption printSelectedClausesOption = 
	    new FlaggedOption("print-selected-clauses") 
	    .setLongFlag("print-selected-clauses")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(printSelectedClausesOption);

	
	FlaggedOption printActivatedClausesOption = 
	    new FlaggedOption("print-activated-clauses") 
	    .setLongFlag("print-activated-clauses")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(printActivatedClausesOption);

	
	
	FlaggedOption markUnselectedLiteralsOption = 
	    new FlaggedOption("mark-unselected-literals") 
	    .setLongFlag("mark-unselected-literals")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(markUnselectedLiteralsOption);



	FlaggedOption printPromotedOption = 
	    new FlaggedOption("print-promoted") 
	    .setLongFlag("print-promoted")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(printPromotedOption);

	FlaggedOption printSelectionUnitsOption = 
	    new FlaggedOption("print-selection-units") 
	    .setLongFlag("print-selection-units")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(printSelectionUnitsOption);

	FlaggedOption printUnifiabilityOption = 
	    new FlaggedOption("print-unifiability") 
	    .setLongFlag("print-unifiability")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(printUnifiabilityOption);

	
	FlaggedOption printPenaltyComputationOption = 
	    new FlaggedOption("print-penalty-computation") 
	    .setLongFlag("print-penalty-computation")         
	    .setStringParser(JSAP.BOOLEAN_PARSER)
	    .setRequired(false); 

	_commandLineParser.registerParameter(printPenaltyComputationOption);

	

    } // describeOptions()




    private static void parseOptions(String[] args) throws Exception {

	JSAPResult parameters = _commandLineParser.parse(args); 

	_commandLineParameters.help = 
	    parameters.getBoolean("help");
	    
	_commandLineParameters.configFiles =
	    parameters.getStringArray("config-file");

	_commandLineParameters.plugIns =
	    parameters.getStringArray("plug-in");

	_commandLineParameters.timeLimit = 
	    parameters.getInt("time-limit");

	_commandLineParameters.maxNumOfTerminalClauses = 
	    parameters.getInt("max-num-of-terminal-clauses");
	    
	_commandLineParameters.mainInputFileName =  
	    parameters.getString("main-input-file");

	if (parameters.contains("id"))
	    {
		_commandLineParameters.problemId =
		    parameters.getString("id");
	    }
	else
	    _commandLineParameters.problemId = 
		_commandLineParameters.mainInputFileName; // default

	_commandLineParameters.includeDirectory =
	    parameters.getString("include-dir");
	
	if (parameters.contains("print-info"))
	    {
		_commandLineParameters.printInfo = 
		    parameters.getBoolean("print-info");
	    }
	else
	    _commandLineParameters.printInfo = new Boolean(true); // default

	if (parameters.contains("print-index-performance-summary"))
	    {
		_commandLineParameters.printIndexPerformanceSummary = 
		    parameters.getBoolean("print-index-performance-summary");
	    };

	if (parameters.contains("print-answers"))
	    _commandLineParameters.printAnswers = 
		parameters.getBoolean("print-answers");


	_commandLineParameters.answerFormat = "native"; // default
	if (parameters.contains("answer-format"))
	    {
		String val = 
		    parameters.getString("answer-format");
		 
		if (!val.equalsIgnoreCase("native") &&
		    !val.equalsIgnoreCase("xml")) 
		    throw new Error("Bad value in --answer-format : must be 'native' or 'xml'.");
		    
		_commandLineParameters.answerFormat = val;
	    };


	if (parameters.contains("print-proofs"))
	    _commandLineParameters.printProofs = 
		parameters.getBoolean("print-proofs");
		
	_commandLineParameters.proofFormat = "native"; // default
	if (parameters.contains("proof-format"))
	    {
		String val = 
		    parameters.getString("proof-format");
		 
		if (!val.equalsIgnoreCase("tptp") &&
		    !val.equalsIgnoreCase("native")) 
		    throw new Error("Bad value in --proof-format : must be 'tptp' or 'native'.");
		    
		
		_commandLineParameters.proofFormat = val;
	    };


	if (parameters.contains("print-input"))
	    _commandLineParameters.printInput = 
		parameters.getBoolean("print-input");
	    
	if (parameters.contains("print-new"))
	    _commandLineParameters.printNew = 
		parameters.getBoolean("print-new");

	if (parameters.contains("print-kept"))
	    _commandLineParameters.printKept = 
		parameters.getBoolean("print-kept");

	if (parameters.contains("print-discarded"))
	    _commandLineParameters.printDiscarded = 
		parameters.getBoolean("print-discarded");

	if (parameters.contains("print-assembled"))
	    _commandLineParameters.printAssembled = 
		parameters.getBoolean("print-assembled");

	if (parameters.contains("print-selected-clauses"))
	    _commandLineParameters.printSelectedClauses = 
		parameters.getBoolean("print-selected-clauses");

	if (parameters.contains("print-activated-clauses"))
	    _commandLineParameters.printActivatedClauses = 
		parameters.getBoolean("print-activated-clauses");

	if (parameters.contains("mark-unselected-literals"))
	    _commandLineParameters.markUnselectedLiterals = 
		parameters.getBoolean("mark-unselected-literals");

	if (parameters.contains("print-promoted"))
	    _commandLineParameters.printPromoted = 
		parameters.getBoolean("print-promoted");

	if (parameters.contains("print-selection-units"))
	    _commandLineParameters.printSelectionUnits = 
		parameters.getBoolean("print-selection-units");

	if (parameters.contains("print-unifiability"))
	    _commandLineParameters.printUnifiability = 
		parameters.getBoolean("print-unifiability");

	if (parameters.contains("print-penalty-computation"))
	    _commandLineParameters.printPenaltyComputation = 
		parameters.getBoolean("print-penalty-computation");


    } // parseOptions(String [] args)


    

    //                Data:

    private final Thread _mainThread;

    private static String _version = "no version yet";

    private static Timer _timer = new Timer();

    /** Command-line options parser. */
    private static JSAP _commandLineParser = new JSAP();

    private static CommandLineParameters _commandLineParameters = 
    new CommandLineParameters();

    private static Kernel _kernelSession = null;
    

}; // class Shell
