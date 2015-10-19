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
 * Literal selection function that selects only negative literals
 * if there is at least one.
 */
public class LitSelFunPosHyper implements LiteralSelectionFunction {
    
    /** @param filterForNegative will be used to select from negative
     *         literals if there is at least one
     *  @param filterForPositive will be used to select from positive
     *         literals when there will be no negative ones
     */
    public LitSelFunPosHyper(LiteralSelectionFunction filterForNegative,
			     LiteralSelectionFunction filterForPositive) 
    {
	_filterForNegative = filterForNegative;
	_filterForPositive = filterForPositive;
    }
    
    
    public final void select(Collection<Literal> lits,
			     Collection<Literal> selected,
			     Collection<Literal> unselected) {
	

	for (Literal lit : lits)
	    if (lit.isNegative())
		{
		    // Select only some negative literals.
		    LinkedList<Literal> negativeLits = 
			new LinkedList<Literal>();
		    for (Literal lit1 : lits)
			if (lit1.isNegative())
			    {
				negativeLits.add(lit1);
			    }
			else
			    unselected.add(lit1);

		    _filterForNegative.select(negativeLits,
					      selected,
					      unselected);
		    return;
		};

	// Only positive literals.
	
	_filterForPositive.select(lits,selected,unselected);

    } // select(Collection<Literal> lits,
    


    
    //                  Data:

    private LiteralSelectionFunction _filterForNegative;

    private LiteralSelectionFunction _filterForPositive;

  }; // class LitSelFunPosHyper
