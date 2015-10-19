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



import java.util.*;

import logic.is.power.cushion.*;

import logic.is.power.logic_warehouse.*;

import logic.is.power.tptp_parser.*;


/** Provides a static method for printing clauses as TPTP derivation elements. 
 *  TODO: Finer clause role detection.
 */ 
public class TPTPPrinter {
    
    public static String print(Clause cl) {
	
	for (Literal lit : cl.literals())
	    if (lit.isGeneral())
		{
		    SimpleTptpParserOutput.AnnotatedFormula form = 
			convertToAnnotatedFormula(cl);
		    
		    return form.toString();
		};
	SimpleTptpParserOutput.AnnotatedClause clause = 
	    convertToAnnotatedClause(cl);
	
	return clause.toString();
	
    } // print(Clause cl)


    private 
	static
	SimpleTptpParserOutput.AnnotatedFormula 
	convertToAnnotatedFormula(Clause cl) {


	TptpParserOutput.FormulaRole role = FOFRole(cl);

	TptpParserOutput.FofFormula body = FOFBody(cl);

	TptpParserOutput.Annotations annotations = 
	    annotations(cl);

	return
	    (SimpleTptpParserOutput.AnnotatedFormula)
	    _factory.createFofAnnotated(Long.toString(cl.number()),
					role,
					body,
					annotations,
					-1); 

	// I don't remember why createFofAnnotated() returns 
	// TptpParserOutput.TptpInput rather than
	// specifically SimpleTptpParserOutput.AnnotatedFormula .
	//                               Alexandre Riazanov


    } // convertToAnnotatedFormula(Clause cl) 
	

    private 
	static
	SimpleTptpParserOutput.AnnotatedClause 
	convertToAnnotatedClause(Clause cl) {

	TptpParserOutput.FormulaRole role = CNFRole(cl);

	TptpParserOutput.CnfFormula body = 
	    CNFBody(cl);

	TptpParserOutput.Annotations annotations = 
	    annotations(cl);

	return
	    (SimpleTptpParserOutput.AnnotatedClause)
	    _factory.createCnfAnnotated(Long.toString(cl.number()),
					role,
					body,
					annotations,
					-1); 

	// I don't remember why createCnfAnnotated() returns 
	// TptpParserOutput.TptpInput rather than
	// specifically SimpleTptpParserOutput.AnnotatedClause .
	//                               Alexandre Riazanov

    } // convertToAnnotatedClause(Clause cl)
	
    

    private static TptpParserOutput.FormulaRole FOFRole(Clause cl) {

	if (cl.isInput())
	    {
		if (cl.isDerivedExclusivelyFrom(Clause.Role.Axiom))
		    return TptpParserOutput.FormulaRole.Axiom;

		if (cl.isDerivedExclusivelyFrom(Clause.Role.Hypothesis))
		    return TptpParserOutput.FormulaRole.Hypothesis;
		
		if (cl.isDerivedExclusivelyFrom(Clause.Role.NegatedConjecture))
		    return TptpParserOutput.FormulaRole.NegatedConjecture;
		
		// TODO: This has to be refined at least for 
		// TptpParserOutput.FormulaRole.Conjecture

		return TptpParserOutput.FormulaRole.Plain;		
	    }
	else // !cl.isInput()
	    {
		// TODO: This has to be refined at least for 
		// TptpParserOutput.FormulaRole.NegatedConjecture
		
		return TptpParserOutput.FormulaRole.Plain;
	    }

    } // FOFRole(Clause cl)



    private 
	static
	TptpParserOutput.FofFormula FOFBody(Clause cl) {
	
	if (cl.literals().isEmpty()) 
	    return _factory.atomAsFormula(_factory.builtInFalse());
	
	Iterator<Literal> iter = cl.literals().iterator();
	
	TptpParserOutput.FofFormula result = 
	    convertLiteralToFormula(iter.next());

	while (iter.hasNext())
	    result = 
		_factory.
		createBinaryFormula(result,
				    TptpParserOutput.BinaryConnective.Or,
				    convertLiteralToFormula(iter.next()));


	// Apply the universal quantifier to the free variables:

	HashSet<Variable> freeVars = new HashSet<Variable>();
	
	cl.collectFreeVariables(freeVars);

	if (!freeVars.isEmpty())
	    {
		LinkedList<String> freeVarNames = new LinkedList<String>();
		
		for (Variable var : freeVars)  
		    freeVarNames.addLast(var.name());
		
		result = 
		    _factory.
		    createQuantifiedFormula(TptpParserOutput.Quantifier.ForAll,
					    freeVarNames,
					    result);
	    };
	
	return result;

    } // FOFBody(Clause cl)



