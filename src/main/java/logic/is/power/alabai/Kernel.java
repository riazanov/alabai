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


/** Main module of the prover; it implements the saturation architecture
 * (as described in
 * <a href="{@docRoot}/../references.html#new_architectures_position_paper">[New Architectures position paper]</a>
 *  ), as well as some auxilliary functionality, such as input and output.
 *  Instances of the class can be viewed as separate sessions of 
 *  saturation. 
 *
 *  The API alse supports stepwise execution, which allows concurrent
 *  (interleaved) execution of several kernel sessions.
 */
public class Kernel implements NumericIdSource {

    public static enum TerminationCode {
       
	/** Indicates that the kernel session has not terminated yet. */
	Unterminated,
	
	/** Indicates that the kernel session was terminated due to saturation. */
	Saturation,                
	
	/** Indicates that the kernel session was aborted externally with 
	 *  {@link Kernel#abort()}.
	 */
	Abortion                    
    };

    /** @param opt options
     *  @param terminalClauseReceiver where all generated 
     *         <a href="{@docRoot}/../glossary.html#terminal_clause">terminal clauses</a>
     *        will be sent
     */
    public Kernel(KernelOptions opt,
		  SimpleReceiver<Clause> terminalClauseReceiver) {
	_options = opt.clone();
	_timer = new logic.is.power.cushion.Timer();
	_abortFlag = new SwitcheableAbortFlag();
	_terminationCode = TerminationCode.Unterminated;
	_statistics = new Statistics();
	_signature = new Signature(_options.mainEqualitySymbol,
				   _options.mainDisequalitySymbol,
				   _options.mainEqualityIsInfix,
				   _options.otherEqualitySymbols,
				   "$true",
				   new LinkedList<String>(),
				   "$false",
				   new LinkedList<String>());

	_clauseContext = new Clause.Context();
	_newClauseContext = new NewClause.Context(this);
	

	if (opt.reductionOrdering == 
	    KernelOptions.ReductionOrdering.SubtermRelation)
	    {
		_reductionOrdering = new SubtermRelationAsReductionOrdering();
	    }
	else if (opt.reductionOrdering == 
		 KernelOptions.ReductionOrdering.NonrecursiveKBO)
	    {
		_reductionOrdering = new NonrecursiveKBO();
	    }
	else
	    assert false;

	_newClauseQueue = new SimpleQueue<NewClause>();
	_newClauseAssembler = new NewClauseAssembler(_newClauseQueue);
	if (opt.preserveInput)
	    {
		_inputClauseQueue = new SimpleRestrictedQueue<NewClause>(1);
		_inputClauseAssembler = new NewClauseAssembler(_inputClauseQueue);
	    };
	_forwardSimplificationCentre = new ForwardSimplificationCentre();
	_constraintSatisfiabilityCheckCentre = 
	    new ConstraintSatisfiabilityCheckCentre();
	_retainedClauseQueue = new SimpleQueue<NewClause>();
	_forwardProcessing = 
	    new ForwardProcessing(_newClauseQueue,
				  _retainedClauseQueue,
				  _forwardSimplificationCentre,
				  _constraintSatisfiabilityCheckCentre);

	_clauseDiscarder = new ClauseDiscarder();
	_disposalBuffer = new LinkedList<Clause>(); 
	_backwardSimplificationCentre = 
	    new BackwardSimplificationCentre(_clauseDiscarder,
					     _newClauseAssembler);
 	_clauseReceiverHub1 = 
 	    new SimpleReceiverConjunction<Clause>(_forwardSimplificationCentre,
 						  _backwardSimplificationCentre);
	_clauseSelection = new ClauseSelection();

 	_clauseReceiverHub2 = 
 	    new SimpleReceiverConjunction<Clause>(_clauseReceiverHub1,
 						  _clauseSelection);

 	_clauseReceiverHub3 = 
 	    new SimpleReceiverConjunction<Clause>(_forwardSimplificationCentre,
 						  terminalClauseReceiver);

	_terminalClauseFilter = 
	    new TerminalClauseFilter(_clauseReceiverHub3,
				     _clauseReceiverHub2);

 	_persistentClauseAssembler = 
 	    new ClauseAssembler(_retainedClauseQueue,_terminalClauseFilter,null);
	

	if (opt.preserveInput)
	    {
		_persistentInputClauseQueue = 
		    new SimpleRestrictedQueue<Clause>(1);
		_persistentInputClauseAssembler = 
		    new ClauseAssembler(_inputClauseQueue,
					_persistentInputClauseQueue,
					null);
	    };

	_sharedTermFactory = new TermFactory();
 
 	_clausifier = new Clausifier(_newClauseAssembler);

 	_generatingInferenceCentre = 
 	    new GeneratingInferenceCentre(_newClauseAssembler,
					  _options.maximalSelectionUnitPenalty);

	_unifiabilityEstimationLog = 
	    new UnifiabilityEstimationLog();

	_unifiabilityEstimation = 
	    new UnifiabilityEstimation(_options.unifiabilityEstimation.maxDepth,
				       _options.unifiabilityEstimation.deepeningCoeff,
				       _options.unifiabilityEstimation.duplicateVarUnifiability,
				       _options.unifiabilityEstimation.constUnifiability,
				       _options.unifiabilityEstimation.funcUnifiability,
				       _options.unifiabilityEstimation.propConstUnifiability,
				       _options.unifiabilityEstimation.predUnifiability);

 	_fineSelectionUnitIdentificationCentre =
 	    new FineSelectionUnitIdentificationCentre(_generatingInferenceCentre,
						      _options.maximalSelectionUnitPenalty,
						      _options.selectionUnitPenaltyForAxioms,
						      _options.selectionUnitPenaltyForHypotheses,
						      _options.selectionUnitPenaltyForNegatedConjectures,
						      _options.paramodulation);

 	_literalSelection = 
 	    new LiteralSelection(_fineSelectionUnitIdentificationCentre); 

	_selectedClause = new Ref<Clause>(); 

	if (opt.preserveInput)
	    _preservedInput = new LinkedList<Clause>(); 

	_lastNumericId = -1;


	// Configure the components here:
	
	_clauseContext.
	    setMarkUnselectedLiterals(_options.markUnselectedLiterals);

	_newClauseAssembler.setPrintAssembled(_options.printNew);
	if (opt.preserveInput)
	    _inputClauseAssembler.setPrintAssembled(_options.printNew);

	_forwardProcessing.
	    setForwardSubsumption(_options.forwardSubsumption.on,null,null);
	_forwardProcessing.
	    setQuickEqualityPretestInForwardSubsumption(_options.forwardSubsumption.equalityPretest);
	_forwardProcessing.
	    setForwardSubsumptionResolution(_options.forwardSubsumptionResolution.on);
	_forwardProcessing.
	    setForwardDemodulation(_options.forwardDemodulation.on);
	_forwardProcessing.
	    setPropositionalTautologyTest(_options.propositionalTautologyTest);
	_forwardProcessing.
	    setEquationalTautologyTest(_options.equationalTautologyTest);
	_forwardProcessing.
	    setSimplifyingEqualityResolution(_options.
					     simplifyingEqualityResolution.on);

	
	_forwardProcessing.
	    setDiscardClausesWithFunctionsInConstraints(!_options.
							queryRewriting.
							allowFunctionsInConstraints);
	_forwardProcessing.
	    setDiscardInconsistencies(!_options.
				      queryRewriting.
				      inconsistenciesAsAnswers);
	_forwardProcessing.
	    setDiscardLongChainsInConstraints(_options.
					      queryRewriting.
					      limitedTransitivePredicateChainingInConstraints.
					      on) ;
	


	_forwardProcessing.
	    setForwardSubsumptionIndexFeatures(_options.
					       forwardSubsumption.
					       featureFunctionVector);
	_forwardProcessing.
	    setForwardDemodulationIndexFeatures(_options.
						forwardDemodulation.
						featureFunctionVector);
	_forwardProcessing.setPrintKept(_options.printKept);
	_forwardProcessing.setPrintDiscarded(_options.printDiscarded);



	_backwardSimplificationCentre.
	    setBackwardSubsumption(_options.backwardSubsumption.on);
	_backwardSimplificationCentre.
	    setBackwardDemodulation(_options.backwardDemodulation.on);
	_backwardSimplificationCentre.
	    setClauseFeatureFunctionVector(_options.
					   backwardSubsumption.
					   featureFunctionVector);
	_backwardSimplificationCentre.
	    setTermFeatureFunctionVector(_options.
					 backwardDemodulation.
					 featureFunctionVector);
	_backwardSimplificationCentre.setPrintDiscarded(_options.printDiscarded);

	_persistentClauseAssembler.setPrintAssembled(_options.printAssembled);
	_generatingInferenceCentre.setCheckUnifierDirectionsInResolution(_options.resolution.checkUnifierDirections);
	_generatingInferenceCentre.setCheckUnifierDirectionsInParamodulation(_options.paramodulation.checkUnifierDirections);
	_generatingInferenceCentre.setPrintPromotedUnits(_options.printPromoted);
	_generatingInferenceCentre.setWatchList(_options.selUnitPromotionWatchList);

	if (_options.printUnifiability)
	    _unifiabilityEstimation.setLogging(_unifiabilityEstimationLog);

	_fineSelectionUnitIdentificationCentre.
	    setCheckUnifierDirectionsInResolution(_options.resolution.checkUnifierDirections);
	_fineSelectionUnitIdentificationCentre.
	    setCheckUnifierDirectionsInParamodulation(_options.paramodulation.checkUnifierDirections);
	_fineSelectionUnitIdentificationCentre.
	    setPrintProcessedClauses(_options.printActivatedClauses);
	_fineSelectionUnitIdentificationCentre.
	    setPrintExtractedUnits(_options.printSelectionUnits);
	_fineSelectionUnitIdentificationCentre.
	    setPrintPenaltyComputation(_options.printPenaltyComputation);

	_literalSelection.
	    setFuncForAxioms(_options.literalSelection.forAxioms);
	_literalSelection.
	    setFuncForHypotheses(_options.literalSelection.forHypotheses);
	_literalSelection.
	    setFuncForNegatedConjectures(_options.literalSelection.forNegatedConjectures);
	
	// Identify special predicates and their properties:

	for (Pair<String,Integer> p : _options.answerPredicates)
	    {
		Predicate pred = 
		    _signature.
		    representationForPredicate(p.first,
					       p.second.intValue());
		PredicateInfo.markAsAnswer(pred);
	    };

	for (Pair<String,Integer> p : _options.constraintPredicates)
	    {
		Predicate pred = 
		    _signature.
		    representationForPredicate(p.first,
					       p.second.intValue());
		PredicateInfo.markAsConstraintPredicate(pred);
	    };

	for (Map.Entry<Pair<String,Integer>,Integer> limAssign :
		 _options.
		 queryRewriting.
		 limitedTransitivePredicateChainingInConstraints.
		 limitedChainPredicates.
		 entrySet())
	    {
		Predicate pred = 
		    _signature.
		    representationForPredicate(limAssign.getKey().first,
					       limAssign.getKey().second.intValue());
		int limit = limAssign.getValue().intValue();
		
		PredicateInfo.setConstraintChainLengthLimit(pred,limit);
	    };

    } // Kernel(KernelOptions opt)


