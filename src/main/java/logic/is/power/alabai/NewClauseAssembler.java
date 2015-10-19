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



import logic.is.power.cushion.*;

import logic.is.power.logic_warehouse.*;


/**
 * Implements assembling of clauses in the {@link alabai_je.NewClause}
 * representation.
 * Inspired by VK::OpenNewClause in 
 * <a href="{@docRoot}/../references.html#vampire_kernel_6_0">[Vampire kernel v6.0 sources]</a>.
 */
public class NewClauseAssembler {
    

    public NewClauseAssembler(SimpleReceiver<NewClause> resultReceiver) {
	_resultReceiver = resultReceiver;
	_printAssembled = false;
	_variableBank = new Variable.Bank(); 
	_variableRenaming = new VariableRenaming();
	_inputVariableRenaming = new InputVariableRenaming();
	_literalBodyAssembler = new FlattermAssembler();
    }

    /** Set the flag that switches printing of clauses in System.out when 
     *  they are completely assembled.
     */
    public void setPrintAssembled(boolean fl) { _printAssembled = fl; }

    
    /** Underlying variable renaming object. */
    public VariableRenaming variableRenaming() {
	return _variableRenaming;
    }


    /** A whole clause representing the formula is assembled. */
    public void convertInputFormula(InputSyntax.Formula form,
				    int role) {

	openClause(InferenceType.Input);
	
	_newClause.markAsDerivedFrom(role);
	
	openLiteral();
	
	_literalBodyAssembler.pushInputFormula(form,_inputVariableRenaming);
	
	endOfLiteral();
	
	endOfClause();

    } // convertInputFormula(InputSyntax.Formula form,..)
    
    /** A whole clause representing <b>clause</b> is assembled. */
    public
    void convertInputClause(Iterable<InputSyntax.Literal> clause,
			    int role) {

	openClause(InferenceType.Input);
	
	_newClause.markAsDerivedFrom(role);
  
	for (InputSyntax.Literal lit : clause)
	    pushInputLiteral(lit);
	
	endOfClause();
 
   } // convertInputClause(Iterable<InputSyntax.Literal> clause,..)
	


    /** A copy of <b>cl</b> is assembled; this procedure is considered as 
     *  an application of the copying inference, so <b>cl</b> becomes 
     *  the only parent of the new clause, and the flags of the new clause
     *  are adjusted accordingly.
     */
    public void copyClause(Clause cl) {

	openClause(InferenceType.Copy);
	
	for (Literal lit : cl.literals())
	    pushLiteral(lit);

	addParent(cl);
	
	endOfClause();

    } // copyClause(Clause cl)
    



    //
    //           Low-level functionality:  
    //
    

    /** Prepares the object for assembling another clause; in particular,
     *  it resets all associated variable renamings.
     *  @param src must be a value from {@link alabai_je.InferenceType}
     */
    public void openClause(int src) {
	
	assert src >= 0;
	assert src <= InferenceType.maxValue();

	_variableBank.reset();

	_newClause = new NewClause(_variableBank);
	
	_variableRenaming.reset(_variableBank);
	
	_inputVariableRenaming.reset(_variableBank);
	
	_clauseSource = src;

	_newClause.addInference(src);

    } // openClause(InferenceType src)
    

    public void openLiteral() { _literalBodyAssembler.reset(); } 


    public void endOfLiteral() {

	_literalBodyAssembler.wrapUp();

	_newClause.addLiteral(new TmpLiteral(_literalBodyAssembler.assembledTerm()));
    }


