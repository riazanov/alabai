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

/**
 * Combines all forward simplifications and retention tests.
 */
public class ForwardProcessing {

      /**
       *  @param inputSender where the input clauses are read from.
       *  @param outputReceiver where the fully processed clauses are passed to.
       *  @param forwardSimplificationCentre this object will perform main 
       *         forward simplifications, such as subsumption and demodulation.
       *  @param constraintSatisfiabilityCheckCentre this object will test 
       *         constraints in clauses for satisfiability
       *         for the purpose of pruning search space in query rewriting.
       */
    public 
    ForwardProcessing(SimpleSender<NewClause> inputSender,
		      SimpleReceiver<NewClause> outputReceiver,
		      ForwardSimplificationCentre forwardSimplificationCentre,
		      ConstraintSatisfiabilityCheckCentre constraintSatisfiabilityCheckCentre) {
	
	_inputSender = inputSender;
	_outputReceiver = outputReceiver;
	_forwardSimplificationCentre = forwardSimplificationCentre;
        _constraintSatisfiabilityCheckCentre = 
	    constraintSatisfiabilityCheckCentre;
	_simplifyingEqualityResolution = 
	    new SimplifyingEqualityResolution();
	_propositionalTautologyTest = true;
	_equationalTautologyTest = true;
	_propositionalFactoring = true;

	_discardInconsistenciesFlag = false;

	_printKept = false;
	_printDiscarded = false;
	_currentClause = null;
    }

    /** Switches forward subsumption. 
     *  @param subsumptionFilter if non-null, it is evaluated when subsumption 
     *         is attempted, and the subsumption is cancelled if the result 
     *         of the evaluation is <code>false</code>.
     *  @param subsumptionHook if non-null, it is executed when subsumption happens
     */
    public 
	void 
	setForwardSubsumption(boolean fl,
			      BinaryPredicateObject<NewClause,Clause> subsumptionFilter,
			      BinaryProcedureObject<NewClause,Clause> subsumptionHook) {
	_forwardSimplificationCentre.setForwardSubsumption(fl,
							   subsumptionFilter,
							   subsumptionHook);
    }


    /** Switches quick equality pretest in forward subsumption,
     *  if <code>fl == true</code>, the subsumption procedure will
     *  try to quickly find an equal clause before trying other possibilities.
     */
    public void setQuickEqualityPretestInForwardSubsumption(boolean fl) {
	_forwardSimplificationCentre.
	    setQuickEqualityPretestInForwardSubsumption(fl);
    }

    /** Switches proper subsumption test: if <code>fl == true</code>, 
     *  only the cheap equality test will be used.
     */
    public void setOnlyEqualityTest(boolean fl) {
	_forwardSimplificationCentre.setOnlyEqualityTest(fl);
    }

    /** Switches forward subsumption resolution. */
    public void setForwardSubsumptionResolution(boolean fl) {
	_forwardSimplificationCentre.setForwardSubsumptionResolution(fl);
    }
    
    /** Switches forward demodulation. */
    public void setForwardDemodulation(boolean fl) {
	_forwardSimplificationCentre.setForwardDemodulation(fl);
    }
    
    /** Switches propositional tautology testing. */
    public void setPropositionalTautologyTest(boolean fl) {
	_propositionalTautologyTest = fl;
    }
    
    /** Switches equational tautology testing. */
    public void setEquationalTautologyTest(boolean fl) {
	_equationalTautologyTest = fl;
    }
    
    /** Switches simplification by propositional factoring. */
    public void setPropositionalFactoring(boolean fl) {
	_propositionalFactoring = fl;
    }
    
    /** Switches simplification by equality resolution. */
    public void setSimplifyingEqualityResolution(boolean fl) {
	_simplifyingEqualityResolutionFlag = fl;
    }    


    /** Switches the discarding of clauses with constraints containing 
     *  non-nullary functions; this is a query rewriting-related feature.
     */
    public void setDiscardClausesWithFunctionsInConstraints(boolean fl) {
	_constraintSatisfiabilityCheckCentre.
	    setDiscardClausesWithFunctionsInConstraints(fl);
    }    

    /** Switches the discarding of empty clauses and clauses containing only 
     *  constraint literals but no answer literals, as answers;
     *  this is a query rewriting-related feature.
     */
    public void setDiscardInconsistencies(boolean fl) {
	_discardInconsistenciesFlag = fl;
    }

