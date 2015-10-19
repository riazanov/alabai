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
 * Literal selection function that separately selects all maximal literals
 * wrt the current admissible ordering, and some negative literals (if any),
 * and then choses the best of these two selections according 
 * to a specified literal collection ordering.
 */
public class LitSelFunMaxNegCompetition implements LiteralSelectionFunction {
    
    /**  @param filterForNegative will be used to select from negative
     *         literals if there is at least one
     *   @param selectionQualityOrdering will be used to decide if the maximal
     *         selection is better then the negative selection ("greater" means
     *         "better", all other values mean "worse")
     */
    public 
	LitSelFunMaxNegCompetition(LiteralSelectionFunction filterForNegative,
				   LiteralCollectionOrdering selectionQualityOrdering)
    {
	_filterForNegative = filterForNegative;
	_selectionQualityOrdering = selectionQualityOrdering;
	_maximalSelection = new LitSelFunAllMaximal();
    }

    
    public final void select(Collection<Literal> lits,
			     Collection<Literal> selected,
			     Collection<Literal> unselected) {
	

	for (Literal lit : lits)
	    if (lit.isNegative())
		{
		    // Negative selection:

		    LinkedList<Literal> negSelected = 
			new LinkedList<Literal>();
		    LinkedList<Literal> negUnselected = 
			new LinkedList<Literal>();

		    LinkedList<Literal> negativeLits = 
			new LinkedList<Literal>();
		    for (Literal lit1 : lits)
			if (lit1.isNegative())
			    {
				negativeLits.add(lit1);
			    }
			else
			    negUnselected.add(lit1);	


		    _filterForNegative.select(negativeLits,
					      negSelected,
					      negUnselected);
		    // Note that lits has not changed so far.


		    // Maximal selection:
		    
		    LinkedList<Literal> maximal = 
			new LinkedList<Literal>();
		    LinkedList<Literal> nonmaximal = 
			new LinkedList<Literal>();
		    
		    _maximalSelection.select(lits,
					     maximal,
					     nonmaximal);

		    
		    // Chosing between the maximal and negative selection:
		    
		    int cmp = 
			_selectionQualityOrdering.compare(maximal,
							  negSelected);

		    if (cmp == ComparisonValue.Greater)
			{
			    // Maximal selection wins:
			    selected.addAll(maximal);
			    unselected.addAll(nonmaximal);
			}
		    else
			{
			    // Negative selection wins:
			    selected.addAll(negSelected);
			    unselected.addAll(negUnselected);
			};
		    
		    return;
		};

	// Only positive literals, select all the maximal ones:
	
	_maximalSelection.select(lits,selected,unselected);

    } // select(Collection<Literal> lits,


    
    //                  Data:


    private LiteralSelectionFunction _filterForNegative;

    private LiteralCollectionOrdering _selectionQualityOrdering;

    private LitSelFunAllMaximal _maximalSelection;
	
} //  class LitSelFunMaxNegCompetition 