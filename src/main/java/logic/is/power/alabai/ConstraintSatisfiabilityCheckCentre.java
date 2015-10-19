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


/** Hub for checking the satisfiability of constraints in clauses
 *  for the purpose of pruning search space in query rewriting,
 *  based on Section 4 of 
 *  <a href="{@docRoot}/../references.html#incremental_query_rewriting">[Incremental Query Rewriting chapter]</a>.
 *  Currently, only simple and cheap special cases are implemented.
 */
public final class ConstraintSatisfiabilityCheckCentre {

    public ConstraintSatisfiabilityCheckCentre() {
	_discardClausesWithFunctionsInConstraintsFlag = true;
	_discardLongChainsInConstraintsFlag = true;
    }

    /** Switches the discarding of clauses with constraints containing 
     *  non-nullary functions; this is a query rewriting-related feature.
     */
    public void setDiscardClausesWithFunctionsInConstraints(boolean fl) {
	_discardClausesWithFunctionsInConstraintsFlag = fl;
    }    
    
    /** Switches the discarding of clauses with too long chains
     *  of transitive predicates in constraints;
     *  this is a query rewriting-related feature.
     */
    public void setDiscardLongChainsInConstraints(boolean fl) {
	_discardLongChainsInConstraintsFlag = fl;
    }
    

    
    /** Checks if the constraint part of the clause is unsatisfiable;
     *  the check is not complete -- a negative unswer does not necessarily 
     *  mean that the constraint is satisfiable in some sense. 
     */
    public boolean clauseHasUnsatisfiableConstraint(NewClause cl) {
	
	if (_discardClausesWithFunctionsInConstraintsFlag)
	    {
		for (TmpLiteral lit : cl.literals())
		    if (lit.isConstraintLiteral())
			{
			    Flatterm subterm = lit.atom();
			    Flatterm after = subterm.after();
			    subterm = subterm.nextCell();
			    while (subterm != after)
				{
				    int kind = subterm.kind();
				    if (kind == Term.Kind.CompoundTerm)
					return true;	
				    subterm = subterm.nextCell();
				};
			};
	    }; // if (_discardClausesWithFunctionsInConstraintsFlag)
	

	if (_discardLongChainsInConstraintsFlag &&
	    clauseHasTooLongChainInConstraint(cl))
	    {
		return true;
	    };

	return false;


    } // clauseHasUnsatisfiableConstraint(NewClause cl)



    private static boolean clauseHasTooLongChainInConstraint(NewClause cl) {

	Collection<TmpLiteral> allLiterals = cl.literals();
	
	int numOfAllLiterals = allLiterals.size();
	
	Iterator<TmpLiteral> iter = allLiterals.iterator();
	
	// This code is slightly optimised. We first try to quickly
	// check if there are suspicious literals, and only after
	// one is found, we do a similar loop with more tests.

	while (iter.hasNext())
	    {
		TmpLiteral lit = iter.next();
		if (lit.isConstraintLiteral())
		    {
			Predicate pred = lit.predicate();
			
			int limit =
			    PredicateInfo.constraintChainLengthLimit(pred);
			
			if (limit != 0 && 
			    limit <= numOfAllLiterals &&
			    lit.atom().firstArg().isVariable() &&
			    lit.atom().secondArg().isVariable())
			    {
				// At least one eligible literal.
				
				TreeMap<Predicate,LiteralArray> litArrays =
				    new TreeMap<Predicate,LiteralArray>();
				
				LiteralArray litArray = 
				    new LiteralArray(numOfAllLiterals);

				litArray.add(lit);				
				litArrays.put(pred,litArray);

				while (iter.hasNext())
				    {
					lit = iter.next();
					
					if (lit.isConstraintLiteral())
					    {
						pred = lit.predicate();
						
						limit =
						    PredicateInfo.
						    constraintChainLengthLimit(pred);
			
						if (limit != 0 && 
						    limit <= numOfAllLiterals &&
						    lit.atom().firstArg().isVariable() &&
						    lit.atom().secondArg().isVariable())
						    {
							litArray = litArrays.get(pred);
							if (litArray == null)
							    {
								litArray = new LiteralArray(numOfAllLiterals);
								litArrays.put(pred,litArray);
							    };	
							litArray.add(lit);
						    };
					    };

				    }; // while (iter.hasNext())

				// All suspicious literals are collected
				// in litArrays

				for (Map.Entry<Predicate,LiteralArray> entry :
					 litArrays.entrySet())
				    {
					pred = entry.getKey();
					limit = 
					    PredicateInfo.constraintChainLengthLimit(pred);
					if (limit > entry.getValue().size)
					    continue;
					
					if (containsChainLongerThan(entry.getValue().literals,
								    0,
								    entry.getValue().size,
								    limit))
					    return true;


				    }; // for (Map.Entry<Predicate,LiteralArray> entry :
				return false;

			    }; // if (limit != 0 && limit <= numOfAllLiterals)
		    };
	    }; // while (iter.hasNext())
	
	return false;

    } // clauseHasTooLongChainInConstraint(NewClause cl)