    private static TptpParserOutput.FormulaRole CNFRole(Clause cl) {
	
	if (cl.isInput())
	    {
		if (cl.isDerivedExclusivelyFrom(Clause.Role.Axiom))
		    return TptpParserOutput.FormulaRole.Axiom;

		if (cl.isDerivedExclusivelyFrom(Clause.Role.Hypothesis))
		    return TptpParserOutput.FormulaRole.Hypothesis;
		
		if (cl.isDerivedExclusivelyFrom(Clause.Role.NegatedConjecture))
		    return TptpParserOutput.FormulaRole.NegatedConjecture;
		
		return TptpParserOutput.FormulaRole.Plain;		
	    }
	else // !cl.isInput()
	    {
		// Can only be "plain"
		return TptpParserOutput.FormulaRole.Plain;
	    }


    } // CNFRole(Clause cl)


    private 
	static
	TptpParserOutput.CnfFormula CNFBody(Clause cl) {
	
	LinkedList<TptpParserOutput.Literal> literals = 
	    new LinkedList<TptpParserOutput.Literal>();

	for (Literal lit : cl.literals())
	    literals.addLast(convertLiteral(lit));
	

	if (literals.isEmpty()) 
	    literals.addLast(_factory.
			     createLiteral(true,
					   _factory.builtInFalse()));
	
	return _factory.createClause(literals);

    } // CNFBody(Clause cl)



    private 
	static TptpParserOutput.Literal convertLiteral(Literal lit) {

	TptpParserOutput.AtomicFormula atom = 
	    convertAtomic(lit.atom());

	return _factory.createLiteral(lit.isPositive(),atom);

    } // convertLiteral(Literal lit)




    private 
	static 
	TptpParserOutput.FofFormula convertLiteralToFormula(Literal lit) {

	
	TptpParserOutput.FofFormula atom = 
	    convertFormula(lit.atom());

	if (lit.isPositive()) return atom;

	return _factory.createNegationOf(atom);

    } // convertLiteralToFormula(Literal lit)



    private 
	static 
	TptpParserOutput.FofFormula convertFormula(Formula form) {

	if (form instanceof AtomicFormula)
	    {
		return _factory.atomAsFormula(convertAtomic(form));
	    }
	else if (form instanceof ConnectiveApplication)
	    {
		int con = 
		    ((ConnectiveApplication)form).
		    connective().id();

		if (con == Connective.Id.Not)
		    {
			assert 
			    ((ConnectiveApplication)form).argument()
			    instanceof
			    Formula;
			
			return 
			    _factory.
			    createNegationOf(convertFormula((Formula)
							    ((ConnectiveApplication)form).argument()));
		
		    }
		else
		    {
			assert 
			    ((ConnectiveApplication)form).
			    connective().arity() == 2;

			TermPair arg = 
			    (TermPair)((ConnectiveApplication)form).argument();
			
			Formula subformula1 = (Formula)arg.first();
			Formula subformula2 = (Formula)arg.second();
			
			
			TptpParserOutput.FofFormula result =
			    _factory.
			    createBinaryFormula(convertFormula(subformula1),
						convertBinaryConnective(con),
						convertFormula(subformula2));

			return result;


		    } // if (con == Connective.Id.Not)
	    }
	else
	    {
		assert form instanceof QuantifierApplication;

		TptpParserOutput.Quantifier quant = 
		    convertQuantifier(((QuantifierApplication)form).quantifier());
		
		AbstractionTerm abstr =
		    (AbstractionTerm)
		    ((QuantifierApplication)form).abstraction();

		LinkedList<String> vars = new LinkedList<String>();

		vars.add(abstr.variable().name());
				
		TptpParserOutput.FofFormula matrix = 
		    convertFormula((Formula)abstr.matrix());
		
		return
		    _factory.
		    createQuantifiedFormula(quant,vars,matrix);
	    }

       
    } // convertFormula(Formula form)
    


