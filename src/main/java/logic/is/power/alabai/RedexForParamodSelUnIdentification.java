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
 * Identification of fine selection units carrying 
 * <a href="{@docRoot}/../glossary.html#candidate_redex">candidate redexes</a>; 
 * instances of the class {@link alabai_je.FinSelUnRedexForParamod}
 * are created.
 */
class RedexForParamodSelUnIdentification {

    public
	RedexForParamodSelUnIdentification(SimpleReceiver<FineSelectionUnit> outputReceiver,
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

	_iter = new Term.LeanIterator(); 
    } // RedexForParamodSelUnIdentification(SimpleReceiver<FineSelectionUnit> outputReceiver,..)
    

    /** Switches output of penalty computation details. */
    public void setPrintPenaltyComputation(boolean flag) {
	_printPenaltyComputation = flag;
    }

    /** Extracts all redex-for-paramodulation selection units from the clause. */
    public void process(Clause cl) {
	
	for (Literal lit : cl.literals())
	{
	    if (lit.selectionStatus() == 
		LiteralProperties.SelectionStatus.Selected &&
		!lit.isGeneral()) // only for clausification
		{
		    if (lit.isEquality())
			{
			    Term arg1 = lit.atom().firstArg();
			    Term arg2 = lit.atom().secondArg();

			    int cmp = 
				ReductionOrdering.current().
				compare(arg1,arg2);

			    if (cmp == ComparisonValue.Greater || 
				cmp == ComparisonValue.Incomparable)
				{
				    // Look in arg1: 
				    
				    int arg1Position = lit.atom().firstArgPosition();
				    int positionInArg1 = 0;

				    _iter.reset(arg1);

				    while (_iter.hasNext())
					{
					    Term redex = _iter.next();
					    
					    int redexPos = arg1Position + positionInArg1;

					    if (!redex.isVariable())
						{
						    FinSelUnRedexForParamod su =
							new FinSelUnRedexForParamod(cl,
										    lit,
										    redexPos);
						    ++(Statistics.
						       current().
						       fineSelectionUnits.
						       redexForParamodulation.
						       extracted);

						    su.setPenalty(computePenalty(lit,cl,redexPos,su));
						    _outputReceiver.receive(su);
						};
					    ++positionInArg1;
					}; // while (_iter.hasNext())

				}; // if (cmp == ComparisonValue.Greater || 
			    




			    if (cmp == ComparisonValue.Smaller || 
				cmp == ComparisonValue.Incomparable)
				{
				    // Look in arg2:
				    
				    int arg2Position = lit.atom().secondArgPosition();
				    int positionInArg2 = 0;

				    _iter.reset(arg2);

				    while (_iter.hasNext())
					{
					    Term redex = _iter.next();
					    
					    int redexPos = arg2Position + positionInArg2;

					    if (!redex.isVariable())
						{
						    FinSelUnRedexForParamod su =
							new FinSelUnRedexForParamod(cl,
										    lit,
										    redexPos);
						    ++(Statistics.
						       current().
						       fineSelectionUnits.
						       redexForParamodulation.
						       extracted);

						    su.setPenalty(computePenalty(lit,cl,redexPos,su));
						    _outputReceiver.receive(su);
						};
					    ++positionInArg2;
					}; // while (_iter.hasNext())


				}; // if (cmp == ComparisonValue.Smaller ||  


			}
		    else // !lit.isEquality()
			{
			    // Look in all arguments of the atom:

			    int redexPos = 1; // skipping the predicate
			    
			    _iter.reset((AtomicFormula)lit.atom());
			    
			    _iter.next();

			    while (_iter.hasNext())
				{
				    Term redex = _iter.next();
				    
				    if (!redex.isVariable())
					{
					    FinSelUnRedexForParamod su =
						new FinSelUnRedexForParamod(cl,
									    lit,
									    redexPos);
					    ++(Statistics.
					       current().
					       fineSelectionUnits.
					       redexForParamodulation.
					       extracted);
					    
					    su.setPenalty(computePenalty(lit,cl,redexPos,su));
					    _outputReceiver.receive(su);
					};
				    ++redexPos;
				}; // while (_iter.hasNext())
			    
			    

			};
		}; // for (Literal lit : cl.literals())
	}; // for (Literal lit : cl.literals())

    } // process(Clause cl)