    private static boolean containsChainLongerThan(TmpLiteral[] literals,
						   int begin,
						   int end,
						   int limit) {

	assert limit > 0;
	assert begin < end;
	
	for (int i = begin; end - i > limit; ++i)
	    {	
		Flatterm atom = literals[i].atom();
	
		Variable var1 = atom.firstArg().variable();
		Variable var2 = atom.secondArg().variable();
		
		int outChainLength = 
		    outChainLength(var2,literals,i + 1,end,limit - 1);
		
		if (outChainLength >= limit) return true;
		
		if (inChainLengthReaches(var1,
					 literals,
					 i + 1,
					 end,
					 limit - outChainLength))
		    return true;
	    }; // for (int i = begin; end - i >= limit; ++i)
	
	return false;

    } // containsChainLongerThan(TmpLiteral[] literals,


    private static int outChainLength(Variable var,
				      TmpLiteral[] literals,
				      int begin,
				      int end,
				      int limit) {	

	int result = 0;
	
	for (int i = begin; i < end; ++i)
	    {
		if (var == literals[i].atom().firstArg().variable())
		    {	
			if (limit == 0) return 1;

			Variable var2 = 
			    literals[i].atom().secondArg().variable();
	
			int canResult = 
			    1 + outChainLength(var2,
					       literals,
					       begin,
					       end,
					       limit - 1);
			if (canResult > result) result = canResult;
		    };

	    }; // for (int i = begin; i < end; ++i)
	
	return result;

    } // outChainLength(Variable var,

    
    
    private static boolean inChainLengthReaches(Variable var,
						TmpLiteral[] literals,
						int begin,
						int end,
						int limit) {
	assert limit > 0;
	
       
	for (int i = begin; i < end; ++i)
	    {
		if (var == literals[i].atom().secondArg().variable())
		    {	
			if (limit == 1) return true;

			Variable var1 = 
			    literals[i].atom().firstArg().variable();
							
			if (inChainLengthReaches(var1,
						 literals,
						 begin,
						 end,
						 limit - 1))
			    return true;
		    };

	    }; // for (int i = begin; i < end; ++i)

	return false;

    } // inChainLengthReaches(Variable var,


    private static class LiteralArray {

	public LiteralArray(int capacity) {
	    literals = new TmpLiteral[capacity];
	    size = 0;
	}
	
	public void add(TmpLiteral lit) {
	    literals[size] = lit;
	    ++size;
	}

	public TmpLiteral[] literals;
	
	public int size;

    } // class LiteralArray
    

    //                        Data:

    private boolean _discardClausesWithFunctionsInConstraintsFlag;
	
    private boolean _discardLongChainsInConstraintsFlag;


} // class ConstraintSatisfiabilityCheckCentre