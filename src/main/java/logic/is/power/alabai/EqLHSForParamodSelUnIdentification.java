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
 * Identification of fine selection units carrying positive equality literals
 * to <em>paramodulate from</em>; 
 * <a href="{@docRoot}/../glossary.html#paramodulation_from_into">paramodulate from</a>
 * instances of the following
 * classes are created: {@link alabai_je.FinSelUnEqLHSForParamodI}, 
 * {@link alabai_je.FinSelUnEqLHSForParamodG} and 
 * {@link alabai_je.FinSelUnEqLHSForParamodB}.
 */
class EqLHSForParamodSelUnIdentification {

    public 
    EqLHSForParamodSelUnIdentification(SimpleReceiver<FineSelectionUnit> outputReceiver,
				      int maxPenalty,
				      KernelOptions.SelectionUnitPenalty selectionUnitPenaltyForAxioms,
				      KernelOptions.SelectionUnitPenalty selectionUnitPenaltyForHypotheses,
				      KernelOptions.SelectionUnitPenalty selectionUnitPenaltyForNegatedConjectures) {
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
    } // EqLHSForParamodSelUnIdentification(SimpleReceiver<FineSelectionUnit> outputReceiver,..)
    

    /** Switches the identification of I- and G-versions of the paramodulation
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

    /** Extracts all eq-LHS-for-paramodulation selection units 
     *  from the clause. 
     */
    public void process(Clause cl) {

	for (Literal lit : cl.literals())
	{
	    if (lit.selectionStatus() == 
		LiteralProperties.SelectionStatus.Selected &&
		lit.isPositive() &&
		lit.isEquality())
	    {
		Term arg1 = lit.firstArg();
		Term arg2 = lit.secondArg();
		
		int cmp = 
		    ReductionOrdering.current().compare(arg1,arg2);

		if (cmp == ComparisonValue.Greater || 
		    cmp == ComparisonValue.Incomparable)
		    {
			// arg1 => arg2

			if (_checkUnifierDirections)
			    {
				FinSelUnEqLHSForParamodI su1 =
				    new FinSelUnEqLHSForParamodI(cl,
								 lit,
								 0); // arg1
				++(Statistics.current().fineSelectionUnits.eqLHSForParamodulation.extracted);
				su1.setPenalty(computePenaltyI(lit,cl,0,su1));
				_outputReceiver.receive(su1);
				
				
				FinSelUnEqLHSForParamodG su2 =
				    new FinSelUnEqLHSForParamodG(cl,
								 lit,
								 0); // arg1
				++(Statistics.current().fineSelectionUnits.eqLHSForParamodulation.extracted);
				su2.setPenalty(computePenaltyI(lit,cl,0,su2));
				_outputReceiver.receive(su2);

			    }; // if (_checkUnifierDirections)

			FinSelUnEqLHSForParamodB su3 =
			    new FinSelUnEqLHSForParamodB(cl,
							 lit,
							 0); // arg1
			++(Statistics.current().fineSelectionUnits.eqLHSForParamodulation.extracted);
			su3.setPenalty(computePenaltyI(lit,cl,0,su3));
			_outputReceiver.receive(su3);
			
		    }; // if (cmp == Greater || cmp == Incomparable)


		if (cmp == ComparisonValue.Smaller || 
		    cmp == ComparisonValue.Incomparable)
		    {
			// arg2 => arg1
			
			if (_checkUnifierDirections)
			    {
				FinSelUnEqLHSForParamodI su1 =
				    new FinSelUnEqLHSForParamodI(cl,
								 lit,
								 1); // arg2
				++(Statistics.current().fineSelectionUnits.eqLHSForParamodulation.extracted);
				su1.setPenalty(computePenaltyI(lit,cl,1,su1));
				_outputReceiver.receive(su1);
				
			
				FinSelUnEqLHSForParamodG su2 =
				    new FinSelUnEqLHSForParamodG(cl,
								 lit,
								 1); // arg2
				++(Statistics.current().fineSelectionUnits.eqLHSForParamodulation.extracted);
				su2.setPenalty(computePenaltyI(lit,cl,1,su2));
				_outputReceiver.receive(su2);
			    }; // if (_checkUnifierDirections)


			FinSelUnEqLHSForParamodB su3 =
			    new FinSelUnEqLHSForParamodB(cl,
							 lit,
							 1); // arg2
			++(Statistics.current().fineSelectionUnits.eqLHSForParamodulation.extracted);
			su3.setPenalty(computePenaltyI(lit,cl,1,su3));
			_outputReceiver.receive(su3);


		    }; // if (cmp == Smaller || cmp == Incomparable)



	    }; // if (lit.selectionStatus() == 
	}; // for (Literal lit : cl.literals())

    } // process(Clause cl)




    //                             Private methods:
    
    /** Computes penalty of selection units of the type 
     *  {@link alabai_je.FinSelUnEqLHSForParamodI} with the specified parameters.
     */
    private int computePenaltyI(Literal lit,
				Clause cl,
				int argumentNumber,
				FineSelectionUnit selUnit) {
	
	float baseValue;
	KernelOptions.SelectionUnitPenalty.EqLHSForParamod param;
	    
	if (cl.isDerivedFrom(Clause.Role.NegatedConjecture))
	    {
		baseValue = 
		    _selectionUnitPenaltyForNegatedConjectures.baseValue;
		param = 
		    _selectionUnitPenaltyForNegatedConjectures.eqLHSForParamodI;
	    }
	else if (cl.isDerivedFrom(Clause.Role.Hypothesis))
	    {
		baseValue = 
		    _selectionUnitPenaltyForHypotheses.baseValue;
		param = 
		    _selectionUnitPenaltyForHypotheses.eqLHSForParamodI;
	    }
	else 
	    {
		assert cl.isDerivedFrom(Clause.Role.Axiom);
		baseValue = 
		    _selectionUnitPenaltyForAxioms.baseValue;
		param = 
		    _selectionUnitPenaltyForAxioms.eqLHSForParamodI;
	    };
	
	return computePenalty(lit,
			      cl,
			      argumentNumber,
			      baseValue,
			      param,
			      selUnit);

    } // computePenaltyI(Literal lit,Clause cl,..)

    


