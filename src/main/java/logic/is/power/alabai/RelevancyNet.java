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

import logic.is.power.cushion.*;

import logic.is.power.logic_warehouse.*;


/** Data structure representing a set of clauses, that supports
 *  the identification of clauses that are relevant wrt some specified
 *  goal clauses.
 */
class RelevancyNet {

    public RelevancyNet() {
    }

    /** Registers the clause as a theory clause. 
     *  @param cl must satisfy 
     *         <code>cl.isDerivedExclusivelyFromAxioms()</code>
     */
    public final void registerTheoryClause(Clause cl) {
    }
    
    /** Marks the end of theory clause registration. */
    public final void endOfTheoryClauses() {
    }

    

} // RelevancyNet
