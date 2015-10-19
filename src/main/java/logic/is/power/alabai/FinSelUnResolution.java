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



/** Common base for classes representing various kinds of
 *  selection units carrying a literal intended
 *  for resolution inferences, such as {@link alabai_je.FinSelUnResolutionI},
 *  {@link alabai_je.FinSelUnResolutionG} and
 *  {@link alabai_je.FinSelUnResolutionB}.
 */
abstract class FinSelUnResolution extends FineSelectionUnitWithActiveness {

    public FinSelUnResolution(int kind,
			      Clause clause,
			      Literal literal) {
	super(kind,clause);
	_literal = literal;
	assert kind == FineSelectionUnit.Kind.ResolutionI ||
	    kind == FineSelectionUnit.Kind.ResolutionG ||
	    kind == FineSelectionUnit.Kind.ResolutionB;
    }
      
    public Literal literal() { return _literal; }
      
    public String toString() {
	return "#[" + number() + "] " + spell(kind()) + 
	    " lit=(" + literal() + ")" +
	    " cl=" + clause().number() + " pen=" + penalty() +
	    " act=" + activeness();
    }


    //                          Data:
    
    private Literal _literal;
    
}; // class FinSelUnResolution
