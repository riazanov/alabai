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


import java.util.LinkedList;

import java.io.*;

import logic.is.power.cushion.*;

import logic.is.power.logic_warehouse.*;

import logic.is.power.tptp_parser.*;



/**
 * This class is a wrapper for {@link tptp_parser.TptpParser}.
 * Given an {@link logic_warehouse_je.Input} object and a file name 
 * an object of this class parses the file and returns 
 * {@link logic_warehouse_je.Input.Formula}
 * and <code>LinkedList<{@link logic_warehouse_je.Input.Literal}></code> objects.
 * 
 * The class implementes lazy evaluation.
 */
class ParserOutputToInput {

    public enum InputKind {
	FORMULA_AXIOM,
	FORMULA_HYPOTHESIS,
	FORMULA_NEGATED_CONJECTURE,
	CLAUSE_AXIOM,
	CLAUSE_HYPOTHESIS,
	CLAUSE_NEGATED_CONJECTURE,
	END_OF_FILE,
	ERROR_IN_INPUT
    }; 
    
    /** @param intermediateRepresentation is used to create formula and
     * clauses generated by send function.
     * @param fileName is a file name to be parsed.
     * @param includeDir is a path to look for include files. If it is null,
     *        include directives are silently ignored.
     */ 
    public ParserOutputToInput(Input intermediateRepresentation,
			       String fileName,
			       String includeDir,
			       boolean printInfo)  
	throws java.io.IOException {
	_irf = intermediateRepresentation;
	_outputRepresentation = new SimpleTptpParserOutput();
	_includeDir = includeDir;
	_parserStack = new LinkedList<Container>();
	_errorMessage = null;
	_printInfo = printInfo;
	openNewFile(fileName); 
    }


    
    /** This function returns parsed expressions one by one (through
     *  the reference parameters). When function returns, only one of 
     *  the parameters may be set to not-null value. enum InputKind is 
     *  used to distinguish which role an input expression has and which 
     *  of the parameter (if any) is set up. The content of other  parameter 
     *  is set to null.
     *  In the case of an error (<code>ERROR_IN_INPUT</code> is returned)
     *  {@link #getErrorMessage()} can be used to get the corresponding error message.
     *  After an error happens it is impossible to parser anything else.
     */
    public InputKind send(Ref<InputSyntax.Formula> formula,
			  Ref<LinkedList<InputSyntax.Literal>> clause)
	throws java.io.IOException  {

	formula.content = null;
	clause.content = null;

	/* there has been an error before */
	if (_errorMessage != null) return InputKind.ERROR_IN_INPUT;
  
	/* the file has been parser and closed already */
	if (_parserStack.isEmpty()) return InputKind.END_OF_FILE;
  
	try {
	    TptpParser parser = _parserStack.getFirst().parser;
    
	    /* parse a top level construct */
	    TptpParserOutput.TptpInput in = 
		parser.topLevelItem(_outputRepresentation);
    
	    /* check whether this is the end of the file */
	    if (in == null) {
		/* close the current file and continue parsing the previous one */
		closeFile(true);
		return send(formula, clause);
	    }
    
	    /* otherwise process the parsed top level item */
	    SimpleTptpParserOutput.TopLevelItem item 
		= (SimpleTptpParserOutput.TopLevelItem)in;
    
	    /* process include directive */
	    switch (item.getKind()) {
		// ------------  INCLUDE  -------------------
		case Include:
		    if (_includeDir == null) { 
			/* just ignore the directive */
		    }
		    else {
			/* this limit on number of nested includes catches recursive includes */
			if (_parserStack.size() >= 1024) {
			    _errorMessage = 
				"Too many nested include directives " +
				"(depth > 1024) in " + _parserStack.getFirst().fileName +
				", line " + item.getLineNumber() + ".";
			    closeFile(false);
			    return InputKind.ERROR_IN_INPUT;
			}
        
			/* create the name of a new file to open */
			String newFile = 
			    ((SimpleTptpParserOutput.IncludeDirective)item).getFileName();
			/* Remove quotes from the file name and add the input directory */
			newFile = 
			    _includeDir + "/" + 
			    newFile.substring(1, newFile.length() - 1);

			if (_printInfo)
			    System.out.println("%    Including from: " + newFile);


			if (!openNewFile(newFile)) return InputKind.ERROR_IN_INPUT;
		    };
		    /* parse another top level construct */
		    return send(formula, clause);
      
		    // ------------  CLAUSE  -------------------
		case Clause: {
		    SimpleTptpParserOutput.AnnotatedClause annClause = 
			(SimpleTptpParserOutput.AnnotatedClause)item;
		    TptpParserOutput.FormulaRole role = annClause.getRole();
		    SimpleTptpParserOutput.Clause cnf = annClause.getClause();
      
		    clause.content = convertToLW(cnf);
      
		    switch(role) {
			/* Axiom group */
			case Axiom:
			case Definition: 
			case Lemma:
			case Theorem:
			case Plain:
			case FiDomain:
			case FiFunctors:
			case FiPredicates:
			    return InputKind.CLAUSE_AXIOM;
    
			    /* Hypothesis group */
			case Hypothesis:
			    return InputKind.CLAUSE_HYPOTHESIS;
    
			    /* Negated Conjecture group */
			case NegatedConjecture:
			    return InputKind.CLAUSE_NEGATED_CONJECTURE;
    
			    /* A clause cannot be negated => it is an error */
			case Conjecture: {
			    _errorMessage = 
				"file " + _parserStack.getFirst().fileName + ", line " +
				 item.getLineNumber() +
				 ": an input CNF formula with role \"conjecture\" "+
				"cannot be negated.";
			    return InputKind.ERROR_IN_INPUT;
			}
    
			    /* error */
			case Unknown: {
			    _errorMessage = 
				"file " + _parserStack.getFirst().fileName + 
				", line " + item.getLineNumber() +
				 ": an input CNF formula has role \"unknown\".";
			    return InputKind.ERROR_IN_INPUT;
			}
		    } /* switch (role) */
		    assert false;
		} /* case CnfFormula */
      
		    // ------------  FOF FORMULA  -------------------
		case Formula: {
		    SimpleTptpParserOutput.AnnotatedFormula annFof = 
			(SimpleTptpParserOutput.AnnotatedFormula)item;
		    TptpParserOutput.FormulaRole role = annFof.getRole();
		    SimpleTptpParserOutput.Formula fof = annFof.getFormula();

		    formula.content = convertToLW(fof);
      
		    switch(role) {
			/* Axiom group */
			case Axiom:
			case Definition: 
			case Lemma:
			case Theorem:
			case Plain:
			case FiDomain:
			case FiFunctors:
			case FiPredicates:
			    return InputKind.FORMULA_AXIOM;
  
			    /* Hypothesis group */
			case Hypothesis:
			    return InputKind.FORMULA_HYPOTHESIS;
  
			    /* Negated Conjecture group */
			case NegatedConjecture:
			    return InputKind.FORMULA_NEGATED_CONJECTURE;
  
			    /* Negate the conjecture */
			case Conjecture:
			    formula.content = 
				_irf.createNegatedFormula(formula.content);
			    return InputKind.FORMULA_NEGATED_CONJECTURE;
  
			    /* error */
			case Unknown: {
			    _errorMessage = 
				"file " + _parserStack.getFirst().fileName + 
				", line " + item.getLineNumber() +
				": an input FOF formula has role \"unknown\".";
			    return InputKind.ERROR_IN_INPUT;
			}
		    }; /* switch (role) */
		} /* case FofFormula */
    
		default:
		    assert false;
		    return InputKind.ERROR_IN_INPUT;
	    } /* switch */
    
    
	}
	// general ANTLR exception. It is enough to catch all ANTRL exceptions
	catch(antlr.ANTLRException e) {
	    _errorMessage = "ERROR during parsing \"" + _parserStack.getFirst().fileName
		+ "\": " + e;
	    closeFile(false);
	    return InputKind.ERROR_IN_INPUT;
	}

    } // send(Ref<InputSyntax.Formula> formula,..)


    
    /** in the case of an error this function returns the error message */
    public String getErrorMessage() { return _errorMessage; }
    





