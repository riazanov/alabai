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
 * Common base for classes representing
 * <a href="{@docRoot}/../glossary.html#main_premise">main premises</a>
 * in various deduction rules, such as resolution or paramodulation;
 * the main purpose of this class is to keep
 * <a href="{@docRoot}/../glossary.html#passive_literals">passive literals</a>
 * in some representation convenient for factoring and for identifying 
 * <a href="{@docRoot}/../glossary.html#residual_literals">residual literals</a>
 * and dumping them in the corresponding inference result assembler.
 * <b>IMPORTANT:</b> When a clause is loaded in this object, all variables
 *            are renamed as variables from a local variable bank;
 *            the corresponding renaming object and the variable bank
 *            are accessible for derived classes as 
 *            <code>_variableRenaming</code> and <code>_variableBank</code>
 *            respectively.
 */
class MainPremise {

    /** Clause currently represented by this premise object. */
    public final Clause clause() { return _clause; }
    

    /** All variables occuring in the active literal (ignoring
     *  instantiation).
     */
    public final Set<Variable> activeLiteralVariableSet() {	
	return _activeLiteralVariableSet;
    }
    

    

    //                      Protected methods:

      
    protected MainPremise(NewClauseAssembler residualLiteralAssembler) {
	_residualLiteralAssembler = residualLiteralAssembler;
	_passiveLiterals = new Vector<Flatterm>(32,32);
	_isResidual = new Vector<Boolean>(32,32);
	_variableBank = new Variable.Bank();
	_variableRenaming = new VariableRenaming(); 
	_literalAssembler = new FlattermAssembler(); 
	_activeLiteralVariableSet = new HashSet();
    }

    /** Makes the clause the current premise with the specified
     *  active literal.
     */
    protected void load(Clause cl,Literal activeLiteral) {
  
	_clause = cl;

	_numberOfPassiveLiterals = cl.totalNumberOfLiterals() - 1;

	_variableBank.reset();
				
	_variableRenaming.reset(_variableBank);

	_passiveLiterals.setSize(_numberOfPassiveLiterals);

	_isResidual.setSize(_numberOfPassiveLiterals);

	int n = 0;
	for (Literal lit : cl.literals())
	    if (lit != activeLiteral)
	    {
		_literalAssembler.reset();
		_literalAssembler.pushLiteral(lit.isPositive(),
					      lit.atom(),
					      _variableRenaming);
		_literalAssembler.wrapUp();

		_passiveLiterals.set(n,_literalAssembler.assembledTerm());
      
		_isResidual.set(n,true);
		
		++n;
	    };

	

	LinkedList<Variable> originalActiveLiteralVariables = 
	    new LinkedList<Variable>();

	activeLiteral.atom().
	    collectFreeVariables(originalActiveLiteralVariables);
	
	// Rename the variables: 

	_activeLiteralVariableSet.clear();

	for (Variable var : originalActiveLiteralVariables)
	    _activeLiteralVariableSet.add(_variableRenaming.rename(var));


    } // load(Clause cl,Literal activeLiteral)

    /** Marks the end of using the current clause as the premise;
     *  performs some clean-up. 
     */
    protected void unload() {

	_clause = null;

	_passiveLiterals.setSize(0);
	
	_variableRenaming.clear();

	_activeLiteralVariableSet.clear();
    }

    /** Dumps the @ref residual_literals "residual literals" 
     * <a href="{@docRoot}/../glossary.html#residual_literals">residual literals</a>
     *  in the {@link alabai_je.NewClauseAssembler} associated with this object.
     * <b>IMPORTANT:</b> This method does not open a new clause in 
     *             the {@link alabai_je.NewClauseAssembler}! This should
     *             be done externally before the method is called.
     */
    public void assembleResidualLiterals() {

	for (int n = 0; n < _numberOfPassiveLiterals; ++n)
	    if (_isResidual.get(n))
		_residualLiteralAssembler.
		    pushLiteralWithGlobSubstAndRenaming(_passiveLiterals.get(n));
    }




    //                            Data:
      

 
    private NewClauseAssembler _residualLiteralAssembler;

    private Clause _clause;

    protected Vector<Flatterm> _passiveLiterals;

    protected int _numberOfPassiveLiterals;

    /** Indicates which passive literals have not been resolved
     *  and should be treated as residual.
     */
    protected Vector<Boolean> _isResidual;
    
    private Variable.Bank _variableBank;
    
    protected VariableRenaming _variableRenaming;

    protected FlattermAssembler _literalAssembler;

    /** All variables occuring in the active literal (ignoring
     *  instantiation).
     */
    private HashSet<Variable> _activeLiteralVariableSet;

}; // class MainPremise