    /** Current session; may be null if no session is active 
     *  at the moment.
     */
    public static Kernel current() { return _current; }

    /** Sets <code>s</code> as the current kernel session.
     *  This method is not called explicitly in this module,
     *  only via <code>AutoSwitch<Kernel></code> objects.
     *  <b>pre:</b> <code>session != null || current() != null</code>.
     */
    public static void makeCurrent(Kernel session) {
	
	if (session != null)
	{
	    if (_current != null) _current._timer.stop();
	    session._timer.start();
	    SwitcheableAbortFlag.makeCurrent(session._abortFlag);
	    Statistics.makeCurrent(session._statistics);
	    Signature.makeCurrent(session._signature);
	    Clause.Context.makeCurrent(session._clauseContext);
	    NewClause.Context.makeCurrent(session._newClauseContext);
	    ReductionOrdering.makeCurrent(session._reductionOrdering);
	    TermFactory.makeCurrent(session._sharedTermFactory);
	    UnifiabilityEstimation.makeCurrent(session._unifiabilityEstimation);
	}
	else // session == null
	{
	    assert _current != null;

	    _current._timer.stop();
	};

	_current = session;

    } // makeCurrent(Kernel session)

    /** @return <b>this</b> == current() */
    public boolean isCurrent() { return this == current(); }
      


    
    //       FOF input:

    public void addAxiom(InputSyntax.Formula f) {


	// Switch to this object as the current static context:
	Kernel savedSession = current();
	makeCurrent(this);

	
	// Echo:

	if (_options.printInput)
	    System.out.println("In/FOF/Ax: (" + f + ")"); 

	if (_options.preserveInput)
	    {
		Clause cl = inputFormulaAsClause(f,Clause.Role.Axiom);
		
		if (cl == null) 
		    // the clause was discarded, e.g., forward-subsumed
		    {
			// Switch back to the previous static context:
			makeCurrent(savedSession);
			return;
		    };
		_preservedInput.addLast(cl);
		_newClauseAssembler.copyClause(cl);
	    }
	else
	    {

		// Dump the clause in _newClauseAssembler:

		_newClauseAssembler.convertInputFormula(f,Clause.Role.Axiom);
	    };

	// Now the clause is in _newClauseQueue.

 	while (_forwardProcessing.processNext()) 
	{
	    // What remains of the clause is now in _retainedClauseQueue

 	    while (_persistentClauseAssembler.processNext()) 
 	    {
 		// nothing here: all the work is done in 
 		// _forwardProcessing.processNext() and 
 		// _persistentClauseAssembler.processNext()
 	    };

	    // What remains of the clause is now in _clauseSelection	    
	};

	// Switch back to the previous static context:
	makeCurrent(savedSession);

    } // addAxiom(InputSyntax.Formula f)