    //                 Private types:

    /** this is just a container to keep info about parser in one place */ 
    private static class Container {
	String fileName;
	FileInputStream in;
	TptpLexer lexer;
	TptpParser parser;
    }; // class Container
    

    //                 Private methods:

    
    /** add one more file to the stack of opened files.
     * @return true if successful and false otherwise.
     */
    private boolean openNewFile(String fileName) throws java.io.IOException {

	FileInputStream in;

	try
	{
	    in = new FileInputStream(fileName);
	}
	catch (java.io.FileNotFoundException e)
	{
	    _errorMessage = "Cannot open an input file \"" + fileName + "\"\n";
	    closeFile(false);
	    return false;
	};

	TptpLexer lexer = new TptpLexer(new DataInputStream(in));
	TptpParser parser = new TptpParser(lexer);
  
	parser.setFilename(fileName);
  
	/* remember the created objects */
	Container cont = new Container();
	cont.fileName = fileName;
	cont.in = in;
	cont.lexer = lexer;
	cont.parser = parser;
	_parserStack.addFirst(cont);
  
	return true;

    } // openNewFile(String fileName)

    /** Used to clean internal data, ie close input file, delete lexer
     * and parser, etc. 
     * @param onlyOne if it is true only one level of the stacks is removed,
     * i.e. only the last opened file is closed. If the parameter is false then
     * all the files are closed (this mode is used in the case of an error,
     * for example).
     */
    private void closeFile(boolean onlyOne) throws java.io.IOException {

	/* if one level has to be removed then stack cannot be empty */
	assert !(onlyOne && _parserStack.isEmpty());
	
	/* clean stack while there is something to clean */
	while (!_parserStack.isEmpty()) {
	    Container cont = _parserStack.getFirst();
	    _parserStack.removeFirst();
	    cont.in.close();
	    
	    if (onlyOne) break; /* only one level has to be removed */
	}
	

    } // closeFile(boolean onlyOne)
  


