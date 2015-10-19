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


/**
 * Implements heuristic-controlled selection of literals.
 */
class LiteralSelection implements SimpleReceiver<Clause> {

    public LiteralSelection(SimpleReceiver<Clause> outputReceiver) {
	_outputReceiver = outputReceiver;
	_litSelFunForNegatedConjectureDescendants = 
	    new LitSelFunTotal();
	_litSelFunForHypothesisDescendants = new LitSelFunTotal();
	_litSelFunForAxiomDescendants = new LitSelFunTotal();
    }

    public void setFuncForNegatedConjectures(LiteralSelectionFunction selFunc) {
	_litSelFunForNegatedConjectureDescendants = selFunc;
	
    }

    public void setFuncForHypotheses(LiteralSelectionFunction selFunc) {
	_litSelFunForHypothesisDescendants = selFunc;
	
    }
    public void setFuncForAxioms(LiteralSelectionFunction selFunc) {
	_litSelFunForAxiomDescendants = selFunc;
	
    }

    

    /** Processes the clause by selecting the relevant parts, and sends 
     *  it to the output receiver.
     *  @return <code>true</code> (always) 
     */
    public boolean receive(Clause cl) {

	LinkedList<Literal> lits = new LinkedList<Literal>(cl.literals());
	LinkedList<Literal> selectedLits = 
	    new LinkedList<Literal>();
	LinkedList<Literal> unselectedLits = 
	    new LinkedList<Literal>();
	
	// Unselect answer and constraint literals:

	for (Iterator<Literal> iter = lits.iterator(); iter.hasNext();)
	    {
		Literal lit = iter.next();
		if (lit.isAnswer() || lit.isConstraintLiteral())
		    {
			lit.markAsUnselected();
			unselectedLits.addLast(lit);
			iter.remove();
		    };
	    };

	


	if (cl.isDerivedFrom(Clause.Role.NegatedConjecture)) 
	    {
		_litSelFunForNegatedConjectureDescendants.
		    select(lits,selectedLits,unselectedLits);
	    }
	else if (cl.isDerivedFrom(Clause.Role.Hypothesis)) 
	    {
		_litSelFunForHypothesisDescendants.
		    select(lits,selectedLits,unselectedLits);      
	    }
	else
	    {
		assert cl.isDerivedFrom(Clause.Role.Axiom);
		
		_litSelFunForAxiomDescendants.
		    select(lits,selectedLits,unselectedLits);      
	    };

	
	// Mark all literals from selectedLits as selected.
	
	for (Literal lit : selectedLits)
	    lit.markAsSelected();
	
	// Mark all literals from unselectedLits as necessarily unselected.
	// One day we will do it differently: we will do heuristic reselection
	// in order to identify literals and subterms that should be selected 
	// even if they can be unselected.
	
	for (Literal lit : unselectedLits)
	    lit.markAsUnselected();

	_outputReceiver.receive(cl);
  
	return true;

    } // receive(Clause cl)
      


    //                     Data:


    private SimpleReceiver<Clause> _outputReceiver;
      
    /** Literal selection function for clauses having at least
     *  one negative conjecture ancestor, including input
     *  negated conjecture clauses themselves.
     */
    private LiteralSelectionFunction _litSelFunForNegatedConjectureDescendants;
      
    /** Literal selection function for clauses having at least
     *  one hypothesis ancestor and no negative conjecture ancestors, 
     *  including input hypothesis clauses themselves.
     */
    private LiteralSelectionFunction _litSelFunForHypothesisDescendants;

      
    /** Literal selection function for clauses having no hypothesis 
     *  or negated conjecture clauses as ancestors.
     */
    private LiteralSelectionFunction _litSelFunForAxiomDescendants;


}; // class LiteralSelection