    /** Finalises the assembling of a clause; the assembled clause
     *  is sent to the result receiver. May print the clause in System.out if
     *  the corresponding flag is set (see setPrintAssembled(boolean fl)).
     */
    public void endOfClause() {
	
	switch (_clauseSource)
	    {
	    case InferenceType.Input: 
		++(Statistics.current().generatedClauses.input);
		break;
		
	    case InferenceType.Copy: 
		++(Statistics.current().generatedClauses.copied);
		break;
		
		
	    case InferenceType.Resolution: 
		++(Statistics.current().generatedClauses.byResolution);
		break;
		
		
	    case InferenceType.ForwardParamodulation: 
		++(Statistics.current().generatedClauses.byForwardParamodulation);
		break;
		
		
	    case InferenceType.BackwardParamodulation: 
		++(Statistics.current().generatedClauses.byBackwardParamodulation);
		break;
		
		
	    case InferenceType.EqualityFactoring: 
		++(Statistics.current().generatedClauses.byEqualityFactoring);
		break;
		
		
	    case InferenceType.EqualityResolution: 
		++(Statistics.current().generatedClauses.byEqualityResolution);
		break;
		
		
	    case InferenceType.BackwardDemodulation: 
		++(Statistics.current().generatedClauses.byBackwardDemodulation);
		break;
		
		
	    case InferenceType.BackwardSubsumptionResolution: 
		++(Statistics.current().generatedClauses.byBackwardSubsumptionResolution);
		break;
		
	    case InferenceType.DoubleNegationCancellation: // as below
	    case InferenceType.NegatedConjunction: // as below
	    case InferenceType.NegatedDisjunction: // as below
	    case InferenceType.NegatedEquivalence: // as below 
	    case InferenceType.NegatedImplication: // as below 
	    case InferenceType.NegatedReverseImplication: // as below
	    case InferenceType.NegatedNotEquivalent: // as below
	    case InferenceType.NegatedNotOr: // as below 
	    case InferenceType.NegatedNotAnd: // as below
	    case InferenceType.NegatedForAll: // as below
	    case InferenceType.NegatedExist: // as below 
	    case InferenceType.ConjunctionElimination: // as below 
	    case InferenceType.DisjunctionElimination: // as below
	    case InferenceType.EquivalenceElimination: // as below
	    case InferenceType.ImplicationElimination: // as below 
	    case InferenceType.ReverseImplicationElimination: // as below 
	    case InferenceType.NotEquivalentElimination: // as below 
	    case InferenceType.NotOrElimination: // as below 
	    case InferenceType.NotAndElimination: // as below 
	    case InferenceType.ForAllElimination: // as below 
	    case InferenceType.ExistElimination: // as below 
	    case InferenceType.NegatedTruthConstant: // as below 
		++(Statistics.current().generatedClauses.byClausification);
		break;


	    default:
		assert false;
		break;
		
	    }; // switch (_clauseSource)
	
	
	if (_printAssembled) 
	    System.out.println("New/" + 
			       InferenceType.toAbbreviatedString(_clauseSource) + 
			       ": " + _newClause);
	
	_resultReceiver.receive(_newClause);
	
	_newClause = null;
	
	_variableRenaming.clear();
	
	_inputVariableRenaming.clear();
	
    } // endOfClause()
    


    /** <b>pre:</b> clause assembling in progress, 
     *  ie openClause(ClauseSource src) has been called unmatched 
     *  by an endOfClause().
     */
    public final void addParent(Clause parentClause) { 
	_newClause.addParent(parentClause);
    }
      

    public final void pushVariable(Variable var) {
	_literalBodyAssembler.pushVar(var);
    }

    public final void pushConstant(IndividualConstant c) {
	_literalBodyAssembler.pushConst(c);
    }

    public final void pushFunction(Function func) {
	_literalBodyAssembler.pushFunc(func);
    }

    public final void pushPredicate(Predicate pred) {
	_literalBodyAssembler.pushPred(pred);
    }


    public final void pushConnective(Connective con) {
	_literalBodyAssembler.pushConnective(con);
    }


    public final void pushQuantifier(Quantifier quant) {
	_literalBodyAssembler.pushQuant(quant);
    }


    public final void pushAbstractionVar(Variable var) {
	_literalBodyAssembler.pushAbstractionVar(var);
    }

	

    /** Applies the main renaming to the variable and applies
     *  pushVariable(Variable var) to the result. 
     */
    public final void pushVariableWithRenaming(Variable var) {
	pushVariable(_variableRenaming.rename(var));
    }

       
    /** Applies the main renaming to the variable and then
     *  calls pushAbstractionVar(Variable var) on it.
     */
    public final void pushAbstractionVarWithRenaming(Variable var) {
	pushAbstractionVar(_variableRenaming.rename(var));
    }

	
    /** Copies the term into the current position in the literal
     *  being currently assembled; the copying is done modulo
     *  the main variable renaming associated with this object,
     *  which is extended on the fly.
     */
    public final void pushTermWithRenaming(Term term) {
	_literalBodyAssembler.pushTerm(term,_variableRenaming);
    }




    /** Copies the literal specified with the polarity and the atom
     *  (which is not necessarily an atomic formula) into
     *  the current position in the clause being currently assembled;
     *  the copying is done modulo the main variable renaming associated
     *  with this object, which is extended on the fly.
     *  <b>pre:</b> atom.isFormula().
     */
    public final void pushLiteralWithRenaming(boolean positive,Term atom) {

	assert atom.isFormula();

	openLiteral();
	
	_literalBodyAssembler.pushLiteral(positive,atom,_variableRenaming);
    
	endOfLiteral();
    }
      
      
    /** Shorthand for pushLiteralWithRenaming(lit.isPositive(),lit.atom()). */
    public final void pushLiteralWithRenaming(Literal lit) {
	pushLiteralWithRenaming(lit.isPositive(),lit.atom());
    }



    /** Copies the term into the current position in the literal
     *  being currently assembled; the copying is done literally,
     *  ie, no variable renaming is applied.
     */
    public final void pushTerm(Term term) {
	_literalBodyAssembler.pushTerm(term);
    }




    /** Copies the literal specified with the polarity and the atom
     *  (which is not necessarily an atomic formula) into
     *  the current position in the clause being currently assembled;
     *  the copying is done literally, ie, no variable renaming is applied.
     *  <b>pre:</b> atom.isFormula().
     */
    public final void pushLiteral(boolean positive,Term atom) {

	assert atom.isFormula();

	openLiteral();
	
	_literalBodyAssembler.pushLiteral(positive,atom);
    
	endOfLiteral();
    }
      
      
    /** Shorthand for pushLiteralWithRenaming(lit.isPositive(),lit.atom()). */
    public final void pushLiteral(Literal lit) {
	pushLiteral(lit.isPositive(),lit.atom());
    }







