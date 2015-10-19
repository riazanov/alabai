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
 * Represents selection units carrying 
 * <a href="{@docRoot}/../glossary.html#candidate_redex">cadidate redexes</a>
 * for paramodulation inferences regardless of the unifier type.
 * Note that selection units of this type can interact
 * with {@link alabai_je.FinSelUnEqLHSForParamodI}, 
 * {@link alabai_je.FinSelUnEqLHSForParamodG}
 * and {@link alabai_je.FinSelUnEqLHSForParamodB} units to produce inferences.
 * @param clause
 * @param literal
 * @param redexPosition position of the redex in the literal's atom
 */
class FinSelUnRedexForParamod extends FineSelectionUnitWithActiveness {

    public FinSelUnRedexForParamod(Clause clause,
				   Literal literal,
				   int redexPosition) {
	super(FineSelectionUnit.Kind.RedexForParamod,clause);
	_literal = literal;
	_redexPosition = redexPosition;
    }

    /** The literal of which the redex is a part. */
    public Literal literal() { return _literal; }

    /** Position of the redex in atom of the literal. */
    public int redexPosition() { return _redexPosition; }


    public Term redex() { 
	return _literal.atom().subtermInPosition(_redexPosition);
    }

    public String toString() {
	  
	return "#[" + number() + "] " + spell(kind()) + 
	    " lit=(" + literal() + ")" +
	    " subterm#" + redexPosition() + 
	    "=(" + redex() + ")" +
	    " cl=" + clause().number() +
	    " pen=" + penalty() + " act=" + activeness();
    }


    //                           Data:
    
    private Literal _literal;
    
    /** Position of the redex term in the literal's atom. */
    private int _redexPosition;

}; // class FinSelUnRedexForParamod





