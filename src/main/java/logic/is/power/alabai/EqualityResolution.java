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
 * Performs equality resolution on specified clauses.
 * TODO: can be optimised
 */
class EqualityResolution {

    
    /** @param conclusionAssembler where the conclusions of inferences
     *         will be assembled
     */
    public EqualityResolution(NewClauseAssembler conclusionAssembler) {
	
	_conclusionAssembler = conclusionAssembler;
	_variableBank = new Variable.Bank();
	_variableRenaming = new VariableRenaming(); 
	_literalAssembler = new FlattermAssembler(); 
	_unifier = new Substitution1();
    } 

    
    /** Performs all possible equality resolution inferences
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

	
	for (Flatterm currentLiteral : _literals)
	    if (currentLiteral.isNegative() &&
		currentLiteral.isEqualityLiteral())
		{
		    if (Unification.unify(currentLiteral.atom().firstArg(),
					  currentLiteral.atom().secondArg(),
					  _unifier))
			{
			    createResolvent(currentLiteral);
			    _unifier.uninstantiateAll();
			};
		};
	
	_literals = null;

    } // performAllInferences(Clause cl)




    //                Private methods:

    
    private final void createResolvent(Flatterm currentLiteral) {
	
	if (_clause.isDiscarded()) return;

	_conclusionAssembler.
	    openClause(InferenceType.EqualityResolution);

	_conclusionAssembler.addParent(_clause);
	
	for (Flatterm lit : _literals)
	    if (lit != currentLiteral)
		_conclusionAssembler.
		    pushLiteralWithGlobSubstAndRenaming(lit);

	_conclusionAssembler.endOfClause();

	
	++(Statistics.current().deductionInferences.equalityResolution.accomplished);
	

	assert GlobalEventCounter.inc();
	
    } // createResolvent(Flatterm currentLiteral)




    //                           Data:

    private NewClauseAssembler _conclusionAssembler;

    private Clause _clause;
    
    private LinkedList<Flatterm> _literals;

    private Variable.Bank _variableBank;
    
    private VariableRenaming _variableRenaming;

    private FlattermAssembler _literalAssembler;
    
    private Substitution1 _unifier;

} // class EqualityResolution 