    /** Copies the specified literal (which is not necessarily an 
     *  ordinary literal, although  it cannot contain quantifiers 
     *  or abstraction) into the current position in the clause being 
     *  currently assembled; the copying is done modulo the global 
     *  substitution, ie, all instantiated variables are replaced
     *  with the corresponding terms.
     */
    public final void pushLiteralWithGlobSubst(Flatterm lit) {

	openLiteral();

	_literalBodyAssembler.pushLiteralWithGlobSubst(lit);
	
	endOfLiteral();
    }


    /** Copies the specified literal (which is not necessarily an 
     *  ordinary literal, although it cannot contain quantifiers 
     *  or abstraction) into the current position in the clause being 
     *  currently assembled; the copying is done modulo the global 
     *  substitution, ie, all instantiated variables are replaced
     *  with the corresponding terms.
     */
    public final void pushLiteralWithGlobSubst(boolean positive,
					       Term atom) {

	openLiteral();

	_literalBodyAssembler.pushLiteralWithGlobSubst(positive,atom);

	endOfLiteral();
    }


    /** Copies the specified as the next part of the literal
     *  being currently assembled; the copying is done modulo the global 
     *  substitution, ie, all instantiated variables are replaced
     *  with the corresponding terms.
     */
     
    public final void pushTermWithGlobSubst(Flatterm term) {

	_literalBodyAssembler.pushTermWithGlobSubst(term);

    } 









    /** Copies the specified literal (which is not necessarily an 
     *  ordinary literal, although  it cannot contain quantifiers 
     *  or abstraction) into the current position in the clause being 
     *  currently assembled; the copying is done modulo the global 
     *  substitution, ie, all instantiated variables are replaced
     *  with the corresponding terms, and modulo the main variable 
     *  renaming associated with this object, which is extended on the fly.
     */
    public final void pushLiteralWithGlobSubstAndRenaming(Flatterm lit) {

	openLiteral();

	_literalBodyAssembler.pushLiteralWithGlobSubst(lit,_variableRenaming);
	
	endOfLiteral();
    }


    /** Copies the specified literal (which is not necessarily an 
     *  ordinary literal, although it cannot contain quantifiers 
     *  or abstraction) into the current position in the clause being 
     *  currently assembled; the copying is done modulo the global 
     *  substitution, ie, all instantiated variables are replaced
     *  with the corresponding terms, and modulo the main variable 
     *  renaming associated with this object, which is extended on the fly.
     */
    public final void pushLiteralWithGlobSubstAndRenaming(boolean positive,
							  Term atom) {

	openLiteral();

	_literalBodyAssembler.pushLiteralWithGlobSubst(positive,
						       atom,
						       _variableRenaming);

	endOfLiteral();
    }


    /** Copies the specified term as the next part of the literal
     *  being currently assembled; the copying is done modulo the global 
     *  substitution, ie, all instantiated variables are replaced
     *  with the corresponding terms, and modulo the main variable 
     *  renaming associated with this object, which is extended on the fly.
     */
     
    public final void pushTermWithGlobSubstAndRenaming(Flatterm term) {

	_literalBodyAssembler.pushTermWithGlobSubst(term,_variableRenaming);

    } 

    /** Copies the specified term as the next part of the literal
     *  being currently assembled; the copying is done modulo global 
     *  substitution 3, ie, all instantiated variables are replaced
     *  with the corresponding terms, and modulo the main variable 
     *  renaming associated with this object, which is extended on the fly.
     */
     
    public final void pushTermWithGlobSubst3AndRenaming(Term term) {

	_literalBodyAssembler.pushTermWithGlobSubst3(term,_variableRenaming);

    } 






    private void pushInputLiteral(InputSyntax.Literal lit) {
    
	openLiteral();

	_literalBodyAssembler.pushInputLiteral(lit,_inputVariableRenaming);
    
	endOfLiteral();
    
    }


    //                      Data:


    /** Where the assembled clauses are passed to. */
    private SimpleReceiver<NewClause> _resultReceiver;
    
    private boolean _printAssembled;

    /** Variables for new clauses will be taken from this bank. */
    private Variable.Bank _variableBank;

    /** Main variable renaming which is used to rename variables
     *  on-the-fly, when copies of some terms or literals are assembled
     *  as parts of a new clause.
     */
    private VariableRenaming _variableRenaming;

    /** Keeps the mapping from input variables to variables in the regular
     *  Variable representation.
     */
    private InputVariableRenaming _inputVariableRenaming;	
		      
    private NewClause _newClause;
    
    /** Source label of the clause in _newClause. */
    private int _clauseSource;
    
    /** Assembles bodies (underlying flatterms) of literals. */
    private FlattermAssembler _literalBodyAssembler;
    
    

}; // class NewClauseAssembler
