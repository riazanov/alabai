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


import logic.is.power.cushion.*;

import logic.is.power.logic_warehouse.*;



/**
 * Identification of fine selection units intended for equality resolution;
 * instances of the class {@link alabai_je.FinSelUnEqualityResolution}
 * are created. 
 */
class EqualityResolutionSelUnIdentification {

    public
    EqualityResolutionSelUnIdentification(SimpleReceiver<FineSelectionUnit> outputReceiver,
					 int maxPenalty,
					 KernelOptions.SelectionUnitPenalty selectionUnitPenaltyForAxioms,
					 KernelOptions.SelectionUnitPenalty selectionUnitPenaltyForHypotheses,
					 KernelOptions.SelectionUnitPenalty selectionUnitPenaltyForNegatedConjectures) {
	_outputReceiver = outputReceiver;

	_printPenaltyComputation = false;

	_maxPenalty = maxPenalty;

	assert selectionUnitPenaltyForAxioms != null;
	assert selectionUnitPenaltyForHypotheses != null;
	assert selectionUnitPenaltyForNegatedConjectures != null;

	_selectionUnitPenaltyForAxioms = selectionUnitPenaltyForAxioms;
	_selectionUnitPenaltyForHypotheses = selectionUnitPenaltyForHypotheses;
	_selectionUnitPenaltyForNegatedConjectures = 
	    selectionUnitPenaltyForNegatedConjectures;
    }
    

    /** Switches output of penalty computation details. */
    public void setPrintPenaltyComputation(boolean flag) {
	_printPenaltyComputation = flag;
    }



    /** Extracts all equality resolution selection units from the clause
     *  (currently, at most one, which allows resolving
     *   any negative equalities in the clause).
     */
    public void process(Clause cl) {
	
	for (Literal lit : cl.literals())
	    if (lit.isNegative() &&
		lit.isEquality() && 
		Unification.possiblyUnify(lit.firstArg(),
					  lit.secondArg()))
		{
		    // One eligible literal is enough to justify
		    // a selection unit:
		    FinSelUnEqualityResolution su = 
			new FinSelUnEqualityResolution(cl);
		    ++(Statistics.current().fineSelectionUnits.equalityResolution.extracted);
		    su.setPenalty(computePenalty(cl,su));
		    _outputReceiver.receive(su);
		    
		    return;
		};
    } // process(Clause cl)



    //                             Private methods:
    
    /** Computes penalty of selection units of the type 
     *  {@link alabai_je.FinSelUnEqualityResolution} with the specified 
     *  clause.
     */
    private int computePenalty(Clause cl,FineSelectionUnit selUnit) {
	
	
	float baseValue;
	KernelOptions.SelectionUnitPenalty.EqualityResolution param;
	    	
	if (cl.isDerivedFrom(Clause.Role.NegatedConjecture))
	    {
		baseValue = 
		    _selectionUnitPenaltyForNegatedConjectures.baseValue;
		param = 
		    _selectionUnitPenaltyForNegatedConjectures.equalityResolution;
	    }
	else if (cl.isDerivedFrom(Clause.Role.Hypothesis))
	    {
		baseValue = 
		    _selectionUnitPenaltyForHypotheses.baseValue;
		param = 
		    _selectionUnitPenaltyForHypotheses.equalityResolution;
	    }
	else 
	    {
		assert cl.isDerivedFrom(Clause.Role.Axiom);
		baseValue = 
		    _selectionUnitPenaltyForAxioms.baseValue;
		param = 
		    _selectionUnitPenaltyForAxioms.equalityResolution;
	    };
       
	    
	int numOfRegularLits = 0;
	int sizeOfRegularLits = 0;

	for (Literal lit : cl.literals())
	    if (lit.isRegular())
		{
		    ++numOfRegularLits;
		    sizeOfRegularLits += lit.numberOfSymbols();
		};
	
	float litNumCoeff = 
	    param.
	    litNumCoeff.
	    evaluate(numOfRegularLits);
	
	
	float clauseSizeCoeff =  
	    param.
	    clauseSizeCoeff.
	    evaluate(sizeOfRegularLits);

	
	float result = 
	    baseValue * param.generalCoeff * litNumCoeff * clauseSizeCoeff;

	
	int intResult = (int)result;
    
	assert intResult >= 0;

	if (intResult > _maxPenalty) 
	    {
		intResult = _maxPenalty;
	    }
	else if (intResult == 0)
	    {
		intResult = 1;
	    };
	    
	
	if (_printPenaltyComputation)
	    System.out.println("PenComp: #[" + selUnit.number() + "] " + 
			       FineSelectionUnit.spell(selUnit.kind()) +
			       "  base " + baseValue +
			       " * gen. coeff. " + param.generalCoeff +
			       " * lit. num. " + litNumCoeff +
			       " + cl. size " + clauseSizeCoeff + 
			       " --> " + result +
			       " --> trunc. " + intResult);
 
	return intResult;



    } // computePenalty(Clause cl,FineSelectionUnit selUnit)






    //                    Data:

    private SimpleReceiver<FineSelectionUnit> _outputReceiver;
    
    private boolean _printPenaltyComputation;

    private int _maxPenalty;

    private KernelOptions.SelectionUnitPenalty _selectionUnitPenaltyForAxioms;

    private KernelOptions.SelectionUnitPenalty _selectionUnitPenaltyForHypotheses;

    private KernelOptions.SelectionUnitPenalty _selectionUnitPenaltyForNegatedConjectures;


}; // class EqualityResolutionSelUnIdentification
