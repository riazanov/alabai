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
 * Representation for literals as elements of persistent clauses.
 * Instances are not meant to be shared, although the formulas representing
 * bodies of the literals are completely shared.
 */
public class Literal extends logic.is.power.logic_warehouse.Literal {

    
    public Literal(Formula body) {
	_body = body;
	_properties = new LiteralProperties();
    }



    public final boolean isNegative() { 
	return _body.isNegative(); 
    }

    /** @return atom().isEquality() */
    public final boolean isEqualityLiteral() {
	return atom().isEquality();
    }

    

    /** Checks that the literal is neither an answer literal nor 
     *  a constraint literal.
     */
    public final boolean isRegular() {
	return !isAnswer() && !isConstraintLiteral();
    }

    public final boolean isAnswer() {
	return atom().isAtomic() &&
	    PredicateInfo.isAnswer(((AtomicFormula)atom()).predicate());
    }

    public final boolean isConstraintLiteral() {
	return atom().isAtomic() &&
	    PredicateInfo.
	    isConstraintPredicate(((AtomicFormula)atom()).predicate());
    }

    public final LiteralProperties properties() { return _properties; }

    public final Formula atom() { return _body.atom(); }
    
    public final boolean isLinear() {
	return atom().isAtomic() &&
	    ((AtomicFormula)atom()).isLinear();
    }

    /** The whole literal as a formula; may create the formula structure,
     *  if necessary.
     */
    public final Formula body() { return _body; }
    
    
    /** @return <code>properties().selectionStatus()</code> */
    public final int selectionStatus() {
	return _properties.selectionStatus();
    }

    /** Marks this literal as necessarily selected. */
    public final void markAsSelected() {
	_properties.setSelectionStatus(LiteralProperties.SelectionStatus.Selected);
    }
    
    /** Marks this literal as possibly unselected. */
    public final void markAsPossiblyUnselected() {
	_properties.setSelectionStatus(LiteralProperties.SelectionStatus.PossiblyUnselected);
    }

    /** Marks this literal as necessarily unselected. */
    public final void markAsUnselected() {
	_properties.setSelectionStatus(LiteralProperties.SelectionStatus.Unselected);
    }

    public final boolean isUnselected() {
	return
	    _properties.selectionStatus() == 
	    LiteralProperties.SelectionStatus.Unselected;
    }

    public final boolean isPossiblyUnselected() {
	return
	    _properties.selectionStatus() == 
	    LiteralProperties.SelectionStatus.PossiblyUnselected;
    }
    
    /** Computes depth of the literal; 
     *  all logic symbols, including the top level negation, if the literal
     *  is negative, and the abstraction construct add 1 to the depth;
     *  pair constructors do not contribute to the depth count.
     */
    public int depth() {
	return _body.depth();
    }

    /** Counts all symbols including logical symbols and variables. */
    public final int numberOfSymbols() {
	return _body.numberOfSymbols();
    }
    
    /** Counts all nonvariable symbols <code>sym</code>, 
     *  including logical symbols, but excluding pair constructors
     *  and abstraction operators, such that 
     *  <code>sym.category(modulus) == category</code>.
     */
    public 
	final 
	int 
	numberOfNonvariableSymbolsFromCategory(int category,
					       int modulus) {
	return 
	    _body.numberOfNonvariableSymbolsFromCategory(category,
							 modulus);
    }
    
    
    /** Counts all nonvariable symbols <code>sym</code>, 
     *  including logical symbols, but excluding pair constructors
     *  and abstraction operators, such that 
     *  <code>sym.category(modulus) == category</code>,
     *  for every <code>category</code> in <code>[0,modulus - 1]</code>,
     *  and adds the numbers to the corresponding values in 
     *  <code>result[category]</code>.
     */
    public 
	final 
	void 
	addNumberOfNonvariableSymbolsFromCategories(int modulus,
						    int[] result) {
	
	_body.
	    addNumberOfNonvariableSymbolsFromCategories(modulus,
							result);
	
    } // addNumberOfNonvariableSymbolsFromCategories(int modulus,
    


    public final void collectFreeVariables(Collection<Variable> result) {
	_body.collectFreeVariables(result);
    }


    public final String toString() { return toString(false); }

    public final String toString(boolean closed) {	

	String result = "";

	if (isNegative() && isEquality())
	    {
		// Special case: negative equality
		
		Term atomArg = ((AtomicFormula)atom()).argument();
		
		assert atomArg.isPair();
		
		Term eqArg1 = ((TermPair)atomArg).first();
		Term eqArg2 = ((TermPair)atomArg).second();
		
		if (closed) result += "(";
		result += eqArg1 + " != " + eqArg2;
		if (closed) result += ")";
		
		return result;
	    }
	else 
	    {
		boolean constr = isConstraintLiteral();
		if (constr) result += "<< ";
		result += _body.toString(closed && !constr);
		if (constr) result += " >>";
		return result;
	    }
		
    } // toString(boolean closed)

      

    //                   Data:

    private final Formula _body;
    
    private final LiteralProperties _properties;
    
}; // class Literal
