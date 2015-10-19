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


/** Implements simplification of new clauses by trivial
 *  or safe destructive equality resolution.
 *  TODO: Safe destructive equality resolution to be implemented.
 */
class SimplifyingEqualityResolution {

    public SimplifyingEqualityResolution() {
    }


    public final boolean simplify(NewClause cl) {

	// Only trivial equality resolution is currently implemented.

	boolean result = false;
	
	while (performDistructiveEqualityResolution(cl))
	    result = true;
	
	Iterator<TmpLiteral> iter = cl.literals().iterator();
	
	while (iter.hasNext())
	    {
		TmpLiteral lit = iter.next();
		
		if (lit.isEquality() && lit.isNegative())
		    {
			Flatterm arg1 = lit.atom().firstArg();
			Flatterm arg2 = lit.atom().secondArg();
			if (arg1.wholeTermEquals(arg2))
			    {
				// trivial equality resolution
				iter.remove();
				result = true;
			    };

		    }; // if (lit.isEquality() && lit.isNegative())

	    }; // while (iter.hasNext())

	while (performDistructiveEqualityResolution(cl))
	    result = true;
	
	return result;
	
    } // simplify(NewClause cl)


    
    private final boolean performDistructiveEqualityResolution(NewClause cl) {

	Iterator<TmpLiteral> iter = cl.literals().iterator();
	
	while (iter.hasNext())
	    {				
		TmpLiteral lit = iter.next();
		
		if (lit.isEquality() && lit.isNegative())
		    {
			Flatterm arg1 = lit.atom().firstArg();
			Flatterm arg2 = lit.atom().secondArg();
			if (arg1.isVariable() && 
				 !arg2.containsVariable(arg1.variable()))
			    {
				// Destructive equality resolution: 
				// instantiate arg1.variable() with arg2
				
				iter.remove();

				iter = cl.literals().iterator();
				while (iter.hasNext())
				    {				
					lit = iter.next();
					lit.instantiate(arg1.variable(),arg2);
				    };

				return true;
			    }
			else if (arg2.isVariable() && 
				 !arg1.containsVariable(arg2.variable()))
			    {				
				// Destructive equality resolution: 
				// instantiate arg2.variable() with arg1
				
				iter.remove();
				
				iter = cl.literals().iterator();
				while (iter.hasNext())
				    {				
					lit = iter.next();
					lit.instantiate(arg2.variable(),arg1);
				    };
				
				return true;				
			    };
		    }; // if (lit.isEquality() && lit.isNegative())
			
	    }; // while (iter.hasNext())
	
	return false;
	
    } // performDistructiveEqualityResolution(NewClause cl)
	




} // class SimplifyingEqualityResolution