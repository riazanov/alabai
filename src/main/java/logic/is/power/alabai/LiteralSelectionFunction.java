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


import java.util.Collection;

import java.util.LinkedList;

import logic.is.power.cushion.*;




/**
 * Common interface for classes representing various literal 
 * selection functions.
 */
public interface LiteralSelectionFunction {
      
    /** Distributes the literals from <code>lits</code> between the two
     *  other lists; note that <code>lits</code> may change in any way.
     *	@param lits
     *  @param selected where the selected literals are saved
     *  @param unselected where the unselected literals are saved;
     *         note that "unselected" here means 
     *         "does not have to be selected" rather than
     *         "cannot be selected"
     */
    public void select(Collection<Literal> lits,
		       Collection<Literal> selected,
		       Collection<Literal> unselected);

}; // class LiteralSelectionFunction