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


/** Info associated with predicates. */
public class PredicateInfo {

    /** <b>post:</b> 
     *    <code>isAnswer(pred) && isConstraintPredicate(pred)</code>.
     *  Note that answer predicates are considered a special case
     *  of constraint predicates.
     */
    public static void markAsAnswer(Predicate pred) {
	if (pred.info() == null)
	    pred.setInfo(new PredicateInfo());
	((PredicateInfo)pred.info())._isAnswer = true;
	((PredicateInfo)pred.info())._isConstraintPredicate = true;
    }

    /** Note that if the result is positive, the predicate is also
     *  a constraint predicate.
     */
    public static boolean isAnswer(Predicate pred) {
	return pred.info() != null &&
	    ((PredicateInfo)pred.info())._isAnswer;
    }


    /** <b>post:</b> <code>isConstraintPredicate(pred)</code> */
    public static void markAsConstraintPredicate(Predicate pred) {
	if (pred.info() == null)
	    pred.setInfo(new PredicateInfo());
	((PredicateInfo)pred.info())._isConstraintPredicate = true;
    }


    public static boolean isConstraintPredicate(Predicate pred) {
	return pred.info() != null &&
	    ((PredicateInfo)pred.info())._isConstraintPredicate;
    }

    /** Assigns the limit on the length of chains in constraints with
     *  this binary predicate; lim=0 removes the limit.
     */
    public static void setConstraintChainLengthLimit(Predicate pred,int lim) {
	if (lim != 0 && pred.arity() != 2)
	    throw new IllegalArgumentException("Chain limit is set on a non-binary predicate " + pred);
	if (pred.info() == null)
	    pred.setInfo(new PredicateInfo());
	((PredicateInfo)pred.info())._constraintChainLengthLimit = lim;
    }

    
    public static int constraintChainLengthLimit(Predicate pred) {
	if (pred.info() == null) return 0;
	return ((PredicateInfo)pred.info())._constraintChainLengthLimit;
    }


    //             Private methods:

    protected PredicateInfo() {
	_isAnswer = false;
	_isConstraintPredicate = false;
        _constraintChainLengthLimit = 0;
    }

    //              Data:

    private boolean _isAnswer;

    private boolean _isConstraintPredicate;

    /** Limit on the length of chains in constraints with this predicate;
     *  0 if there is no limit.
     */
    private int _constraintChainLengthLimit;

} // class PredicateInfo 