    public void addHypothesis(InputSyntax.Formula f) {

		
	// Switch to this object as the current static context:
	Kernel savedSession = current();
	makeCurrent(this);

	
	// Echo:

	if (_options.printInput)
	    System.out.println("In/FOF/Hyp: (" + f + ")"); 


	if (_options.preserveInput)
	    {
		Clause cl = inputFormulaAsClause(f,Clause.Role.Hypothesis);
		
		if (cl == null) 
		    // the clause was discarded, e.g., forward-subsumed
		    {
			// Switch back to the previous static context:
			makeCurrent(savedSession);
			return;
		    };
		_preservedInput.addLast(cl);
		_newClauseAssembler.copyClause(cl);
	    }
	else
	    {
		// Dump the clause in _newClauseAssembler:
		
		_newClauseAssembler.convertInputFormula(f,Clause.Role.Hypothesis);
	    };

	// Now the clause is in _newClauseQueue.

 	while (_forwardProcessing.processNext()) 
	{
	    // What remains of the clause is now in _retainedClauseQueue

 	    while (_persistentClauseAssembler.processNext()) 
 	    {
 		// nothing here: all the work is done in 
 		// _forwardProcessing.processNext() and 
 		// _persistentClauseAssembler.processNext()
 	    };

	    // What remains of the clause is now in _clauseSelection	    
	};

	// Switch back to the previous static context:
	makeCurrent(savedSession);


    } // addHypothesis(InputSyntax.Formula f)


    
    public void addNegatedConjecture(InputSyntax.Formula f) {

		
	// Switch to this object as the current static context:
	Kernel savedSession = current();
	makeCurrent(this);

	
	// Echo:

	if (_options.printInput)
	    System.out.println("In/FOF/NConj: (" + f + ")"); 

	if (_options.preserveInput)
	    {
		Clause cl = inputFormulaAsClause(f,Clause.Role.NegatedConjecture);
		
		if (cl == null) 
		    // the clause was discarded, e.g., forward-subsumed
		    {
			// Switch back to the previous static context:
			makeCurrent(savedSession);
			return;
		    };
		_preservedInput.addLast(cl);
		_newClauseAssembler.copyClause(cl);
	    }
	else
	    {

		// Dump the clause in _newClauseAssembler:

		_newClauseAssembler.convertInputFormula(f,Clause.Role.NegatedConjecture);
	    };



	// Now the clause is in _newClauseQueue.

 	while (_forwardProcessing.processNext()) 
	{
	    // What remains of the clause is now in _retainedClauseQueue

 	    while (_persistentClauseAssembler.processNext()) 
 	    {
 		// nothing here: all the work is done in 
 		// _forwardProcessing.processNext() and 
 		// _persistentClauseAssembler.processNext()
 	    };

	    // What remains of the clause is now in _clauseSelection	    
	};

	// Switch back to the previous static context:
	makeCurrent(savedSession);

    } // addNegatedConjecture(InputSyntax.Formula f)



    //       CNF input:

    /** @param clause != null */
    public void addAxiom(Collection<InputSyntax.Literal> clause) {

	// Switch to this object as the current static context:
	Kernel savedSession = current();
	makeCurrent(this);

	
	// Echo:

	if (_options.printInput)
	{
	    System.out.print("In/CNF/Ax: ("); 
	    Iterator<InputSyntax.Literal> lit = clause.iterator();
	    if (lit.hasNext())
	    {
		System.out.print(lit.next().toString());
		while (lit.hasNext())
		    System.out.print(" | " + lit.next());
	    };
	    System.out.println(")");
	};

	if (_options.preserveInput)
	    {
		Clause cl = inputLiteralListAsClause(clause,Clause.Role.Axiom);
		
		if (cl == null) 
		    // the clause was discarded, e.g., forward-subsumed
		    {
			// Switch back to the previous static context:
			makeCurrent(savedSession);
			return;
		    };
		_preservedInput.addLast(cl);
		_newClauseAssembler.copyClause(cl);
	    }
	else
	    {
		// Dump the clause in _newClauseAssembler:
		
		_newClauseAssembler.convertInputClause(clause,Clause.Role.Axiom);
	    };

		
	// Now the clause is in _newClauseQueue.

 	while (_forwardProcessing.processNext()) 
	{
	    // What remains of the clause is now in _retainedClauseQueue

 	    while (_persistentClauseAssembler.processNext()) 
 	    {
 		// nothing here: all the work is done in 
 		// _forwardProcessing.processNext() and 
 		// _persistentClauseAssembler.processNext()
 	    };

	    // What remains of the clause is now in _clauseSelection	    
	};

	// Switch back to the previous static context:
	makeCurrent(savedSession);
	
    } // addAxiom(Collection<InputSyntax.Literal> clause)



