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
 * Identification of fine selection units intended for resolution and
 * creation of the corresponding objects; instances of the following
 * classes are created: {@link alabai_je.FinSelUnResolutionI}, 
 * {@link alabai_je.FinSelUnResolutionG} and 
 * {@link alabai_je.FinSelUnResolutionB}.
 */
class ResolutionSelUnIdentification {

    public
	ResolutionSelUnIdentification(SimpleReceiver<FineSelectionUnit> outputReceiver,
				      int maxPenalty,
				      KernelOptions.SelectionUnitPenalty selectionUnitPenaltyForAxioms,
				      KernelOptions.SelectionUnitPenalty selectionUnitPenaltyForHypotheses,
				      KernelOptions.SelectionUnitPenalty selectionUnitPenaltyForNegatedConjectures) 
    {
	_outputReceiver = outputReceiver;

	_checkUnifierDirections = false;

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

    /** Switches the identification of I- and G-versions of the resolution
     *  selection units: if <code>flag == false</code>, no
     *  I or G units will be created.
     */
    public void setCheckUnifierDirections(boolean flag) {
	_checkUnifierDirections = flag;
    }
    
    /** Switches output of penalty computation details. */
    public void setPrintPenaltyComputation(boolean flag) {
	_printPenaltyComputation = flag;
    }

    /** Extracts all resolution-related selection units from the clause. */
    public void process(Clause cl) {

	for (Literal lit : cl.literals())
	{
	    if (lit.selectionStatus() == 
		LiteralProperties.SelectionStatus.Selected &&
		!lit.isEquality())
	    {
		if (_checkUnifierDirections)
		    {
			FinSelUnResolutionI su1 = 
			    new FinSelUnResolutionI(cl,lit); 
			++(Statistics.current().fineSelectionUnits.resolution.extracted);
			su1.setPenalty(computePenaltyI(lit,cl,su1));
			_outputReceiver.receive(su1);
			
			
			
			FinSelUnResolutionG su2 = 
			    new FinSelUnResolutionG(cl,lit);
			++(Statistics.current().fineSelectionUnits.resolution.extracted);
			su2.setPenalty(computePenaltyG(lit,cl,su2));
			_outputReceiver.receive(su2);
		    }; // if (_checkUnifierDirections)


		FinSelUnResolutionB su3 = 
		    new FinSelUnResolutionB(cl,lit);
		++(Statistics.current().fineSelectionUnits.resolution.extracted);
		su3.setPenalty(computePenaltyB(lit,cl,su3));
		_outputReceiver.receive(su3);
	    }; 
	};
    } // process(Clause cl)



    //                             Private methods:
    


    /** Computes penalty of selection units of the type 
     *  {@link alabai_je.FinSelUnResolutionI} with the specified parameters.
     */
    private int computePenaltyI(Literal lit,Clause cl,FineSelectionUnit selUnit) {
	
	float baseValue;
	KernelOptions.SelectionUnitPenalty.Resolution param;
	    
	if (cl.isDerivedFrom(Clause.Role.NegatedConjecture))
	    {
		baseValue = 
		    _selectionUnitPenaltyForNegatedConjectures.baseValue;
		param = 
		    _selectionUnitPenaltyForNegatedConjectures.resolutionI;
	    }
	else if (cl.isDerivedFrom(Clause.Role.Hypothesis))
	    {
		baseValue = 
		    _selectionUnitPenaltyForHypotheses.baseValue;
		param = 
		    _selectionUnitPenaltyForHypotheses.resolutionI;
	    }
	else 
	    {
		assert cl.isDerivedFrom(Clause.Role.Axiom);
		baseValue = 
		    _selectionUnitPenaltyForAxioms.baseValue;
		param = 
		    _selectionUnitPenaltyForAxioms.resolutionI;
	    };
	
	return computePenalty(lit,cl,baseValue,param,selUnit);

    } // computePenaltyI(Literal lit,Clause cl,FineSelectionUnit selUnit)

    
    /** Computes penalty of selection units of the type 
     *  {@link alabai_je.FinSelUnResolutionG} with the specified parameters.
     */
    private int computePenaltyG(Literal lit,Clause cl,FineSelectionUnit selUnit) {

	float baseValue;
	KernelOptions.SelectionUnitPenalty.Resolution param;
	    
	if (cl.isDerivedFrom(Clause.Role.NegatedConjecture))
	    {
		baseValue = 
		    _selectionUnitPenaltyForNegatedConjectures.baseValue;
		param = 
		    _selectionUnitPenaltyForNegatedConjectures.resolutionG;
	    }
	else if (cl.isDerivedFrom(Clause.Role.Hypothesis))
	    {
		baseValue = 
		    _selectionUnitPenaltyForHypotheses.baseValue;
		param = 
		    _selectionUnitPenaltyForHypotheses.resolutionG;
	    }
	else 
	    {
		assert cl.isDerivedFrom(Clause.Role.Axiom);
		baseValue = 
		    _selectionUnitPenaltyForAxioms.baseValue;
		param = 
		    _selectionUnitPenaltyForAxioms.resolutionG;
	    };
	
	return computePenalty(lit,cl,baseValue,param,selUnit);

    } // computePenaltyG(Literal lit,Clause cl,FineSelectionUnit selUnit)

    
    /** Computes penalty of selection units of the type 
     *  {@link alabai_je.FinSelUnResolutionB} with the specified parameters.
     */
    private int computePenaltyB(Literal lit,Clause cl,FineSelectionUnit selUnit) {

	
	float baseValue;
	KernelOptions.SelectionUnitPenalty.Resolution param;
	    
	if (cl.isDerivedFrom(Clause.Role.NegatedConjecture))
	    {
		baseValue = 
		    _selectionUnitPenaltyForNegatedConjectures.baseValue;
		param = 
		    _selectionUnitPenaltyForNegatedConjectures.resolutionB;
	    }
	else if (cl.isDerivedFrom(Clause.Role.Hypothesis))
	    {
		baseValue = 
		    _selectionUnitPenaltyForHypotheses.baseValue;
		param = 
		    _selectionUnitPenaltyForHypotheses.resolutionB;
	    }
	else 
	    {
		assert cl.isDerivedFrom(Clause.Role.Axiom);
		baseValue = 
		    _selectionUnitPenaltyForAxioms.baseValue;
		param = 
		    _selectionUnitPenaltyForAxioms.resolutionB;
	    };
	
	return computePenalty(lit,cl,baseValue,param,selUnit);

    } // computePenaltyB(Literal lit,Clause cl,FineSelectionUnit selUnit)




    /** Computes penalty of selection units with the specified parameters. */
    private int computePenalty(Literal lit,
			       Clause cl,
			       float baseValue,
			       KernelOptions.SelectionUnitPenalty.Resolution param,
			       FineSelectionUnit selUnit) {

	float litUnifiability = 
	    UnifiabilityEstimation.
	    current().
	    estimateUnifiability(lit.atom());
	
	float unifiabilityCoeff = 
	    param.
	    litUnifiabilityCoeff.
	    evaluate(litUnifiability);
	    

	int numOfResidualLits = 0;
	int sizeOfResidualLits = 0;
	
	for (Literal lit1 : cl.literals())
	    if (lit1 != lit && lit1.isRegular())
		{
		    // lit1 is a residual literal
		    ++numOfResidualLits;
		    sizeOfResidualLits += lit1.numberOfSymbols();
		};
	
	float resLitCoeff = 
	    param.
	    residualLitCoeff.
	    evaluate(numOfResidualLits);
	
	float resSizeCoeff =  
	    param.
	    residualSizeCoeff.
	    evaluate(sizeOfResidualLits);


	int litSize = lit.numberOfSymbols();
	float litSizeCoeff = 
	    param.
	    litSizeCoeff.
	    evaluate(litSize);
	    

	int litDepth = lit.depth();
	float litDepthCoeff = 
	    param.
	    litDepthCoeff.
	    evaluate(litDepth);
	
	float result = 
	    baseValue * 
	    param.generalCoeff * 
	    unifiabilityCoeff * 
	    resLitCoeff * 
	    resSizeCoeff *
	    litSizeCoeff *
	    litDepthCoeff;


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
			       " * unif. c. " + unifiabilityCoeff + 
			       " * res. lit. num. c. " + resLitCoeff +
			       " * res. size c. " + resSizeCoeff + 
			       " * lit. size c. " + litSizeCoeff + 
			       " * lit. depth c. " + litDepthCoeff + 
			       " --> " + result +
			       " --> trunc. " + intResult);
 
	return intResult;


    } // computePenalty(Literal lit,..)




      //                            Data:

    private SimpleReceiver<FineSelectionUnit> _outputReceiver;

    private boolean _checkUnifierDirections;

    private boolean _printPenaltyComputation;

    private int _maxPenalty;

    private KernelOptions.SelectionUnitPenalty _selectionUnitPenaltyForAxioms;

    private KernelOptions.SelectionUnitPenalty _selectionUnitPenaltyForHypotheses;

    private KernelOptions.SelectionUnitPenalty _selectionUnitPenaltyForNegatedConjectures;

}; // class ResolutionSelUnIdentification
