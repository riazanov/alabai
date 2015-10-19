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



/** Common base for classes representing various kinds of
 *  selection units carrying the LHS of a positive equality
 *  intended for 
 * <a href="{@docRoot}/../glossary.html#paramodulation_from_into">paramodulation inferences from</a>
 *  it, such as {@link alabai_je.FinSelUnEqLHSForParamodI}, 
 *  {@link alabai_je.FinSelUnEqLHSForParamodG} and 
 *  {@link alabai_je.FinSelUnEqLHSForParamodB}.
 */
abstract class FinSelUnEqLHSForParamod extends FineSelectionUnitWithActiveness {

    /** <b>pre:<b> literal is a positive equality <code> &&
     *	(argumentNumber == 0 || argumentNumber == 1)</code>.
     */
    public FinSelUnEqLHSForParamod(int kind,
				   Clause clause,
				   Literal literal,
				   int argumentNumber) {
	super(kind,clause);

	assert literal.isPositive();
	assert literal.isEquality();
	assert argumentNumber == 0 || argumentNumber == 1;
	_literal = literal;
	_argumentNumber = argumentNumber;
    }

      

    public Literal literal() { return _literal; }

    public int argumentNumber() { return _argumentNumber; }


    public Term lhs() {
	return 
	    (_argumentNumber == 0)?
	    _literal.firstArg() 
	    :
	    _literal.secondArg(); 
    }

      
    public String toString() {
	return "#[" + number() + "] " + spell(kind()) + " eq=(" + literal() + ")" +
	    " arg=" + argumentNumber() + " cl=" + clause().number() +
	    " pen=" + penalty() + " act=" + activeness();
    }

      
    //                            Data:

    
    private Literal _literal;
    
    /** Number of the equality argument considered the LHS. */
    private int _argumentNumber;

}; // class FinSelUnEqLHSForParamod
