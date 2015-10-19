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
 * Represents selection units carrying clauses which can potentially produce
 * equality resolution inferences.
 */
class FinSelUnEqualityResolution extends FineSelectionUnit {

    public FinSelUnEqualityResolution(Clause clause) {
	super(FineSelectionUnit.Kind.EqualityResolution,clause);
    }
    

    public String toString() {
	return "#[" + number() + "] " + spell(kind()) + 
	    " cl=" + clause().number() +
	    " pen=" + penalty();
    }

}; // class FinSelUnEqualityResolution