    /** Computes penalty of selection units of the type 
     *  {@link alabai_je.FinSelUnRedexForParamod} with the specified 
     *  parameters.
     */
    private int computePenalty(Literal lit,
			       Clause cl,
			       int redexPosition,
			       FinSelUnRedexForParamod selUnit) {

	Term redex = lit.atom().subtermInPosition(redexPosition);

	float baseValue;
	KernelOptions.SelectionUnitPenalty.RedexForParamod param;

	if (cl.isDerivedFrom(Clause.Role.NegatedConjecture))
	    {
		baseValue = 
		    _selectionUnitPenaltyForNegatedConjectures.baseValue;
		param = 
		    _selectionUnitPenaltyForNegatedConjectures.redexForParamod;
	    }
	else if (cl.isDerivedFrom(Clause.Role.Hypothesis))
	    {
		baseValue = 
		    _selectionUnitPenaltyForHypotheses.baseValue;
		param = 
		    _selectionUnitPenaltyForHypotheses.redexForParamod;
	    }
	else 
	    {
		assert cl.isDerivedFrom(Clause.Role.Axiom);
		baseValue = 
		    _selectionUnitPenaltyForAxioms.baseValue;
		param = 
		    _selectionUnitPenaltyForAxioms.redexForParamod;
	    };
	
	
	float redexUnifiability = 
	    UnifiabilityEstimation.
	    current().
	    estimateUnifiability(redex);

	float unifiabilityCoeff = 
	    param.
	    redexUnifiabilityCoeff.
	    evaluate(redexUnifiability);
	    

	int numOfResidualLits = 0;
	int sizeOfResidualLits = 0;
	
	for (Literal lit1 : cl.literals())
	    if (lit1 != lit && lit1.isRegular())
		{
		    // lit1 is a residual literal
		    ++numOfResidualLits;
		    sizeOfResidualLits += lit1.numberOfSymbols();
		};
	
	// Something will also remain of lit:
	sizeOfResidualLits += 
	    lit.numberOfSymbols() -
	    redex.numberOfSymbols() +
	    1;


	float resLitCoeff = 
	    param.
	    residualLitCoeff.
	    evaluate(numOfResidualLits);
	
	float resSizeCoeff =  
	    param.
	    residualSizeCoeff.
	    evaluate(sizeOfResidualLits);

	
	int redexDepth = lit.atom().depthOfPosition(redexPosition);
	assert redexDepth > 0;

	float redexDepthCoeff = 
	    param.
	    redexDepthCoeff.
	    evaluate(redexDepth);
	    
	
	float result = 
	    baseValue * 
	    param.generalCoeff * 
	    unifiabilityCoeff * 
	    resLitCoeff * 
	    resSizeCoeff *
	    redexDepthCoeff;
	    
	
	if (lit.isPositive())
	    {
		if (lit.isEqualityLiteral())
		    {
			result = result * param.positiveEqCoeff;
		    }
		else
		    {
			result = result * param.positiveNonEqCoeff;
		    }
	    }
	else 
	    {
		if (lit.isEqualityLiteral())
		    {
			result = result * param.negativeEqCoeff;
		    }
		else
		    {
			result = result * param.negativeNonEqCoeff;
		    }
	    };
	    



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
	    {
		String polarityBasedCoeff;
	
		if (lit.isPositive())
		    {
			if (lit.isEqualityLiteral())
			    {
				polarityBasedCoeff = 
				    " * pos.eq.coeff. " + param.positiveEqCoeff;
			    }
			else
			    {
				polarityBasedCoeff = 
				    " * pos.noneq.coeff. " + param.positiveNonEqCoeff;
			    }
		    }
		else 
		    {
			if (lit.isEqualityLiteral())
			    {
				polarityBasedCoeff = 
				    " * neg.eq.coeff. " + param.negativeEqCoeff;
			    }
			else
			    {
				polarityBasedCoeff = 
				    " * neg.noneq.coeff. " + param.negativeNonEqCoeff;
			    }
		    };
		
		
		System.out.println("PenComp: #[" + selUnit.number() + "] " + 
				   FineSelectionUnit.spell(selUnit.kind()) +
				   "  base " + baseValue +
				   " * gen.coeff. " + param.generalCoeff +
				   
				   polarityBasedCoeff +
				   
				   " * unif " + unifiabilityCoeff + 
				   " * res.lit.num. " + resLitCoeff +
				   " * res.size " + resSizeCoeff + 
				   " * red.depth " + redexDepthCoeff + 
				   " --> " + result +
				   " --> trunc. " + intResult);
	    };

	return intResult;

    } // computePenalty(Literal lit,..)



    //                  Data:

    private SimpleReceiver<FineSelectionUnit> _outputReceiver;

    private boolean _printPenaltyComputation;

    private int _maxPenalty;

    private KernelOptions.SelectionUnitPenalty _selectionUnitPenaltyForAxioms;

    private KernelOptions.SelectionUnitPenalty _selectionUnitPenaltyForHypotheses;

    private KernelOptions.SelectionUnitPenalty _selectionUnitPenaltyForNegatedConjectures;

    private Term.LeanIterator _iter;

}; // class RedexForParamodSelUnIdentification

