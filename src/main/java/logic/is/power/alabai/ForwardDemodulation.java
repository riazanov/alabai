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


/** Encapsulates all forward demodulation, including the index. */
final class ForwardDemodulation {

    public ForwardDemodulation() {
	_index = new ForwardDemodulationIndex();
	_rhsInstanceAssembler = new FlattermAssembler(); 
    }
    
    public void setTermFeatureFunctionVector(TermFeatureVector vector) {
	_index.setFeatureFunctionVector(vector);

	_retrieval = _index.new Retrieval();
	// This is not in the constructor because a retrieval object
	// can only be created after the feature function vector is set.
    }

    public final void insert(Clause cl) {
	_index.insert(cl);
    }
    
    public final boolean remove(Clause cl) {
	return _index.remove(cl);
    }

    public final boolean simplify(NewClause cl) {
	
	boolean result = false;

	for (TmpLiteral lit : cl.literals())
	    if (simplify(lit,cl)) result = true;

	return result;
    } 
    
    private
	final 
	boolean simplify(TmpLiteral lit,NewClause cl) {
	
	boolean result = false;

	for (Flatterm arg = lit.atom().nextCell();
	     arg != lit.atom().after();
	     arg = arg.after())
	    {
		if (simplify(arg,lit,cl)) result = true;
	    };

	return result;

    } // simplify(TmpLiteral lit,NewClause cl)

    
    
    private 
	boolean 
	simplify(Flatterm redex,TmpLiteral lit,NewClause cl) {

                 assert redex != null;
        
	boolean result = false;

	boolean  passSuccessfull;
       
	Ref<Flatterm> redexReplacement = new Ref<Flatterm>();

	do
	    {
		passSuccessfull = false;

		if (redex.isCompound()) {
		    
                                       Flatterm after = redex.after();
                                       
		    // Try to completely simplify the arguments:
		    for (Flatterm arg = redex.nextCell();
			 arg != after;
			 arg = arg.after())
			{
			    if (simplify(arg,lit,cl)) 
				passSuccessfull = true;
			};
		  
                        
                        
		    // Try to simplify the redex at the top position:
		    if (rewrite(redex,lit,cl,redexReplacement)) 
			{

			    //System.out.println("REW " + redex + " --> " + redexReplacement.content);

			    passSuccessfull = true;
			    redex = redexReplacement.content;
			};
		}
		else if (redex.isIndividualConstant()) {
		    if (rewrite(redex,lit,cl,redexReplacement)) 
			{
			    passSuccessfull = true;
			    redex = redexReplacement.content;
			};
		}; // if (redex.isCompound())
	
		if (passSuccessfull) result = true;
	    }
	while (passSuccessfull);
	
	return result;

    } // simplify(Flatterm redex,TmpLiteral lit,NewClause cl)
	


    /** <b>pre:</b>
     *   <code>redex.isCompound() || redex.isIndividualConstant()</code>
     */
    private 
	boolean 
	rewrite(Flatterm redex,
		TmpLiteral lit,
		NewClause cl,
		Ref<Flatterm> replacement) {

	_retrieval.reset(redex);

	while (_retrieval.hasNext())
	    {

		Triple<Clause,Literal,Term> rewriteRule =
		    _retrieval.next();
		
		Clause clause = rewriteRule.first;
		
		assert clause.literals().size() == 1;
		
		Literal equation = rewriteRule.second;
		
		assert equation.isPositive();
		assert equation.isEquality();
		
		Term lhs = rewriteRule.third;
		
		assert equation.atom().firstArg() == lhs ||
		    equation.atom().secondArg() == lhs;
		

		assert redex.equalsModuloSubst2(lhs);
		
		Term rhs =
		    (equation.atom().firstArg() == lhs)?
		    equation.atom().secondArg()
		    :
		    equation.atom().firstArg();

		assert 
		    ReductionOrdering.current().canBeGreaterModuloSubst(lhs,rhs);

		// Check that that global substitution 2 orients
		// the equation properly:
		
		//System.out.println("GREATER? " + lhs + " > " + rhs + "  MOD " + _retrieval.witnessSubstitution());

		if (ReductionOrdering.current().greaterModuloSubst2(lhs,rhs))
		    {
			_rhsInstanceAssembler.reset();
			_rhsInstanceAssembler.pushTermWithGlobSubst2(rhs);
			_rhsInstanceAssembler.wrapUp();
			
			lit.body().replace(redex,
					   _rhsInstanceAssembler.assembledTerm());
			
			replacement.content = _rhsInstanceAssembler.assembledTerm();
			cl.addInference(InferenceType.ForwardDemodulation);
			cl.addParent(clause);
			
			
			++(Statistics.current().
			   forwardSimplifyingInferences.demodulation);

			_retrieval.clear();
			
			return true;

		    }; // if (ReductionOrdering.current().greaterModuloSubst2(lhs,rhs))

	    }; // if (result)

	_retrieval.clear();

	return false;

    } // rewrite(Flatterm redex,TmpLiteral lit,NewClause cl,..)


    //                Data:

    private final ForwardDemodulationIndex _index;

    private ForwardDemodulationIndex.Retrieval _retrieval;

    private final FlattermAssembler _rhsInstanceAssembler;
    
} // class ForwardDemodulation