    /** @param clause != null */
    public void addHypothesis(Collection<InputSyntax.Literal> clause) {
	
	// Switch to this object as the current static context:
	Kernel savedSession = current();
	makeCurrent(this);

	
	// Echo:

	if (_options.printInput)
	{
	    System.out.print("In/CNF/Hyp: ("); 
	    Iterator<InputSyntax.Literal> lit = clause.iterator();
	    if (lit.hasNext())
	    {
		System.out.print(lit.next().toString());
		while (lit.hasNext())
		    System.out.print(" | " + lit.next());
	    };
	    System.out.println(")");
	};


	if (_options.preserveInput)
	    {
		Clause cl = inputLiteralListAsClause(clause,Clause.Role.Hypothesis);
		
		if (cl == null) 
		    // the clause was discarded, e.g., forward-subsumed
		    {
			// Switch back to the previous static context:
			makeCurrent(savedSession);
			return;
		    };
		_preservedInput.addLast(cl);
		_newClauseAssembler.copyClause(cl);
	    }
	else
	    {
		// Dump the clause in _newClauseAssembler:

		_newClauseAssembler.convertInputClause(clause,Clause.Role.Hypothesis);
	    };

	// Now the clause is in _newClauseQueue.

 	while (_forwardProcessing.processNext()) 
	{
	    // What remains of the clause is now in _retainedClauseQueue

 	    while (_persistentClauseAssembler.processNext()) 
 	    {
 		// nothing here: all the work is done in 
 		// _forwardProcessing.processNext() and 
 		// _persistentClauseAssembler.processNext()
 	    };

	    // What remains of the clause is now in _clauseSelection	    
	};

	// Switch back to the previous static context:
	makeCurrent(savedSession);

    } // addHypothesis(Collection<InputSyntax.Literal> clause)



    /** @param clause != null */
    public void addNegatedConjecture(Collection<InputSyntax.Literal> clause) {
	
	// Switch to this object as the current static context:
	Kernel savedSession = current();
	makeCurrent(this);

	
	// Echo:

	if (_options.printInput)
	{
	    System.out.print("In/CNF/NConj: ("); 
	    Iterator<InputSyntax.Literal> lit = clause.iterator();
	    if (lit.hasNext())
	    {
		System.out.print(lit.next().toString());
		while (lit.hasNext())
		    System.out.print(" | " + lit.next());
	    };
	    System.out.println(")");
	};


	if (_options.preserveInput)
	    {
		Clause cl = inputLiteralListAsClause(clause,Clause.Role.NegatedConjecture);
		
		if (cl == null) 
		    // the clause was discarded, e.g., forward-subsumed
		    {
			// Switch back to the previous static context:
			makeCurrent(savedSession);
			return;
		    };
		_preservedInput.addLast(cl);
		_newClauseAssembler.copyClause(cl);
	    }
	else
	    {
		// Dump the clause in _newClauseAssembler:
		
		_newClauseAssembler.convertInputClause(clause,Clause.Role.NegatedConjecture);
	    };

	// Now the clause is in _newClauseQueue.

 	while (_forwardProcessing.processNext()) 
	{
	    // What remains of the clause is now in _retainedClauseQueue

 	    while (_persistentClauseAssembler.processNext()) 
 	    {
 		// nothing here: all the work is done in 
 		// _forwardProcessing.processNext() and 
 		// _persistentClauseAssembler.processNext()
 	    };

	    // What remains of the clause is now in _clauseSelection	    
	};

	// Switch back to the previous static context:
	makeCurrent(savedSession);

    } // addNegatedConjecture(Collection<InputSyntax.Literal> clause)



    //          Optional end-of-input event:

    /** Signals that there will be no more input, in which case
     *  the kernel session may work under the complete input assumption;
     *  calling this method is optional - the input may be incremental.
     */
    public void endOfInput() {
	// Check if the input has equality and set the corresponding flag.	
    }

    
    
//      Saturation in time-slicing mode:

