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


/** Encapsulates all backward demodulation, including the index. */
final class BackwardDemodulation {

    /** @param clauseDiscarder where to send the discarded clauses 
     *  @param simplifiedClauseAssembler where the simplified versions of
     *         clauses may be assembled
     */
    public 
	BackwardDemodulation(SimpleReceiver<Clause> clauseDiscarder,
			     NewClauseAssembler simplifiedClauseAssembler) {
	_clauseDiscarder = clauseDiscarder;
	_simplifiedClauseAssembler = simplifiedClauseAssembler;
	_printDiscarded = false;
	_index = new BackwardDemodulationIndex();
    }

    public void setTermFeatureFunctionVector(TermFeatureVector vector) {
	_index.setFeatureFunctionVector(vector);

	_retrieval = _index.new Retrieval();
	// This is not in the constructor because a retrieval object
	// can only be created after the feature function vector is set.
    }

    public void setPrintDiscarded(boolean fl) { _printDiscarded = fl; }

    public final void insert(Clause cl) {
	_index.insert(cl);
    }
    
    public final boolean remove(Clause cl) {
	return _index.remove(cl);
    }

    /** @return false if no clauses could be simplified */
    public final boolean simplifyBy(Clause cl) {

	if (cl.literals().size() != 1) return false;;

	Literal lit = cl.literals().iterator().next();

	if (!lit.isEquality() || lit.isNegative()) return false;
	
	Term arg1 = lit.atom().firstArg();
	Term arg2 = lit.atom().secondArg();

	boolean result = false;

	if (ReductionOrdering.
	    current().
	    canBeGreaterModuloSubst(arg1,arg2) &&
	    simplifyBy(cl,arg1,arg2))
	    result = true;

	if (ReductionOrdering.
	    current().
	    canBeGreaterModuloSubst(arg2,arg1) &&
	    simplifyBy(cl,arg2,arg1))
	    result = true;
	
	return result;

    } // simplifyBy(NewClause cl)





    private boolean simplifyBy(Clause cl,Term lhs,Term rhs) {

	_retrieval.reset(lhs,rhs);
	
	if (!_retrieval.hasNext()) return false;
	

	do
	    {
		Pair<Clause,Term> indexedObj = _retrieval.next();
		
		Clause simplifiedClause = indexedObj.first;
		
		if (simplifiedClause != cl)
		    {
			Term redex = indexedObj.second;
	
			rewrite(simplifiedClause,redex,rhs,cl);
			
			++(Statistics.current().
			   backwardSimplifyingInferences.
			   demodulation);
			
			if (_printDiscarded)
			    System.out.println("Discard/Kept/Demod: " +
					       simplifiedClause);
			
			++(Statistics.current().
			   keptClauses.discardedAsSimplifiedByDemodulation);
			
			_clauseDiscarder.receive(simplifiedClause);
		    };
	    }	
	while (_retrieval.hasNext());

	_retrieval.clear();

	return true;


    } // simplifyBy(Clause cl,Term lhs,Term rhs)



    private void rewrite(Clause simplifiedClause,
			 Term redex,
			 Term redexReplacement,
			 Clause simplifier)
    {
	_simplifiedClauseAssembler.
	    openClause(InferenceType.BackwardDemodulation);

	_simplifiedClauseAssembler.addParent(simplifiedClause);
	_simplifiedClauseAssembler.addParent(simplifier);

	boolean somethingRewritten = false;
	for (Literal lit : simplifiedClause.literals())
	    {
		if (somethingRewritten)
		    {
			// Just copy the literal to _simplifiedClauseAssembler:
			
			_simplifiedClauseAssembler.
			    pushLiteralWithRenaming(lit);
		    }
		else
		    somethingRewritten = 
			rewrite(lit,redex,redexReplacement,simplifier);
	    };

	_simplifiedClauseAssembler.endOfClause();

    } // rewrite(Clause simplifiedClause,..)



    private boolean rewrite(Literal lit,
			    Term redex,
			    Term redexReplacement,
			    Clause simplifier)
    {
	boolean result = false;
	
	_simplifiedClauseAssembler.openLiteral();

	if (lit.isNegative())
	    _simplifiedClauseAssembler.
		pushConnective(Connective.getNot());

	Term.LeanIterator subtermIter = 
	    new Term.LeanIterator(lit.atom());
	
	do
	    {
		Term subterm = subtermIter.next();
		
		if (!result && subterm.equals(redex))
		    {
			result = true;

			// This occurence of the redex is really rewritten:
			_simplifiedClauseAssembler.
			    pushTermWithGlobSubst3AndRenaming(redexReplacement);
			
			subtermIter.skipSubtermRemainder();

		    }
		else
		    {
			switch (subterm.kind())
			    {
			    case Term.Kind.Variable: 
				_simplifiedClauseAssembler.
				    pushVariableWithRenaming((Variable)subterm);
				break;
				    

			    case Term.Kind.CompoundTerm: 
				_simplifiedClauseAssembler.
				    pushFunction(((CompoundTerm)subterm).
						 function());
				break;
				

			    case Term.Kind.IndividualConstant: 
				_simplifiedClauseAssembler.
				    pushConstant((IndividualConstant)subterm);
				break;

			    case Term.Kind.AtomicFormula: 
				_simplifiedClauseAssembler.
				    pushPredicate(((AtomicFormula)subterm).
						  predicate());
				break;
				
	    
			    case Term.Kind.ConnectiveApplication:
				_simplifiedClauseAssembler.
				    pushConnective(((ConnectiveApplication)subterm).
						   connective());
				break;

			    case Term.Kind.QuantifierApplication:
				_simplifiedClauseAssembler.
				    pushQuantifier(((QuantifierApplication)subterm).
						   quantifier());
				break;

			    case Term.Kind.AbstractionTerm: 
				_simplifiedClauseAssembler.
				    pushAbstractionVar(((AbstractionTerm)subterm).
						       variable());
				break;
				
			    default:
				assert false;
				
			    }; // switch (subterm.kind())
	

		    }; // if (subterm.equals(redex))
	    }
	while (subtermIter.hasNext());

	_simplifiedClauseAssembler.endOfLiteral();
	
	return result;

    } // rewrite(Literal lit,..)



    //                Data:

    private SimpleReceiver<Clause> _clauseDiscarder;

    private NewClauseAssembler _simplifiedClauseAssembler;
    
    private boolean _printDiscarded;
    
    private final BackwardDemodulationIndex _index;

    private BackwardDemodulationIndex.Retrieval _retrieval;


} // class BackwardDemodulation