    /** converts a given {@link tptp_parser#SimpleTptptParserOutput#Formula}
     *  to a {@link logic_warehouse_je#Input#Formula}. 
     */
    private Input.Formula convertToLW(SimpleTptpParserOutput.Formula fof) {

	switch (fof.getKind()) {
	    case Atomic: { //============== 
      
		SimpleTptpParserOutput.Formula.Atomic atomic 
		    = (SimpleTptpParserOutput.Formula.Atomic)fof;
      
		boolean equality = atomic.getPredicate().equals("=");

		String negatedInfix = (equality)? "!=" : null;
		

		Input.Predicate p 
		    = _irf.createPredicate(atomic.getPredicate(),
					   atomic.getNumberOfArguments(),
					   equality,
					   negatedInfix);
		LinkedList<InputSyntax.Term> arguments 
		    = convertToLW(atomic.getArguments()); 
      
		return _irf.createAtomicFormula(p, arguments);
	    } /* case AtomicFormula */
    
	    case Negation: { //============== 
      
		SimpleTptpParserOutput.Formula.Negation negation 
		    = (SimpleTptpParserOutput.Formula.Negation)fof;
        
		return _irf
		    .createNegatedFormula(convertToLW(negation.getArgument()));
	    } /* case NegationFormula */
    
	    case Binary: { //============== 
      
		SimpleTptpParserOutput.Formula.Binary binary = 
		    (SimpleTptpParserOutput.Formula.Binary)fof;
      
		Input.Formula lhs = convertToLW(binary.getLhs());
		Input.Formula rhs = convertToLW(binary.getRhs());
      
		TptpParserOutput.BinaryConnective outOp
		    = binary.getConnective();
        
		int binCon = -1000;
      
		switch(outOp) {
		    case And:
		    case Or: {
          
			int inOp 
			    = ((outOp == TptpParserOutput.BinaryConnective.And)? 
			       InputSyntax.AssociativeConnective.And 
			       : 
			       InputSyntax.AssociativeConnective.Or);
          
			LinkedList<InputSyntax.Formula> arguments 
			    = new LinkedList<InputSyntax.Formula>();
          
			arguments.addLast(lhs);
			arguments.addLast(rhs);
          
			return _irf.createAssociativeFormula(inOp, arguments);
		    }
      
		    case Equivalence: /* <=> */
			binCon = InputSyntax.BinaryConnective.Equivalent;
			break;
		    case Implication: /* => */
			binCon = InputSyntax.BinaryConnective.Implies;
			break;
		    case ReverseImplication: /* <= */
			binCon = InputSyntax.BinaryConnective.ReverseImplies;
			break;
		    case Disequivalence: /* <~> */
			binCon = InputSyntax.BinaryConnective.NotEquivalent;
			break;
		    case NotOr: /* ~| */
			binCon = InputSyntax.BinaryConnective.NotOr;
			break;
		    case NotAnd: /* ~& */
			binCon = InputSyntax.BinaryConnective.NotAnd;
			break;
          
		    default: assert false;
		}; /* switch */
      
		/* create a formula for not-associative binary connective */
		return _irf.createBinaryFormula(binCon, lhs, rhs);
      
	    } /* case BinaryFormula */ 
     
	    case Quantified: { //============== 
      
		SimpleTptpParserOutput.Formula.Quantified quanFof 
		    = (SimpleTptpParserOutput.Formula.Quantified)fof;
      
      
		int quant
		    = ((TptpParserOutput.Quantifier.ForAll == quanFof.getQuantifier())
		       ? InputSyntax.Quantifier.ForAll : InputSyntax.Quantifier.Exist);
      
		LinkedList<InputSyntax.Variable> varList = 
		    new LinkedList<InputSyntax.Variable>();
		varList.addLast(_irf.createVariable(quanFof.getVariable()));

		return _irf.createQuantifiedFormula(quant, varList,
						    convertToLW(quanFof.getMatrix()));
	    } /* case QuantifiedFormula */
    
	    default: assert false;
	}; /* switch */
	return null; 

    } // convertToLW(SimpleTptpParserOutput.FofFormula fof)


  
    /** converts a given list of 
     *  {@link tptp_parser#SimpleTptptParserOutput#Term} to a
     *  <code>LinkedList<{@link logic_warehouse_je#InputSyntax#Term}></code>. 
     */
    private 
    LinkedList<InputSyntax.Term> 
    convertToLW(Iterable<SimpleTptpParserOutput.Term> termList) {
       
	if (termList == null) return null;

	LinkedList<InputSyntax.Term> newList = 
	    new LinkedList<InputSyntax.Term>();
	for (SimpleTptpParserOutput.Term term : termList)
	    newList.addLast(convertToLW(term));
	return newList;
    } // convertToLW(LinkedList<SimpleTptpParserOutput.Term> termList)