    /** Instructs the kernel session to perform some limited
     *  amount of processing, eg, a few saturation steps;
     *  this kind of API allows running a kernel session in
     *  the time-slicing mode.
     *  Note that between calls to <code>doSomething()</code>, input methods
     *  can be called, thus making input incremental.
     *  <b>pre:</b> <code>!terminated()</code>
     */
    public void doSomething() 
	throws SwitcheableAbortFlag.Exception
    {	
	// Switch to this object as the current static context:
	Kernel savedSession = current();
	makeCurrent(this);

	// Check if there are new clauses from the previous
	// steps:

	while (_forwardProcessing.processNext()) {
	    
	    while (_persistentClauseAssembler.processNext()) 
		{
		    // Nothing here, everything is done 
		    // in _persistentClauseAssembler.processNext()
		};
	    
	    if (_backwardSimplificationCentre.doSomething())
		{
		    // Switch back to the previous static context:
		    makeCurrent(savedSession);

		    return;
		};
	}; // while (_forwardProcessing.processNext())

	
	if (!_generatingInferenceCentre.isBusy())
	    // It is safe to dispose of discarded clauses:
	    disposeOfDiscardedClauses();


	// No new clauses from the previous steps.
	// Try to generate some.

	boolean tryClauseSelection = shouldTryClauseSelection();

	if (tryClauseSelection &&
	    _clauseSelection.send(_selectedClause))
	    {
		++(Statistics.current().keptClauses.selected);
		if (!_selectedClause.content.isDiscarded())
		    if (_selectedClause.content.isForClausification()) 
			{
			    if (_options.printSelectedClauses)
				System.out.println("Sel/Clsf: " + 
						   _selectedClause.content);

			    _clausifier.process(_selectedClause.content);
			}
		    else
			{
			    if (_options.printSelectedClauses)
				System.out.println("Sel/GenInf: "  + 
						   _selectedClause.content);

			    // Annotate the clause with information on which 
			    // of its parts can be selected/unselected.
			    _literalSelection.receive(_selectedClause.content);
	      
			    // Note that the annotated clause is automatically sent
			    // to _fineSelectionUnitIdentificationCentre. 
			    // The selection units extracted in _fineSelectionUnitIdentificationCentre
			    // from the clause are automatically sent to _generatingInferenceCentre.
			    
			    
			    _generatingInferenceCentre.doSomething();

			}; // if (_selectedClause.content.isForClausification()) 
	    }
	else
	    {
		if (_generatingInferenceCentre.doSomething())
		    {
			// Nothing here currently.
		    }
		else
		    {
			if (tryClauseSelection)
			    {
				// Clause selection was attempted but failed.
			    
				// Nothing to do at all. Report termination.
				
				_terminationCode = TerminationCode.Saturation;
			    };
		    };

	    }; // if (shouldTryClauseSelection() && ..


	// Switch back to the previous static context:
	makeCurrent(savedSession);


    } // doSomething()
    






    
    //     Aborting:


    /** Aborts the kernel session (without the possibility 
     *  of resumption); abortion is done by throwing a
     *  {@link logic.is.power.cushion.SwitcheableAbortFlag.Exception}.
     *  Most importantly, this method can be
     *  invoked asynchronously, eg, during an execution of
     *  <code>doSomething()</code>
     *  or an input method. In particular, this allows aborting
     *  on alarms or other signals. Note that the abortion is not
     *  guaranteed to be immediate. The method essentially sets
     *  certain flag and it may take a while before the code in, 
     *  eg, <code>doSomething()</code> notices that the flag has been set.
     *  @param description can be used to supply additional information,
     *         eg, about what caused the abortion; this string
     *         can be later accessed as 
     *         <code>(String)(ex.flagObject().info())</code>, where
     *         <code>ex</code> is the corresponding instance of
     *         {@link logic.is.power.cushion.SwitcheableAbortFlag.Exception}.
     */
    public final void abort(String description) {

	_terminationCode = TerminationCode.Abortion;

	_abortFlag.set();
    }

    
    /** Same as <code>abort(null)</code>. */
    public final void abort() { abort(null); }

     /** <b>pre:</b> <code>isCurrent()</code> and the timer must not have been 
      *  stopped earlier 
      */
    public final void stopTimer() {
	assert isCurrent();

	// It is possible that !_timer.isTicking() here.
	
	_timer.stop();
    }
    
    /** <b>pre:</b> <code>isCurrent()</code> and the timer must have
     *  been stopped earlier. 
     */
    public final void restartTimer() {
	assert isCurrent();
	assert !_timer.isTicking();
	_timer.start();
    }


     /** Checks if the session has terminated smoothly, eg, 
      *  the current clause set is saturated wrt the specified
      *  strategy; note that completeness of the strategy is 
      *  not implied and, also, some clauses could have been 
      *  lost due to time/memory limitations.
      */
    public final boolean terminated() {
	return _terminationCode != TerminationCode.Unterminated;
    }
     
     /** <b>pre:</b> <code>terminated()</code> */
    public final TerminationCode terminationCode() {
	assert terminated();
	return _terminationCode;
     }


    public final Statistics statistics() { return _statistics; }
	  
    public String indexPerformanceSummary() {
	return _forwardSimplificationCentre.indexPerformanceSummary();
    }

    /** CPU time spent on behalf of this session. */
    public final int CPUTimeInMilliseconds() {
	return (int)_timer.elapsedMilliseconds();
    }

    /** Representation of equality within this kernel session. */
    public final Predicate equalityPredicate() {
	// temporary, will be parameterised by the equality print name.
	// See also logic_warehouse_je.Predicate.isEquality
	return _signature.equalityPredicate();
    }
    


    //             Package-access methods:

    /** Generates a fresh numeric id for a clause or a fine selection 
     *  unit, etc, which will be unique wrt this session.
     *  @return >= 0
     */
    public long generateFreshNumericId() {
	++_lastNumericId;
	return _lastNumericId;
    }


