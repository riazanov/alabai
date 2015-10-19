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
 * Specialises {@link alabai_je.SidePremise} for keeping
 * premises of paramodulation that contain the positive equality.
 */
class SidePremiseWithPosEqForParamodulation extends SidePremise {

    public 
	SidePremiseWithPosEqForParamodulation(NewClauseAssembler conclusionAssembler)
    {
	super(conclusionAssembler);
	
    }

    /** Makes the clause the current premise with the specified
     *  active (positive equality) literal and the number of the argument
     *  to serve as the LHS.
     */    
    public 
	void 
	load(Clause cl,Literal activeLiteral,int lhsArgumentNumber) {
	
	assert activeLiteral.isPositive();
	assert activeLiteral.isEquality();
	assert lhsArgumentNumber == 0 || lhsArgumentNumber == 1;
	
	super.load(cl,activeLiteral);

	_literalAssembler.reset();
	_literalAssembler.pushLiteralWithGlobSubst(true,
						   activeLiteral.atom());
	_literalAssembler.wrapUp();
    
	_activeLiteralFlatterm = _literalAssembler.assembledTerm();

	Term lhs;

	if (lhsArgumentNumber == 0)
	    {
		lhs = activeLiteral.atom().firstArg();
		_lhsFlatterm = _activeLiteralFlatterm.nextCell();
		_rhsFlatterm = _lhsFlatterm.after();
	    }
	else
	    {
		lhs = activeLiteral.atom().secondArg();
		_rhsFlatterm = _activeLiteralFlatterm.nextCell();
		_lhsFlatterm = _rhsFlatterm.after();
	    };

	_lhsVariableSet = new HashSet<Variable>();
	lhs.collectFreeVariables(_lhsVariableSet);

    } // load(Clause cl,Literal activeLiteral,int lhsArgumentNumber)



    /** Marks the end of using the current clause as the premise;
     *  performs some clean-up. 
     */
    public void unload() {

	super.unload();
	_lhsVariableSet = null;
    }

    /** Active literal (after instantiation with global substitution 1). */ 
    public final Flatterm activeLiteralFlatterm() {
	return _activeLiteralFlatterm;
    }
	
    
    /** LHS of the active literal (after instantiation with global substitution 1). */ 
    public final Flatterm lhsFlatterm() { return _lhsFlatterm; }
    
    /** RHS of the active literal (after instantiation with global substitution 1). */ 
    public final Flatterm rhsFlatterm() { return _rhsFlatterm; }

    /** Free variables of the LHS, ignoring the instantiation. */
    public final Set<Variable> lhsVariableSet() {
	return _lhsVariableSet;
    }
    

    //                  Data:
	
    private Flatterm _activeLiteralFlatterm;
	
    private Flatterm _lhsFlatterm;

    private Flatterm _rhsFlatterm;

    private Set<Variable> _lhsVariableSet;

} // class SidePremiseWithPosEqForParamodulation