    /** Switches the discarding of clauses with too long chains
     *  of transitive predicates in constraints;
     *  this is a query rewriting-related feature.
     */
    public void setDiscardLongChainsInConstraints(boolean fl) {
	_constraintSatisfiabilityCheckCentre.
	    setDiscardLongChainsInConstraints(fl);
    }
    


    /** Specifies the feature functions to be used in the forward
     *  subsumption index (the order is important).
     */
    public 
	void 
	setForwardSubsumptionIndexFeatures(ClauseFeatureVector vector)
    {
	_forwardSimplificationCentre.setClauseFeatureFunctionVector(vector);
    }

    /** Specifies the feature functions to be used in the forward
     *  demodulation index (the order is important).
     */
    public 
	void 
	setForwardDemodulationIndexFeatures(TermFeatureVector vector)
    {
	_forwardSimplificationCentre.setTermFeatureFunctionVector(vector);
    }


    public void setPrintKept(boolean fl) { _printKept = fl; }

    public void setPrintDiscarded(boolean fl) { _printDiscarded = fl; }

    /** Tries to fully process one clause from the input sender 
     *  or internal queue.
     *  Occasionally checks {@link cushion_je.SwitcheableAbortFlag#current()}.
     *  @return false if no clauses are available for processing.
     */
    public boolean processNext() {
	
	Ref<NewClause> inputClause = new Ref<NewClause>();
	
	if (!_inputSender.send(inputClause))
	    return false;

	_currentClause = inputClause.content;

	
	if (!AnswerFactoring.factor(_currentClause))
	    {
		discardCurrentClause(DiscardReason.UnfactorableAnswer);
		return true;
	    };

	boolean anotherIteration;
	
	do
	    {
		if (_currentClause.literals().isEmpty())
		    break;

		if (_propositionalTautologyTest &&
		    TautologyTest.isPropositionalTautology(_currentClause.literals()))
		    {
			discardCurrentClause(DiscardReason.PropTautology);
			return true;
		    };
		
		if (_equationalTautologyTest &&
		    TautologyTest.isEquationalTautology(_currentClause.literals()))
		    {
			discardCurrentClause(DiscardReason.EqTautology);
			return true;
		    };
		
		
		if (_propositionalFactoring &&
		    PropositionalFactoring.simplify(_currentClause.literals()))
		    {
			++(Statistics.current().
			   forwardSimplifyingInferences.identicalLiteralFactoring);
		    };

		assert !_currentClause.literals().isEmpty();

		
		if (_simplifyingEqualityResolutionFlag &&
		    _simplifyingEqualityResolution.simplify(_currentClause))
		    {
			++(Statistics.current().
			   forwardSimplifyingInferences.simplifyingEqualityResolution);
		    };
		
		if (_currentClause.literals().isEmpty())
		    break;
		
		if (_forwardSimplificationCentre.subsume(_currentClause))
		    {
			discardCurrentClause(DiscardReason.Subsumption);
			return true;
		    };

		anotherIteration = 
		    _forwardSimplificationCentre.
		    simplifyByDemodulation(_currentClause);
		
		if (_forwardSimplificationCentre.
		    simplifyBySubsumptionResolution(_currentClause))
		    {
			if (_currentClause.literals().isEmpty())
			    break;
		    };

		assert !_currentClause.literals().isEmpty();

	    }
	while (anotherIteration);
	
	if (_constraintSatisfiabilityCheckCentre.
	    clauseHasUnsatisfiableConstraint(_currentClause))
	    {
		discardCurrentClause(DiscardReason.UnsatisfiableConstraint);
		return true;
	    };

	if (_discardInconsistenciesFlag)
	    {
		if (_currentClause.isContradiction())
		    {			
			discardCurrentClause(DiscardReason.AbsoluteContradiction);
			return true;
		    };

		if (_currentClause.isConditionalContradiction())
		    {			
			discardCurrentClause(DiscardReason.ConditionalContradiction);
			return true;
		    };

	    };

	// See also testForRetention(NewClause cl)
	
	// _currentClause is retained

	++(Statistics.current().keptClauses.total);
	
	if (_printKept)
	    System.out.println("Ret: " + _currentClause);
	
	_outputReceiver.receive(_currentClause);
	
	_currentClause = null;
	
	return true;

    } // processNext()
    


