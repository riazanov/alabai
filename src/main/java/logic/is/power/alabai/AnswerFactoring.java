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


/** Implements answer factoring inferences (as a kind of simplifying
 *  forward inferences).
 */
class AnswerFactoring {

    /** @return false if there is an unfactorable pair of answer literals */ 
    public static boolean factor(NewClause cl) {
	
	// Find first answer literal:

	TmpLiteral mainAnswerLit = null;

	Iterator<TmpLiteral> iter = cl.literals().iterator();
	
	while (iter.hasNext())
	    {
		mainAnswerLit = iter.next();
		if (mainAnswerLit.isAnswer())
		    break;
		mainAnswerLit = null;
	    };
	
	LinkedList<TmpLiteral> newLiterals = 
	    new LinkedList<TmpLiteral>();
	
	while (iter.hasNext())
	    {
		TmpLiteral lit = iter.next();

		if (lit.isAnswer())
		    {
			if (!factor(mainAnswerLit,lit,newLiterals))
			    return false;
			iter.remove();
		    };
	    };
	
	cl.literals().addAll(newLiterals);

	return true;

    } // factor(NewClause cl)



    private static boolean factor(TmpLiteral mainAnswerLit,
				 TmpLiteral lit,
				 LinkedList<TmpLiteral> newLiterals) {
	assert mainAnswerLit.isAnswer();
	assert lit.isAnswer();

	if (!mainAnswerLit.predicate().equals(lit.predicate()) ||
	    mainAnswerLit.isNegative() != lit.isNegative())
	    return false;

	Flatterm arg1 = mainAnswerLit.atom().nextCell();
	Flatterm arg2 = lit.atom().nextCell();
	
	for (int n = 0; n < mainAnswerLit.predicate().arity(); ++n)
	    {
		if (!arg1.wholeTermEquals(arg2))
		    {
			// Add arg1 != arg2 as a new literal:
			newLiterals.add(createNewDisequality(arg1,arg2));
		    };

		arg1 = arg1.after();
		arg2 = arg2.after();
	    };

	
	return true;

    } // factor(TmpLiteral mainAnswerLit,..
 

    private static TmpLiteral createNewDisequality(Flatterm arg1,
						   Flatterm arg2) {

	_newLiteralAssembler.reset();

	_newLiteralAssembler.pushConnective(Connective.getNot());
	
	_newLiteralAssembler.
	    pushPred(Kernel.current().equalityPredicate());

	_newLiteralAssembler.pushCopyOf(arg1);
	_newLiteralAssembler.pushCopyOf(arg2);

	_newLiteralAssembler.wrapUp();
      
	return new TmpLiteral(_newLiteralAssembler.assembledTerm());

    } // createNewDisequality(Flatterm arg1,..)


    private static FlattermAssembler _newLiteralAssembler = 
	new FlattermAssembler();


} // class AnswerFactoring