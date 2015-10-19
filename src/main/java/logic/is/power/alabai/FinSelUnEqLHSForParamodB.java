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



/**
 * Represents selection units carrying an argument of a positive equality
 * literal to be used as the LHS of the equality in 
 * <a href="{@docRoot}/../glossary.html#paramodulation_from_into">paramodulation inferences from</a>
 * it in which the unifier is 
 * <a href="{@docRoot}/../glossary.html#bidirectional_unifier">bidirectional</a>.
 */
class FinSelUnEqLHSForParamodB extends FinSelUnEqLHSForParamod {


    /** <b>pre:</b> literal is a positive equality <code> &&
     *	(argumentNumber == 0 || argumentNumber == 1) </code>.
     */
    public FinSelUnEqLHSForParamodB(Clause clause,
				    Literal literal,
				    int argumentNumber) {
	super(FineSelectionUnit.Kind.EqLHSForParamodB,
	      clause,
	      literal,
	      argumentNumber);
    }
    

}; // class FinSelUnEqLHSForParamodB
