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



import logic.is.power.logic_warehouse.*;


/**
 * Flatterm-based representation of literals intended to support 
 * efficient forward processing; apart from the flatterm body of 
 * a literal, a <code>TmpLiteral</code> object contains additional information
 * about the literal.
 * See also {@link alabai_je.NewClause}.
 */
public class TmpLiteral extends logic.is.power.logic_warehouse.FlattermLiteral {

    public TmpLiteral(Flatterm body) {
	_body = body;
	_isNegative = 
	    (_body.isConnectiveApplication() &&
	     _body.connective().id() == Connective.Id.Not &&
	     _body.nextCell().isAtomicFormula());
    }

    
    /** Checks if the literal is an 
 
     * <a href="{@docRoot}/../glossary.html#ordinary_literal">ordinary</a>
     * negative literal, ie, negation of an atom;
     * polarity may be flipped with {@link #flipPolarity()}.
     */
    public final boolean isNegative() { 
	return _isNegative;
    }
    
    public final void flipPolarity() { _isNegative = !_isNegative; }


    /** Checks if the literal is simple, ie, an atom or negation
     *  of an atom.
     */
    public final boolean isSimple() {
      return _body.isAtomicFormula() ||
	(_body.isConnectiveApplication() &&
	_body.connective().id() == Connective.Id.Not &&
	_body.nextCell().isAtomicFormula());
    }


    public final boolean isAnswer() {
	return isSimple() && 
	    PredicateInfo.isAnswer(atom().predicate()); 
    }

    public final boolean isConstraintLiteral() {
	return isSimple() && 
	    PredicateInfo.isConstraintPredicate(atom().predicate()); 
    }

    /** The underlying flatterm representation of the whole literal. */
    public final Flatterm body() {
	if (_isNegative == 
	    (_body.isConnectiveApplication() &&
	     _body.connective().id() == Connective.Id.Not &&
	     _body.nextCell().isAtomicFormula()))
	    // Polarity has the original value.
	    return _body; 
	
	// Polarity is flipped. 
	
	if (!_isNegative) return atom();
	
	// Negation has to be added:

	Flatterm result = 
	    Flatterm.newConnectiveApplicationCell(Connective.getNot());
	
	result.setNextCell(atom());
	result.setLastCell(atom().lastCell());

	return result;

    } // body()



    /** <b>pre:</b> not necessarily <code>isSimple()</code>! */
    public final Flatterm atom() {
      return 
	  (_body.isAtomicFormula() || !isSimple())? _body : _body.nextCell();
    }
    
    /** <b>pre:</b> <code>isSimple()</code> */
    public final Predicate predicate() {
	assert isSimple();
	return atom().predicate();
    }
    
    /** Counts all symbols, including logic symbols and variables;
     *  an abstraction operator is counted as 1 symbol.
     */
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
    





    
    /** Instantiates <code>var</code> in this literal 
     *  by (copies of) <code>instance</code>.
     */
    public 
	final 
	void instantiate(Variable var,Flatterm instance) {

	_body.instantiate(var,instance);
    } 	



    public String toString() {
	String result = "";
	boolean constr = isConstraintLiteral();
	if (constr) result += "<< ";
	result += _body.toString();
	if (constr) result += " >>";
	return result;
    }


    //                           Data:

    private Flatterm _body;

    /** Current polarity of the literal; need not correspond to the polarity 
     *  of _body.
     */
    private boolean _isNegative;

} // class TmpLiteral