    int maximalSelectionUnitActiveness() {
	return _options.maximalSelectionUnitActiveness;
    }
    
	
    //             Private classes:
    
    private static class UnifiabilityEstimationLog 
	implements SimpleReceiver<Pair<Term,Float>> {

	/** Simply prints the pair in <code>System.out</code>. */
	public final boolean receive(Pair<Term,Float> p) {
	    System.out.println("Unifiability: " + p.first + 
			       " ---> " + (int)(p.second * 10000) + "/10000 (" +
			       ((float)(int)(p.second * 10000))/10000 + ")"); 
	    return true;
	}

    } // class UnifiabilityEstimationLog 

    /** Prepares a received clause for destruction by marking it
     *  as discarded; if it is possible to remove the clause
     *  and any dependent data, eg, selection units, from some indexes 
     *  or other sets, this is done; if some relevant indexes
     *  are locked, the clause is kept for future deletion from
     *  these indexes.
     */
    private class ClauseDiscarder implements SimpleReceiver<Clause> {
	
	
	public final boolean receive(Clause cl) {

	    cl.markAsDiscarded();

	    _disposalBuffer.addLast(cl);

	    return true;

	} // receive(Clause cl)


    } // class ClauseDiscarder




    //             Private methods:

    /** Removes all discarded clauses and their dependent data,
     *  such as selection units, from all indexes and sets.
     */
    private void disposeOfDiscardedClauses() {

	while (!_disposalBuffer.isEmpty())
	    disposeOfDiscardedClause(_disposalBuffer.removeFirst());

    } 

    
    /** Removes the discarded clause and its dependent data,
     *  such as selection units, from all indexes and sets.
     */
    private void disposeOfDiscardedClause(Clause cl) {
	
	assert cl.isDiscarded();

	_forwardSimplificationCentre.remove(cl);

	_backwardSimplificationCentre.remove(cl);

	_clauseSelection.erase(cl);
	
	for (FineSelectionUnit su = cl.selectionUnits();
	     su != null;
	     su = su.next())
	    _generatingInferenceCentre.remove(su);
	cl.discardSelectionUnits();
	

    } // disposeOfDiscardedClause(Clause cl)
    

    
    /** Decides, based on the state of this kernel session, 
     *  whether another clause should be selected in
     *  <code>_clauseSelection</code> and passed to the modules
     *  that identify fine selection units, etc.
     *  This function is "fair" in one direction: once in a while it returns 
     *  <code>true</code>
     *  (even if the database of passive clauses is empty),
     *  although there is no guarantee it will ever return <code>false</code>.
     */
    private boolean shouldTryClauseSelection() {
	
	assert _options.clauseSelectionDelay >= 0;

	++_clauseSelectionCheckCounter;

	return 
	    (_clauseSelectionCheckCounter % 
	     (_options.clauseSelectionDelay + 1)) == 0;

    } // shouldTryClauseSelection()



    /** Create a {@link #Clause} representing the specified input formula.
     *  @return null if the clause is discarded
     */ 
    private Clause inputFormulaAsClause(InputSyntax.Formula f,
					int role) {

	assert current() == this;

	// Dump the clause in _inputClauseAssembler:

	_inputClauseAssembler.convertInputFormula(f,role);
       

	Ref<NewClause> newClauseRef = new Ref<NewClause>();
	
	boolean newClauseAssembed = _inputClauseQueue.send(newClauseRef); 

	assert newClauseAssembed;

	if (!_forwardProcessing.testForRetention(newClauseRef.content))
	    return null;			
	
	_inputClauseQueue.receive(newClauseRef.content);
	// Note that _inputClauseQueue is the input for 
	// _persistentInputClauseAssembler

	Ref<Clause> persistentClauseRef = new Ref<Clause>();

	boolean persistentClauseAssembed = 
	    _persistentInputClauseAssembler.processNext() &&
	    _persistentInputClauseQueue.send(persistentClauseRef);

	assert persistentClauseAssembed;

	return persistentClauseRef.content;

    } // inputFormulaAsClause(InputSyntax.Formula f,



    /** Create a {@link #Clause} representing the specified input literal list.
     *  @return null if the clause is discarded
     */ 
    private Clause inputLiteralListAsClause(Collection<InputSyntax.Literal> lits,
					    int role) {


	assert current() == this;

	// Dump the clause in _inputClauseAssembler:

	_inputClauseAssembler.convertInputClause(lits,role);       

	Ref<NewClause> newClauseRef = new Ref<NewClause>();
	
	boolean newClauseAssembed = _inputClauseQueue.send(newClauseRef); 

	assert newClauseAssembed;

	if (!_forwardProcessing.testForRetention(newClauseRef.content))
	    return null;			
	
	_inputClauseQueue.receive(newClauseRef.content);
	// Note that _inputClauseQueue is the input for 
	// _persistentInputClauseAssembler

	Ref<Clause> persistentClauseRef = new Ref<Clause>();

	boolean persistentClauseAssembed = 
	    _persistentInputClauseAssembler.processNext() &&
	    _persistentInputClauseQueue.send(persistentClauseRef);

	assert persistentClauseAssembed;

	return persistentClauseRef.content;

    } // inputLiteralListAsClause(Collection<InputSyntax.Literal> lits,