    /** Computes penalty of selection units of the type 
     *  {@link alabai_je.FinSelUnEqLHSForParamodG} with the specified parameters.
     */
    private int computePenaltyG(Literal lit,
				Clause cl,
				int argumentNumber,
				FineSelectionUnit selUnit) {
	
	
	
	float baseValue;
	KernelOptions.SelectionUnitPenalty.EqLHSForParamod param;
	    
	if (cl.isDerivedFrom(Clause.Role.NegatedConjecture))
	    {
		baseValue = 
		    _selectionUnitPenaltyForNegatedConjectures.baseValue;
		param = 
		    _selectionUnitPenaltyForNegatedConjectures.eqLHSForParamodG;
	    }
	else if (cl.isDerivedFrom(Clause.Role.Hypothesis))
	    {
		baseValue = 
		    _selectionUnitPenaltyForHypotheses.baseValue;
		param = 
		    _selectionUnitPenaltyForHypotheses.eqLHSForParamodG;
	    }
	else 
	    {
		assert cl.isDerivedFrom(Clause.Role.Axiom);
		baseValue = 
		    _selectionUnitPenaltyForAxioms.baseValue;
		param = 
		    _selectionUnitPenaltyForAxioms.eqLHSForParamodG;
	    };
	
	return computePenalty(lit,
			      cl,
			      argumentNumber,
			      baseValue,
			      param,
			      selUnit);


    } // computePenaltyG(Literal lit,Clause cl,..)

    


    /** Computes penalty of selection units of the type 
     *  {@link alabai_je.FinSelUnEqLHSForParamodB} with the specified parameters.
     */
    private int computePenaltyB(Literal lit,
				Clause cl,
				int argumentNumber,
				FineSelectionUnit selUnit) {
	
	
	float baseValue;
	KernelOptions.SelectionUnitPenalty.EqLHSForParamod param;
	    
	if (cl.isDerivedFrom(Clause.Role.NegatedConjecture))
	    {
		baseValue = 
		    _selectionUnitPenaltyForNegatedConjectures.baseValue;
		param = 
		    _selectionUnitPenaltyForNegatedConjectures.eqLHSForParamodB;
	    }
	else if (cl.isDerivedFrom(Clause.Role.Hypothesis))
	    {
		baseValue = 
		    _selectionUnitPenaltyForHypotheses.baseValue;
		param = 
		    _selectionUnitPenaltyForHypotheses.eqLHSForParamodB;
	    }
	else 
	    {
		assert cl.isDerivedFrom(Clause.Role.Axiom);
		baseValue = 
		    _selectionUnitPenaltyForAxioms.baseValue;
		param = 
		    _selectionUnitPenaltyForAxioms.eqLHSForParamodB;
	    };
	
	return computePenalty(lit,
			      cl,
			      argumentNumber,
			      baseValue,
			      param,
			      selUnit);

    } // computePenaltyB(Literal lit,Clause cl,..)



    /** Computes penalty of selection units with the specified parameters. */
    private int computePenalty(Literal lit,
			       Clause cl,
			       int argumentNumber,
			       float baseValue,
			       KernelOptions.SelectionUnitPenalty.EqLHSForParamod param,
			       FineSelectionUnit selUnit) {

	Term lhs = (argumentNumber == 0)? lit.firstArg() : lit.secondArg();
	Term rhs = (argumentNumber == 0)? lit.secondArg() : lit.firstArg(); 

	float lhsUnifiability = 
	    UnifiabilityEstimation.
	    current().
	    estimateUnifiability(lhs);
	
	float unifiabilityCoeff = 
	    param.
	    lhsUnifiabilityCoeff.
	    evaluate(lhsUnifiability);
	    

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
	    evaluate(sizeOfResidualLits + rhs.numberOfSymbols());

	
	float result = 
	    baseValue * 
	    param.generalCoeff * 
	    unifiabilityCoeff * 
	    resLitCoeff * 
	    resSizeCoeff;


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
			       "  * unif " + unifiabilityCoeff + 
			       " * res.lit. num. " + resLitCoeff +
			       " + res. size " + resSizeCoeff + 
			       " --> " + result +
			       " --> trunc. " + intResult);
 
	return intResult;


    } // computePenalty(Literal lit,..)




    //                           Data: 

    private SimpleReceiver<FineSelectionUnit> _outputReceiver;

    private boolean _checkUnifierDirections;

    private boolean _printPenaltyComputation;

    private int _maxPenalty;

    private KernelOptions.SelectionUnitPenalty _selectionUnitPenaltyForAxioms;

    private KernelOptions.SelectionUnitPenalty _selectionUnitPenaltyForHypotheses;

    private KernelOptions.SelectionUnitPenalty _selectionUnitPenaltyForNegatedConjectures;

}; // class EqLHSForParamodSelUnIdentification