    private 
	static 
	TptpParserOutput.AtomicFormula convertAtomic(Formula atom) {
	
	assert atom instanceof AtomicFormula;

	String predicate = ((AtomicFormula)atom).predicate().name();
	
	LinkedList<TptpParserOutput.Term> arguments;

	Term arg = ((AtomicFormula)atom).argument();
	
	if (arg != null)
	    {
		arguments = 
		    new LinkedList<TptpParserOutput.Term>();
		while (arg.isPair())
		    {
			arguments.
			    addLast(convertTerm(((TermPair)arg).first()));
			arg = ((TermPair)arg).second();
		    };
		arguments.addLast(convertTerm(arg));
	    }
	else 
	    arguments = null;

	return 
	    _factory.
	    createPlainAtom(predicate,arguments);

    } // convertAtomic(Formula atom)


    
    private 
	static TptpParserOutput.Term convertTerm(Term term) {

	switch (term.kind())
	    {
	    case Term.Kind.Variable:
		return 
		    _factory.
		    createVariableTerm(((Variable)term).name());
	    case Term.Kind.CompoundTerm:
		{
		    String func =
			((CompoundTerm)term).function().name();

		    Term arg = ((CompoundTerm)term).argument();
		    
		    assert arg != null;
	
		    LinkedList<TptpParserOutput.Term> arguments = 
			new LinkedList<TptpParserOutput.Term>();
		    while (arg.isPair())
			{
			    arguments.addLast(convertTerm(((TermPair)arg).first()));
			    arg = ((TermPair)arg).second();
			};
		    arguments.addLast(convertTerm(arg));
		    
		    return 
			_factory.
			createPlainTerm(func,arguments);
		}				
		
	    case Term.Kind.IndividualConstant:
		    return 
			_factory.
			createPlainTerm(((IndividualConstant)term).
					name(),
					null);
		    
		
	    default:
		throw new Error("Wrong kind of term.");
		
	    } // switch (term.kind())

    } // convertTerm(Term term)



    private 
	static 
	TptpParserOutput.Annotations annotations(Clause cl) {

	TptpParserOutput.Source source;

	if (cl.isInput())
	    {
		if (cl.isStrictlyInput())
		    {
			source = 
			    _factory.createSourceFromFile("dummy_file_name",
							  "some_unmodified_input");
		    }
		else
		    {
			
			source = 
			    _factory.createSourceFromFile("dummy_file_name",
							  "some_modified_input");
		    }
	    }
	else 
	    {
		String inferenceRuleName = 
		    "'" + cl.inferences().toString("_") + "'";
		if (inferenceRuleName.equals("")) 
		    inferenceRuleName = "unspecified_inference";

		LinkedList<TptpParserOutput.InfoItem> inferenceInfo = 
		    new LinkedList<TptpParserOutput.InfoItem>();
		
		String statusValue;

		if (cl.inferenceWasUsed(InferenceType.ExistElimination))
		    {
			// skolemisation
			statusValue = "esa";
		    }
		else
		    statusValue = "thm";

		TptpParserOutput.InfoItem status = 
		    _factory.
		    createInferenceStatusInfoItem(statusValue);
		inferenceInfo.add(status);
		
		
		LinkedList<TptpParserOutput.ParentInfo> parentInfoList = 
		    new LinkedList<TptpParserOutput.ParentInfo>();
		
		for (Clause par : cl.parents())
		    {
			TptpParserOutput.Source numAsSource =  
			    _factory.
			    createSourceFromName(Long.toString(par.number()));
			    
			
			TptpParserOutput.ParentInfo parentInfo = 
			    _factory.
			    createParentInfo(numAsSource,null);

			parentInfoList.addLast(parentInfo);
		    };

		if (inferenceInfo.isEmpty()) inferenceInfo = null;
		
		assert parentInfoList != null;
		assert !parentInfoList.isEmpty();

		source = 
		    _factory.
		    createSourceFromInferenceRecord(inferenceRuleName,
						    inferenceInfo,
						    parentInfoList);
	    };

	return _factory.createAnnotations(source,null);

    } // annotations(Clause cl)


    
    private 
	static 
	TptpParserOutput.BinaryConnective 
	convertBinaryConnective(int con) {
	
	switch (con)
	    {
	    case Connective.Id.And: return TptpParserOutput.BinaryConnective.And;
	    case Connective.Id.Or: return TptpParserOutput.BinaryConnective.Or;
	    case Connective.Id.Equivalent: return TptpParserOutput.BinaryConnective.Equivalence;
	    case Connective.Id.Implies: return TptpParserOutput.BinaryConnective.Implication;
	    case Connective.Id.ReverseImplies: return TptpParserOutput.BinaryConnective.ReverseImplication;
	    case Connective.Id.NotEquivalent: return TptpParserOutput.BinaryConnective.Disequivalence;
	    case Connective.Id.NotOr: return TptpParserOutput.BinaryConnective.NotOr;
	    case Connective.Id.NotAnd: return TptpParserOutput.BinaryConnective.NotAnd;
	    }; 
	
	throw new Error("Bad binary connective.");

    } // convertBinaryConnective(Connective.Id con)


    
    private 
	static 
	TptpParserOutput.Quantifier convertQuantifier(Quantifier quant) {

	switch (quant.id())			
	    {
	    case Quantifier.Id.ForAll: return TptpParserOutput.Quantifier.ForAll;
	    case Quantifier.Id.Exist: return TptpParserOutput.Quantifier.Exists;
	    }
	
	throw new Error("Bad quantifier.");
	
    } // convertQuantifier(Quantifier quant)


    
    private static SimpleTptpParserOutput _factory = 
	new SimpleTptpParserOutput();

} // class TPTPPrinter