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
 * Literal selection function that selects all literals maximal
 * wrt the specified literal ordering.
 */
public class LitSelFunAllMaximalWRT implements LiteralSelectionFunction {
    
    public LitSelFunAllMaximalWRT(LiteralOrdering ord) 
    {
	_literalOrdering = ord;
    }
    
    
    public final void select(Collection<Literal> lits,
			     Collection<Literal> selected,
			     Collection<Literal> unselected) {

	while (!lits.isEmpty())
	    {
		Iterator<Literal> iter = lits.iterator();
		
		Literal cand = iter.next();
		
		iter.remove();

		while (iter.hasNext())
		    {
			Literal lit = iter.next();
			int cmp = 
			    _literalOrdering.compare(cand,lit);
			
			if (cmp == ComparisonValue.Greater ||
			    cmp == ComparisonValue.Equivalent)
			    {
				iter.remove();
			    }
			else if (cmp == ComparisonValue.Smaller)
			    {
				// cand is definitely non-maximal
				unselected.add(cand);
				cand = lit;
				iter.remove();
				iter = lits.iterator();
			    };

		    }; // while (iter.hasNext())
		
		// cand is maximal:

		selected.add(cand);
		
	    }; // while (!lits.isEmpty())
	
    } // select(Collection<Literal> lits,..)


    private final LiteralOrdering _literalOrdering ;
    

  }; // class LitSelFunAllMaximalWRT