    //             Data:


	
    /** Current active kernel session; null if no session is active. */
    static Kernel _current = null;

    private KernelOptions _options;


    /** Counts time during which this session is active, 
     *  ie, the time spent by method execution on this object.
     */
    private logic.is.power.cushion.Timer _timer;


    private SwitcheableAbortFlag _abortFlag;

    private TerminationCode _terminationCode;

    private Statistics _statistics;

    private Signature _signature;

    /** Switcheable static context for {@link logic.is.power.alabai.Clause}. */
    private Clause.Context _clauseContext;

    /** Switcheable static context for {@link logic.is.power.alabai.NewClause}. */
    private NewClause.Context _newClauseContext;


    private ReductionOrdering _reductionOrdering;
    
    /** Stores clauses waiting their turn to be forward-processed. */
    private SimpleQueue<NewClause> _newClauseQueue;
    
    /** Converts clauses and formulas received as input
     *  into {@link logic.is.power.alabai.NewClause} representation, and
     *  sends the results to the queue for forward processing;
     *  MIND the difference with _inputClauseAssembler.
     */
    private NewClauseAssembler _newClauseAssembler;    
    
    /** Stores input clauses to be saved after a retention test. */
    private SimpleRestrictedQueue<NewClause> _inputClauseQueue;

    /** Converts clauses and formulas received as input
     *  into {@link logic.is.power.alabai.NewClause} representation, and
     *  sends the results to the queue for input copies;
     *  MIND the difference with _newClauseAssembler.
     */
    private NewClauseAssembler _inputClauseAssembler;    


    private ForwardSimplificationCentre _forwardSimplificationCentre;

    private ConstraintSatisfiabilityCheckCentre _constraintSatisfiabilityCheckCentre;
    
    /** Stores clauses that just passed forward processing. */
    private SimpleQueue<NewClause> _retainedClauseQueue;

    private ForwardProcessing _forwardProcessing;

    /** Where clauses indended to be discarded are sent. */
    private ClauseDiscarder _clauseDiscarder;

    /** Where discarded clauses wait their chance to be disposed. */
    private LinkedList<Clause> _disposalBuffer;

    private BackwardSimplificationCentre _backwardSimplificationCentre;
      
    /** Receiver hub for <code>_forwardSimplificationCentre</code> and
     *  <code>_backwardSimplificationCentre</code>.
     */
    private SimpleReceiverConjunction<Clause> _clauseReceiverHub1;

    /** Database of kept clauses available for clause selection
     *  (not to be confused with fine selection!) and 
     *  the selection mechanism itself.
     */
    private ClauseSelection _clauseSelection;

    /** Receiver hub for <code>_forwardSimplificationCentre</code>,
     *  <code>_backwardSimplificationCentre</code> and 
     *  <code>_clauseSelection</code>.
     */
    private SimpleReceiverConjunction<Clause> _clauseReceiverHub2;

    /** Receiver hub for <code>_forwardSimplificationCentre</code> and 
     *  <code>terminalClauseReceiver</code> given as a parameter
     *  to the constructor.
     */
    private SimpleReceiverConjunction<Clause> _clauseReceiverHub3;
    
    private TerminalClauseFilter _terminalClauseFilter;

    private ClauseAssembler _persistentClauseAssembler;

    private SimpleRestrictedQueue<Clause> _persistentInputClauseQueue;

    private ClauseAssembler _persistentInputClauseAssembler;

    private TermFactory _sharedTermFactory;

    private Clausifier _clausifier;

    private GeneratingInferenceCentre _generatingInferenceCentre;

    private UnifiabilityEstimationLog _unifiabilityEstimationLog;
    
    private UnifiabilityEstimation _unifiabilityEstimation;

    private 
	FineSelectionUnitIdentificationCentre 
	_fineSelectionUnitIdentificationCentre;

    private LiteralSelection _literalSelection;

    /** Where the clause selection procedure returns its result. */
    private Ref<Clause> _selectedClause;

    private LinkedList<Clause> _preservedInput;

    /** Last number returned by {@link #generateFreshNumericId()}. */
    private long _lastNumericId;

    /** Counts calls to {@link #shouldTryClauseSelection()}. */
    private static long _clauseSelectionCheckCounter = 0; 

}; // class Kernel
