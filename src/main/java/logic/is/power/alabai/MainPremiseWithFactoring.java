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

import logic.is.power.logic_warehouse.*;

/**
 * Extends {@link alabai_je.MainPremise} with the possibility of 
 * enumerating all factoring inferences between the active literal and 
 * one or more passive literals; this kind of premise object is 
 * primarily for use with the resolution rule; potentially, it
 * may also be used with other rules, such as transitive chaining.
 */
public class MainPremiseWithFactoring extends MainPremise {

    public
    MainPremiseWithFactoring(NewClauseAssembler residualLiteralAssembler) {
	super(residualLiteralAssembler); 
	_factoredPassiveLiterals = new LinkedList<Integer>(); 
	_localSubstitution = new Substitution1();
	_unificationCapsule = new Vector<UnificationCapsule>();
    }


    /** Makes the clause the current premise with the specified
     *  active literal.
     */
    public void load(Clause cl,Literal activeLiteral) {

	super.load(cl,activeLiteral);
    
	// Make sure that _unificationCapsule.size() is sufficient:

	for (int n = _unificationCapsule.size();
	     n < cl.totalNumberOfLiterals();
	     ++n)
	{
	    _unificationCapsule.add(new UnificationCapsule());
	};

	// Rename variables in the active literal:

	_literalAssembler.reset();
	_literalAssembler.pushLiteral(activeLiteral.isPositive(),
				      activeLiteral.atom(),
				      _variableRenaming);
	_literalAssembler.wrapUp();
    
	_activeLiteralFlatterm = _literalAssembler.assembledTerm();


    }
      
    /** Marks the end of using the current clause as the premise;
     *  performs some clean-up. 
     */
    public void unload() {

	super.unload();

	_activeLiteralFlatterm = null;

	for (int n = _factoredPassiveLiterals.size() - 1;
	     n >= 0;
	     --n)
	    _unificationCapsule.get(n).reverse();

	_factoredPassiveLiterals.clear();
	
    } // unload()



    /** Initiates a new round of enumeration of factoring inferences
     *  between the active literal and one or more passive literals.
     */
    public void resetFactoring() {

	for (int n = _factoredPassiveLiterals.size() - 1;
	     n >= 0;
	     --n)
	    _unificationCapsule.get(n).reverse();

	_factoredPassiveLiterals.clear();
	
    } // resetFactoring()
	

    /** Tries to make another factoring inference.
     *  @return <code>false</code> if all possible factoring inferences
     *          have been made
     */
    public boolean makeAnotherFactoringInference() {
	
	int passiveLitNum = 
	    (_factoredPassiveLiterals.isEmpty())? 
	    0 
	    : 
	    _factoredPassiveLiterals.getFirst() + 1;
	
    
	while (passiveLitNum < _numberOfPassiveLiterals)
	{
	    boolean unified = 
		_unificationCapsule.get(passiveLitNum).unify(_activeLiteralFlatterm,
							     _passiveLiterals.get(passiveLitNum),
							     _localSubstitution);
	    if (unified) 
	    {
		
		// New factoring inference. 
		
		_isResidual.set(passiveLitNum,false); 
                // the literal is considered resolved
		
		_factoredPassiveLiterals.addFirst(passiveLitNum);
		
		return true;
	    }
	    else
		++passiveLitNum;			 
	}; // while (passiveLitNum < _numberOfPassiveLiterals)



	// No more incremental factoring. Try to backtrack.


    
	while (!_factoredPassiveLiterals.isEmpty())
	{
	    // Undo the last factoring.
	    
	    passiveLitNum = _factoredPassiveLiterals.getFirst();

	    _unificationCapsule.get(passiveLitNum).reverse();

	    _isResidual.set(passiveLitNum,true);

	    _factoredPassiveLiterals.removeFirst();

	    ++passiveLitNum;

	    while (passiveLitNum < _numberOfPassiveLiterals)
	    {
		boolean unified = 
		    _unificationCapsule.get(passiveLitNum).unify(_activeLiteralFlatterm,
								 _passiveLiterals.get(passiveLitNum),
								 _localSubstitution);
		if (unified) 
		{
		    // New factoring inference. 
		
		    _isResidual.set(passiveLitNum,false); // the literal is considered resolved
		
		    _factoredPassiveLiterals.addFirst(passiveLitNum);
		
		    return true;
		}
		else
		    ++passiveLitNum;			 
	    }; // while (passiveLitNum < _numberOfPassiveLiterals)
	
	    // More backtracking is required.

	}; // while (_factoredPassiveLiterals.isEmpty())

    
	// Nowhere to backtrack.
	
	assert _localSubstitution.empty();
	
	return false;


    } // makeAnotherFactoringInference()



    public Flatterm activeLiteralFlatterm() { return _activeLiteralFlatterm; }
      
    

    //                      Data:

    private Flatterm _activeLiteralFlatterm;

    private LinkedList<Integer> _factoredPassiveLiterals;

    /** Registers variable instantiations made locally,
     *  ie, within this object.
     */
    private Substitution1 _localSubstitution;

    private Vector<UnificationCapsule> _unificationCapsule;

}; // class MainPremiseWithFactoring