    /** converts a given {@link tptp_parser#SimpleTptptParserOutput#Term} 
     *  to an {@link logic_warehouse_je#Input#Term}. 
     */  
    private Input.Term convertToLW(SimpleTptpParserOutput.Term term) {

	SimpleTptpParserOutput.Symbol symb = term.getTopSymbol();
   
	/* this is a variable */
	if (symb.isVariable()) {
	    assert 0 == term.getNumberOfArguments();
	    return _irf.createTerm(_irf.createVariable(symb.getText()));
	}

	/* this is a function or constant */
	int argNum = term.getNumberOfArguments();
	Input.FunctionalSymbol fun 
	    = _irf.createFunctionalSymbol(symb.getText(), argNum);

	/* this is a constant */
	if (0 == argNum) return _irf.createTerm(fun);
   
	/* this is a function */
	return _irf.createTerm(fun, convertToLW(term.getArguments()));


    } // convertToLW(SimpleTptpParserOutput.Term term)



    /** converts a given {@link tptp_parser#SimpleTptptParserOutput#Clause}
     *  to a
     *  <code>LinkedList<{@link logic_warehouse_je#InputSyntax#Literal}></code>. 
     */
    private LinkedList<InputSyntax.Literal> 
    convertToLW(SimpleTptpParserOutput.Clause cnf) {

	LinkedList<InputSyntax.Literal> newList = 
	    new LinkedList<InputSyntax.Literal>();

	for (SimpleTptpParserOutput.Literal lit : cnf.getLiterals())
	    newList.addLast(convertToLW(lit));

	return newList;
    } // convertToLW(SimpleTptpParserOutput.Clause cnf)


  
    /** converts a given {@link tptp_parser#SimpleTptptParserOutput#Literal}
     *  to an {@link logic_warehouse_je#Input#Literal}. 
     */
    private Input.Literal convertToLW(SimpleTptpParserOutput.Literal lit) {

	
	SimpleTptpParserOutput.Formula.Atomic atomic = lit.getAtom();
  
	boolean equality = atomic.getPredicate().equals("=");
	
	String negatedInfix = (equality)? "!=" : null;

	Input.Predicate p 
	    = _irf.createPredicate(atomic.getPredicate(),
				   atomic.getNumberOfArguments(),
				   equality,
				   negatedInfix);

	LinkedList<InputSyntax.Term> arguments 
	    = convertToLW(atomic.getArguments()); 

	return _irf.createLiteral(lit.isPositive(), p, arguments);
	

    } // convertToLW(SimpleTptpParserOutput.Literal lit)


    //                 Data:


    /** Input Represenation Factory to create classes related to 
     *  {@link logic_warehouse_je#Input}.
     */
    private Input _irf;
    
    /** a factory to create classes related to 
     *  {@link tptp_parser#SimpleTptpParserOutput}.
     */
    private SimpleTptpParserOutput _outputRepresentation;
    
    /** a path to look for include files */
    private String _includeDir; 

    /** stack of opened file and parsers (to deal with nested includes) */
    private LinkedList<Container> _parserStack;
    
    /** error message holder. it is null iff there is no error. */
    private String _errorMessage;

    private boolean _printInfo;
    
}; // class ParserOutputToInput
