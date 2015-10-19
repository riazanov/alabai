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

import logic.is.power.logic_warehouse.*;


/**
 * Specialises {@link alabai_je.SidePremise} for keeping
 * premises of paramodulation that contain the redex.
 */
class SidePremiseWithRedexForParamodulation extends SidePremise {

    public
	SidePremiseWithRedexForParamodulation(NewClauseAssembler conclusionAssembler) 
    {
	super(conclusionAssembler);
	_conclusionAssembler = conclusionAssembler;
    }



    /** Makes the clause the current premise with the specified
     *  active literal and the redex specified by its position
     *  in the atom of the active literal.
     */    
    public 
	void 
	load(Clause cl,Literal activeLiteral,int redexPosition) {
	
	super.load(cl,activeLiteral);

	_literalAssembler.reset();
	_literalAssembler.pushLiteralWithGlobSubst(activeLiteral.isPositive(),
						   activeLiteral.atom());
	_literalAssembler.wrapUp();
    
	_activeLiteralFlatterm = _literalAssembler.assembledTerm();

	int newRedexPosition = 
	    activeLiteral.atom().mapPositionWithSubst1(redexPosition);

	_redexFlatterm = 
	    _activeLiteralFlatterm.
	    atom().
	    subtermInPosition(newRedexPosition);

	_redexVariableSet = new HashSet<Variable>();
	activeLiteral.
	    atom().
	    subtermInPosition(redexPosition).
	    collectFreeVariables(_redexVariableSet);

    } // load(Clause cl,Literal activeLiteral,int redexPosition)


    /** Marks the end of using the current clause as the premise;
     *  performs some clean-up. 
     */
    public void unload() {

	super.unload();
	_redexVariableSet = null;
    }

    
    /** Active literal (after instantiation with global substitution 1). */
    public 
	final 
	Flatterm 
	activeLiteralFlatterm() { return _activeLiteralFlatterm; }

    /** Redex in the active literal (after instantiation with global substitution 1). */
    public final Flatterm redexFlatterm() { return _redexFlatterm; }


    /** Dumps the @ref residual_literals "residual literals" 
     * <a href="{@docRoot}/../glossary.html#residual_literals">residual literals</a>
     *  and the active literal after replacing the redex in it with
     *  <code>redexReplacement</code>,
     *  in the {@link alabai_je.NewClauseAssembler} associated with this object;
     *  the term <code>redexReplacement</code> is taken modulo the global 
     *  substitution 1.
     * <b>IMPORTANT:</b> This method does not open a new clause in 
     *             the {@link alabai_je.NewClauseAssembler}! This should
     *             be done externally before the method is called.
     */
    public final void assembleLiterals(Flatterm redexReplacement) {

	super.assembleResidualLiterals();

	// Assemble the active literal after replacing the redex:

	_conclusionAssembler.openLiteral();
	
	Flatterm cursor = _activeLiteralFlatterm;


	do
	    {
		if (cursor == _redexFlatterm)
		    {
			// Push the replacement term:

			_conclusionAssembler.
			    pushTermWithGlobSubstAndRenaming(redexReplacement);

			cursor = cursor.after();
		    }
		else // cursor != _redexFlatterm
		    {
			// Push the symbol or its image wrt the subst:

			
			switch (cursor.kind())
			    {
			    case Term.Kind.Variable: 
				if (cursor.variable().isInstantiated1())
				    {
					_conclusionAssembler.
					    pushTermWithGlobSubstAndRenaming(cursor.
									     variable().
									     instance1());
				    }
				else
				    _conclusionAssembler.
					pushVariableWithRenaming(cursor.variable());
				break;
				    

			    case Term.Kind.CompoundTerm: 
				_conclusionAssembler.
				    pushFunction(cursor.function());
				break;
				

			    case Term.Kind.IndividualConstant: 
				_conclusionAssembler.
				    pushConstant(cursor.individualConstant());
				break;

			    case Term.Kind.AtomicFormula: 
				_conclusionAssembler.
				    pushPredicate(cursor.predicate());
				break;
				
	    
			    case Term.Kind.ConnectiveApplication:
				_conclusionAssembler.
				    pushConnective(cursor.connective());
				break;

			    case Term.Kind.QuantifierApplication:
				_conclusionAssembler.
				    pushQuantifier(cursor.quantifier());
				break;

			    case Term.Kind.AbstractionTerm: 
				_conclusionAssembler.
				    pushAbstractionVarWithRenaming(cursor.variable());
				break;
				
			    default:
				assert false;
				
			    }; // switch (kind())
	

			cursor = cursor.nextCell();

		    }; // if (cursor == _redexFlatterm)	
	    }
	while (cursor != _activeLiteralFlatterm.after());
	
	_conclusionAssembler.endOfLiteral();


    } // assembleLiterals(Flatterm redexReplacement)


    
    /** Free variables of the redex, ignoring the instantiation. */
    public final Set<Variable> redexVariableSet() {
	return _redexVariableSet;
    }
    


    //                  Data:
	
    private NewClauseAssembler _conclusionAssembler;

    private Flatterm _activeLiteralFlatterm;
	
    private Flatterm _redexFlatterm;

    private Set<Variable> _redexVariableSet;
	
} // class SidePremiseWithRedexForParamodulation