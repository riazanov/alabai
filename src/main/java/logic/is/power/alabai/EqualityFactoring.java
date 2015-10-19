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

/**
 * Performs equality factoring on specified clauses.
 * TODO: Ordering constraint checks MUST be added to detect
 *       redundant inferences.
 * TODO: can be optimised
 */
class EqualityFactoring {


    public 
	EqualityFactoring(NewClauseAssembler conclusionAssembler) {
	_conclusionAssembler = conclusionAssembler;
	_variableBank = new Variable.Bank();
	_variableRenaming = new VariableRenaming(); 
	_literalAssembler = new FlattermAssembler(); 
	_unifier = new Substitution1();
    }

    
    /** Performs all possible equality factoring inferences
     *  and sends the results to the associated conclusion assembler.
     */
    public final void performAllInferences(Clause cl) {
	
	_clause = cl;
	
	_variableBank.reset();
				
	_variableRenaming.reset(_variableBank);

	_literals = new LinkedList<Flatterm>();

	for (Literal lit : cl.literals())
	    {
		_literalAssembler.reset();
		_literalAssembler.pushLiteral(lit.isPositive(),
					      lit.atom(),
					      _variableRenaming);
		_literalAssembler.wrapUp();
		_literals.addLast(_literalAssembler.assembledTerm());
	    };


	for (Flatterm litST : _literals)
	    if (litST.isPositive() &&
		litST.isEqualityLiteral())
		{

		    for (Flatterm litSPrimeTPrime : _literals)
			if (litSPrimeTPrime != litST &&
			    litSPrimeTPrime.isPositive() &&
			    litSPrimeTPrime.isEqualityLiteral())
			    {
				Flatterm s = litST.atom().firstArg();
				Flatterm t = litST.atom().secondArg();

				
				Flatterm sPrime = 
				    litSPrimeTPrime.atom().firstArg();
				Flatterm tPrime = 
				    litSPrimeTPrime.atom().secondArg();

				if (Unification.unify(s,sPrime,_unifier))
				    {
					createConclusion(litST,
							 s,
							 t,
							 litSPrimeTPrime,
							 sPrime,
							 tPrime);
					_unifier.uninstantiateAll();
				    };

				// Rotate litSPrimeTPrime:
				
				sPrime = 
				    litSPrimeTPrime.atom().secondArg();
				tPrime = 
				    litSPrimeTPrime.atom().firstArg();

				if (Unification.unify(s,sPrime,_unifier))
				    {
					createConclusion(litST,
							 s,
							 t,
							 litSPrimeTPrime,
							 sPrime,
							 tPrime);
					_unifier.uninstantiateAll();
				    };


				// Rotate litST:
				s = litST.atom().secondArg();
				t = litST.atom().firstArg();
				
				sPrime = 
				    litSPrimeTPrime.atom().firstArg();
				tPrime = 
				    litSPrimeTPrime.atom().secondArg();

				if (Unification.unify(s,sPrime,_unifier))
				    {
					createConclusion(litST,
							 s,
							 t,
							 litSPrimeTPrime,
							 sPrime,
							 tPrime);
					_unifier.uninstantiateAll();
				    };

				// Rotate litSPrimeTPrime:
				
				sPrime = 
				    litSPrimeTPrime.atom().secondArg();
				tPrime = 
				    litSPrimeTPrime.atom().firstArg();

				if (Unification.unify(s,sPrime,_unifier))
				    {
					createConclusion(litST,
							 s,
							 t,
							 litSPrimeTPrime,
							 sPrime,
							 tPrime);
					_unifier.uninstantiateAll();
				    };

			    }; // if (litSPrimeTPrime != litST &&
		    
		}; // if (litST.isPositive() &&
	    
	
	
	_literals = null;

    } //  performAllInferences(Clause cl)



    //                Private methods:

    
    private final void createConclusion(Flatterm litST,
					Flatterm s,
					Flatterm t,
					Flatterm litSPrimeTPrime,
					Flatterm sPrime,
					Flatterm tPrime) {
	assert litST != litSPrimeTPrime;
	
	if (_clause.isDiscarded()) return;

	_conclusionAssembler.
	    openClause(InferenceType.EqualityFactoring);
	
	_conclusionAssembler.addParent(_clause);
	
	
	// Residual literals:
	for (Flatterm lit : _literals)
	    if (lit != litST &&
		lit != litSPrimeTPrime)
		_conclusionAssembler.
		    pushLiteralWithGlobSubstAndRenaming(lit);

	// New literals:
	
	// s == tPrime
	_conclusionAssembler.openLiteral();

	_conclusionAssembler.pushPredicate(litST.predicate());
	_conclusionAssembler.pushTermWithGlobSubstAndRenaming(s);
	_conclusionAssembler.pushTermWithGlobSubstAndRenaming(tPrime);

	_conclusionAssembler.endOfLiteral();
	

	// t != tPrime
	_conclusionAssembler.openLiteral();

	_conclusionAssembler.pushConnective(Connective.getNot());
	_conclusionAssembler.pushPredicate(litST.predicate());
	_conclusionAssembler.pushTermWithGlobSubstAndRenaming(t);
	_conclusionAssembler.pushTermWithGlobSubstAndRenaming(tPrime);

	_conclusionAssembler.endOfLiteral();

	
	_conclusionAssembler.endOfClause();

	++(Statistics.current().deductionInferences.equalityFactoring.accomplished);
	
	assert GlobalEventCounter.inc();
	
    } // createConclusion(Flatterm litST,..)

    
    
    //                           Data:

    private NewClauseAssembler _conclusionAssembler;

    private Clause _clause;
    
    private LinkedList<Flatterm> _literals;

    private Variable.Bank _variableBank;
    
    private VariableRenaming _variableRenaming;

    private FlattermAssembler _literalAssembler;
    
    private Substitution1 _unifier;


} // class EqualityFactoring