    /** Applies the available retention test, eg, forward subsumption  
     *  and tautology detection, to the specified clause.
     */
    public boolean testForRetention(NewClause cl) {

	// See also processNext()

	_currentClause = cl;

	if (!_currentClause.literals().isEmpty())
	    {
		
		if (_propositionalTautologyTest &&
		    TautologyTest.isPropositionalTautology(_currentClause.literals()))
		    {
			discardCurrentClause(DiscardReason.PropTautology);
			return false;
		    };
		
		if (_equationalTautologyTest &&
		    TautologyTest.isEquationalTautology(_currentClause.literals()))
		    {
			discardCurrentClause(DiscardReason.EqTautology);
			return false;
		    };
		
		
		
		
		if (_forwardSimplificationCentre.subsume(_currentClause))
		    {
			discardCurrentClause(DiscardReason.Subsumption);
			return false;
		    };
	    }; // if (!_currentClause.literals().isEmpty())
	
	
	if (_constraintSatisfiabilityCheckCentre.
	    clauseHasUnsatisfiableConstraint(_currentClause))
	    {
		discardCurrentClause(DiscardReason.UnsatisfiableConstraint);
		return true;
	    };

	
	if (_discardInconsistenciesFlag)
	    {
		if (_currentClause.isContradiction())
		    {			
			discardCurrentClause(DiscardReason.AbsoluteContradiction);
			return true;
		    };

		if (_currentClause.isConditionalContradiction())
		    {			
			discardCurrentClause(DiscardReason.ConditionalContradiction);
			return true;
		    };

	    };

	// _currentClause is retained
	
	++(Statistics.current().keptClauses.total);
	
	if (_printKept)
	    System.out.println("Ret: " + _currentClause);
	
	return true;

    } // testForRetention(NewClause cl)
    





    //                        Private types:

      
    private static class DiscardReason {
	
	public static final int PropTautology = 0;
	public static final int EqTautology = 1;
	public static final int Subsumption = 2;
	public static final int UnfactorableAnswer = 3;
	public static final int UnsatisfiableConstraint = 4;
	public static final int AbsoluteContradiction = 5;
	public static final int ConditionalContradiction = 6;

    }; // class DiscardReason



    //                        Private methods:


    private void discardCurrentClause(int reason) {


	String reasonShorthand = null;

	switch (reason)
	{
	    case DiscardReason.PropTautology:
		++(Statistics.current().
		   generatedClauses.discardedAsPropositionalTautologies);
		reasonShorthand = "PropTaut";
		break;

	    case DiscardReason.EqTautology:
		++(Statistics.current().
		   generatedClauses.discardedAsEquationalTautologies);
		reasonShorthand = "EqTaut";
		break;
		
	    case DiscardReason.Subsumption:
		++(Statistics.current().
		   generatedClauses.discardedBySubsumption);
		reasonShorthand = "Subs";
		break;

	    case DiscardReason.UnfactorableAnswer:
		++(Statistics.current().
		   generatedClauses.discardedAsUnfactorableAnswers);
		reasonShorthand = "UnfAns";
		break;

	    case DiscardReason.UnsatisfiableConstraint:
		++(Statistics.current().
		   generatedClauses.discardedDueToUnsatisfiableConstraints);
		reasonShorthand = "UnsatConstr";
		break;

	    case DiscardReason.AbsoluteContradiction:
		++(Statistics.current().
		   generatedClauses.discardedAsAbsoluteContradictions);
		reasonShorthand = "AbsContr";
		break;

	    case DiscardReason.ConditionalContradiction:
		++(Statistics.current().
		   generatedClauses.discardedAsConditionalContradictions);
		reasonShorthand = "CondContr";
		break;


	    default:
		assert false;
		break;
		
	}; // switch (reason)

	if (_printDiscarded)
	    System.out.println("Discard/Gen/" + reasonShorthand 
			       + ": " + _currentClause); 
	
	_currentClause = null;

    } // discardCurrentClause(DiscardReason reason)



    //                        Data:

    private SimpleSender<NewClause> _inputSender;

    private SimpleReceiver<NewClause> _outputReceiver;
      
    private ForwardSimplificationCentre _forwardSimplificationCentre;

    private ConstraintSatisfiabilityCheckCentre _constraintSatisfiabilityCheckCentre;
    
    private SimplifyingEqualityResolution _simplifyingEqualityResolution;

    private boolean _propositionalTautologyTest;

    private boolean _equationalTautologyTest;

    private boolean _propositionalFactoring;

    private boolean _simplifyingEqualityResolutionFlag;

    private boolean _discardInconsistenciesFlag;
	
    private boolean _printKept;

    private boolean _printDiscarded;

    private NewClause _currentClause;

}; // class ForwardProcessing
