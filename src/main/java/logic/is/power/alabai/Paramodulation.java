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
 * Performs paramodulation inferences; in particular, maintains
 * the graded activeness scale for the corresponding selection units (see 
 * <a href="{@docRoot}/../references.html#new_architectures_position_paper">[New Architectures position paper]</a>
 *  ).
 * TODO: ordering constraint checks MUST be added to detect redundant 
 *       inferences!
 */
class Paramodulation {

    
    /** @param conclusionAssembler where the conclusions of inferences
     *         will be assembled
     */
    public Paramodulation(NewClauseAssembler conclusionAssembler) {

	_checkUnifierDirections = false; // default

	_conclusionAssembler = conclusionAssembler;
	
	_sideSelUnitWithPosEq = new Ref<FinSelUnEqLHSForParamod>();

	_sideSelUnitWithRedex = new Ref<FinSelUnRedexForParamod>();

	_eqLHSIndex =
	    new ClusteredUnificationIndex<FinSelUnEqLHSForParamod>();
    
	_redexIndex =
	    new ClusteredUnificationIndex<FinSelUnRedexForParamod>();
	
	_mainPremiseWithRedex = 
	    new MainPremiseWithRedexForParamodulation(conclusionAssembler);
	
	_sidePremiseWithPosEq =
	    new SidePremiseWithPosEqForParamodulation(conclusionAssembler);
    
	_mainPremiseWithPosEq = 
	    new MainPremiseWithPosEqForParamodulation(conclusionAssembler);

	_sidePremiseWithRedex = 
	    new SidePremiseWithRedexForParamodulation(conclusionAssembler);
	
	
	_eqLHSRetrieval = _eqLHSIndex.new Retrieval();
    
	_redexRetrieval = _redexIndex.new Retrieval();;

    } // Paramodulation(NewClauseAssembler conclusionAssembler)



    public void setCheckUnifierDirections(boolean fl) {
	_checkUnifierDirections = fl;
    }    
    
    /** Initiates promotion of the selection unit to a higher degree of activeness
     *  and begins generation of inferences with the selection unit;
     *  inferences are performed by later calls to {@link #performSomeInferences()}.
     *  @param selUnit must be an {@link FinSelUnEqLHSForParamodI},
     *         {@link FinSelUnEqLHSForParamodG},
     *         {@link FinSelUnEqLHSForParamodB} or
     *         {@link FinSelUnRedexForParamod}.
     *  @param lastPromotion is assigned a value indicating whether
     *         this is the last possible promotion for this unit
     */
    public void promote(FineSelectionUnit selUnit,
			Ref<Boolean> lastPromotion) {


	assert 
	    selUnit.kind() == FineSelectionUnit.Kind.EqLHSForParamodI ||
	    selUnit.kind() == FineSelectionUnit.Kind.EqLHSForParamodG ||
	    selUnit.kind() == FineSelectionUnit.Kind.EqLHSForParamodB ||
	    selUnit.kind() == FineSelectionUnit.Kind.RedexForParamod;

	if (selUnit.kind() == FineSelectionUnit.Kind.RedexForParamod)
	    {
		promoteRedex(selUnit,lastPromotion);	
	    }
	else
	    promoteEqLHS(selUnit,lastPromotion);

    } // promote(FineSelectionUnit selUnit,..)


      /** Generates some inferences with the most recently promoted
       *  selection unit.
       *  @return <code>false</code> if no more inferences can be done 
       *          with the selection unit being promoted
       */
    public boolean performSomeInferences() {
	
	if (_modeIsForward)
	    return performSomeForwardInferences();
	
	return performSomeBackwardInferences();

    } // performSomeInferences()


    /** Wraps up the current promotion; every promotion cycle must be finished
     *  with a call to this method, regardless of whether all inferences
     *  have been made or not.
     */
    public void finishCurrentPromotion() {

	if (_modeIsForward)
	    {
		_mainPremiseWithRedex.unload();
		
		if (_redexUnitBeingPromoted.activeness() == 1)
		    {
			// _redexUnitBeingPromoted is not in the index yet

			_redexIndex.insert(_redexUnitBeingPromoted.redex(),
					   _newCluster,
					   _redexUnitBeingPromoted);
					   

		    }
		else // _redexUnitBeingPromoted.activeness() != 1
		    {
			// _redexUnitBeingPromoted is already in the index
			_redexIndex.relocate(_redexUnitBeingPromoted.redex(),
					     _oldCluster,
					     _newCluster,
					     new Equals(_redexUnitBeingPromoted));
		    };

	    }
	else // !_modeIsForward
	    {
		_mainPremiseWithPosEq.unload();
		
		if (_eqLHSUnitBeingPromoted.activeness() == 1)
		    {
			// _eqLHSUnitBeingPromoted is not in the index yet
			_eqLHSIndex.insert(_eqLHSUnitBeingPromoted.lhs(),
					   _newCluster,
					   _eqLHSUnitBeingPromoted);
					   

		    }
		else // _eqLHSUnitBeingPromoted.activeness() != 1
		    {
			// _eqLHSUnitBeingPromoted is already in the index
			_eqLHSIndex.relocate(_eqLHSUnitBeingPromoted.lhs(),
					     _oldCluster,
					     _newCluster,
					     new Equals(_eqLHSUnitBeingPromoted));
		    };
	    };

    } // finishCurrentPromotion()
    
    
    /** Removes the selection unit from the internal database. 
     *  @param selUnit must be an {@link FinSelUnEqLHSForParamodI},
     *         {@link FinSelUnEqLHSForParamodG},
     *         {@link FinSelUnEqLHSForParamodB} or
     *         {@link FinSelUnRedexForParamod}.
     */
    public void remove(FineSelectionUnit selUnit) {

	assert 
	    selUnit.kind() == FineSelectionUnit.Kind.EqLHSForParamodI ||
	    selUnit.kind() == FineSelectionUnit.Kind.EqLHSForParamodG ||
	    selUnit.kind() == FineSelectionUnit.Kind.EqLHSForParamodB ||
	    selUnit.kind() == FineSelectionUnit.Kind.RedexForParamod;


	if (selUnit.kind() == FineSelectionUnit.Kind.RedexForParamod)
	    {
		Term redex = 
		    ((FinSelUnRedexForParamod)selUnit).redex();

		_redexIndex.erase(redex,
				  ((FinSelUnRedexForParamod)selUnit).cluster(),
				  new Equals(selUnit));
	    }
	else
	    {
		Term lhs = 
		    ((FinSelUnEqLHSForParamod)selUnit).lhs(); 

		_eqLHSIndex.erase(lhs,
				  ((FinSelUnEqLHSForParamod)selUnit).cluster(),
				  new Equals(selUnit));
		

	    };
	
    } // remove(FineSelectionUnit selUnit)

    



    //                           Private:
    
    


    private void promoteRedex(FineSelectionUnit selUnit,
			      Ref<Boolean> lastPromotion) {

	assert 
	    selUnit.kind() == FineSelectionUnit.Kind.RedexForParamod;

	_modeIsForward = true;

	_redexUnitBeingPromoted = (FinSelUnRedexForParamod)selUnit;
	    
	_oldCluster = _redexUnitBeingPromoted.cluster();

	_redexUnitBeingPromoted.incrementActiveness();
	
	_newCluster = _redexUnitBeingPromoted.cluster();
	

	_mainPremiseWithRedex.load(_redexUnitBeingPromoted.clause(),
				   _redexUnitBeingPromoted.literal(),
				   _redexUnitBeingPromoted.redexPosition());

	

	lastPromotion.content = 
	    (_redexUnitBeingPromoted.activeness() >= 
	     Kernel.current().maximalSelectionUnitActiveness());
	  	
	_complementaryClusters = 
	    _redexUnitBeingPromoted.complementaryClusters();

	assert !_complementaryClusters.isEmpty();

	_eqLHSRetrieval.
	    resetQuery(_mainPremiseWithRedex.redexFlatterm(),
		       _complementaryClusters.removeFirst());
		       
	_unifierSavepoint = 
	    _eqLHSRetrieval.unifier().savepoint();



    } // promoteRedex(FineSelectionUnit selUnit,..) 


    private void promoteEqLHS(FineSelectionUnit selUnit,
			      Ref<Boolean> lastPromotion) {


	assert 
	    selUnit.kind() == FineSelectionUnit.Kind.EqLHSForParamodI ||
	    selUnit.kind() == FineSelectionUnit.Kind.EqLHSForParamodG ||
	    selUnit.kind() == FineSelectionUnit.Kind.EqLHSForParamodB;
	
	_modeIsForward = false;

	_eqLHSUnitBeingPromoted = (FinSelUnEqLHSForParamod)selUnit;

	_oldCluster = _eqLHSUnitBeingPromoted.cluster();

	_eqLHSUnitBeingPromoted.incrementActiveness();
	
	_newCluster = _eqLHSUnitBeingPromoted.cluster();

	_mainPremiseWithPosEq.load(_eqLHSUnitBeingPromoted.clause(),
				   _eqLHSUnitBeingPromoted.literal(),
				   _eqLHSUnitBeingPromoted.argumentNumber());


	lastPromotion.content = 
	    (_eqLHSUnitBeingPromoted.activeness() >= 
	     Kernel.current().maximalSelectionUnitActiveness());
	  	
	_complementaryClusters = 
	    _eqLHSUnitBeingPromoted.complementaryClusters();


	// This selection unit can only interact
	// with FinSelUnRedexForParamod units:
	assert _complementaryClusters.size() == 1;


// 	if (_eqLHSUnitBeingPromoted.number() == 9 &&
// 	    _eqLHSUnitBeingPromoted.activeness() == 10)
// 	    System.out.println("RESET RETRIEVAL " + _mainPremiseWithPosEq.lhsFlatterm());


	_redexRetrieval.
	    resetQuery(_mainPremiseWithPosEq.lhsFlatterm(),
		       _complementaryClusters.removeFirst());
		       
	_unifierSavepoint = 
	    _redexRetrieval.unifier().savepoint();


    } // promoteEqLHS(FineSelectionUnit selUnit,..)




    private boolean performSomeForwardInferences() {
	
	while (_eqLHSRetrieval.retrieveNext(_sideSelUnitWithPosEq))
	    {
		assert _sideSelUnitWithPosEq.content != null;

		_sidePremiseWithPosEq.
		    load(_sideSelUnitWithPosEq.content.clause(),
			 _sideSelUnitWithPosEq.content.literal(),
			 _sideSelUnitWithPosEq.content.argumentNumber());
		
		if (forwardInferenceUnifierIsEligible())
		    {
			processForwardInferenceConclusion();
			return true;
		    };
		
		
	    }; // while (_eqLHSRetrieval.retrieveNext(_sideSelUnitWithPosEq))
	
	_eqLHSRetrieval.finish();

	if (_complementaryClusters.isEmpty())
	    return false; 


	_eqLHSRetrieval.
	    resetQuery(_mainPremiseWithRedex.redexFlatterm(),
		       _complementaryClusters.removeFirst());
	
	assert _unifierSavepoint ==
	    _redexRetrieval.unifier().savepoint();
	
	return performSomeForwardInferences();

    } // performSomeForwardInferences()





    private boolean performSomeBackwardInferences() {
	
	while (_redexRetrieval.retrieveNext(_sideSelUnitWithRedex))
	    {
		assert _sideSelUnitWithRedex.content != null;

		_sidePremiseWithRedex.
		    load(_sideSelUnitWithRedex.content.clause(),
			 _sideSelUnitWithRedex.content.literal(),
			 _sideSelUnitWithRedex.content.redexPosition());

		if (backwardInferenceUnifierIsEligible())
		    {
			processBackwardInferenceConclusion();
			return true;
		    };
		
	    }; // while (_redexRetrieval.retrieveNext(_sideSelUnitWithRedex))


	_redexRetrieval.finish();

	assert _complementaryClusters.isEmpty();

	return false; 

    } // performSomeBackwardInferences()




    /** Checks if the unifier is eligible wrt the types of 
     *  the current main and side selection units.
     *  <b>pre:</b> <code>_modeIsForward == true</code>
     */
    private boolean forwardInferenceUnifierIsEligible() {
	assert _modeIsForward;

//       	System.out.println("FORWARD\n    " + _redexUnitBeingPromoted + 
//       			   "\n    " + _sideSelUnitWithPosEq.content + 
//       			   "\n      : " + _eqLHSRetrieval.unifier());
			   

	if (!_checkUnifierDirections) return true;

	if (_sideSelUnitWithPosEq.content.kind() == 
	    FineSelectionUnit.Kind.EqLHSForParamodI)
	    {
		// Check that the substitution is instantiating
		// wrt the LHS and generalising wrt the redex.
		
		return 
		    Resolution.
		    substIsInstantiating(_eqLHSRetrieval.unifier(),
					 _unifierSavepoint,
					 _sidePremiseWithPosEq.lhsVariableSet(),
					 _mainPremiseWithRedex.redexVariableSet(),
					 false);

	    }; // if (_sideSelUnitWithPosEq.kind() == 



	if (_sideSelUnitWithPosEq.content.kind() == 
	    FineSelectionUnit.Kind.EqLHSForParamodG)
	    {
		// Check that the substitution is instantiating
		// wrt the redex and generalising wrt the LHS.

		// Note that we require it to be instantiating in the strong
		// sense to avoid some duplicate inferences.
		
		return 
		    Resolution.
		    substIsInstantiating(_eqLHSRetrieval.unifier(),
					 _unifierSavepoint,
					 _mainPremiseWithRedex.redexVariableSet(),
					 _sidePremiseWithPosEq.lhsVariableSet(),
					 true);

	    }; // if (_sideSelUnitWithPosEq.kind() == 

	if (_sideSelUnitWithPosEq.content.kind() == 
	    FineSelectionUnit.Kind.EqLHSForParamodB)
	    {
		// Check that at least one variable from the redex 
		// in the main premise, and at least one variable
		// from the LHS in the side premise have been
		// instantiated since _unifierSavepoint.
		for (Variable var1 :
			 _mainPremiseWithRedex.redexVariableSet())
		    if (_eqLHSRetrieval.
			unifier().
			variableWasInstantiatedAfter(var1,
						     _unifierSavepoint))
			{
			    for (Variable var2 :
				     _sidePremiseWithPosEq.lhsVariableSet())
				if (_eqLHSRetrieval.
				    unifier().
				    variableWasInstantiatedAfter(var2,
								 _unifierSavepoint))
				    return true;
			    return false;
			};

		return false;

	    }; // if (_sideSelUnitWithPosEq.kind() == 


	assert false;
	return false;

    } // forwardInferenceUnifierIsEligible()




    /** Checks if the unifier is eligible wrt the types of 
     *  the current main and side selection units.
     *  <b>pre:</b> <code>_modeIsForward == false</code>
     */
    private boolean backwardInferenceUnifierIsEligible() {
	assert !_modeIsForward;

//    	System.out.println("BACKWARD\n    " + _eqLHSUnitBeingPromoted +
//    			   "\n    " + _sideSelUnitWithRedex.content + 
//    			   "\n      : " + _redexRetrieval.unifier());


	if (!_checkUnifierDirections) return true;

	if (_eqLHSUnitBeingPromoted.kind() == 
	    FineSelectionUnit.Kind.EqLHSForParamodI)
	    {
		// Check that the substitution is instantiating
		// wrt the LHS and generalising wrt the redex.
		
		return 
		    Resolution.
		    substIsInstantiating(_redexRetrieval.unifier(),
					 _unifierSavepoint,
					 _mainPremiseWithPosEq.lhsVariableSet(),
					 _sidePremiseWithRedex.redexVariableSet(),
					 false);
		
	    }; // if (_eqLHSUnitBeingPromoted.kind() == 
	

	if (_eqLHSUnitBeingPromoted.kind() == 
	    FineSelectionUnit.Kind.EqLHSForParamodG)
	    {
		// Check that the substitution is instantiating
		// wrt the redex and generalising wrt the LHS .
				
		// Note that we require it to be instantiating in the strong
		// sense to avoid some duplicate inferences.

		return 
		    Resolution.
		    substIsInstantiating(_redexRetrieval.unifier(),
					 _unifierSavepoint,
					 _sidePremiseWithRedex.redexVariableSet(),
					 _mainPremiseWithPosEq.lhsVariableSet(),
					 true);
		
	    }; // if (_eqLHSUnitBeingPromoted.kind() == 
	



	if (_eqLHSUnitBeingPromoted.kind() == 
	    FineSelectionUnit.Kind.EqLHSForParamodB)
	    {
		// Check that at least one variable from the LHS 
		// in the main premise, and at least one variable
		// from the redex in the side premise have been
		// instantiated since _unifierSavepoint.
		
		for (Variable var1 :
			 _mainPremiseWithPosEq.lhsVariableSet())
		    if (_redexRetrieval.
			unifier().
			variableWasInstantiatedAfter(var1,
						     _unifierSavepoint))
			{

			    for (Variable var2 :
				     _sidePremiseWithRedex.redexVariableSet())
				{
				    if (_redexRetrieval.
					unifier().
					variableWasInstantiatedAfter(var2,
								     _unifierSavepoint))
					    return true;
				};
			    return false;
			};

		return false;
	
	    }; // if (_eqLHSUnitBeingPromoted.kind() == 
	
	assert false;
	return false;

    } // backwardInferenceUnifierIsEligible() 
	


    /** Creates a conclusion based on the contents of
     *  <code>_mainPremiseWithRedex</code> and <code>_sidePremiseWithPosEq</code>
     *  in <code>_conclusionAssembler</code>.
     */
    private void processForwardInferenceConclusion() {
	assert _modeIsForward;

	if (_redexUnitBeingPromoted.isDiscarded() ||
	    _sideSelUnitWithPosEq.content.isDiscarded())
	    return;
	
	_conclusionAssembler.
	    openClause(InferenceType.ForwardParamodulation);
	
	_conclusionAssembler.addParent(_redexUnitBeingPromoted.clause());
	_conclusionAssembler.addParent(_sideSelUnitWithPosEq.content.clause());

	_mainPremiseWithRedex.assembleLiterals(_sidePremiseWithPosEq.rhsFlatterm());
	_sidePremiseWithPosEq.assembleResidualLiterals();

	_conclusionAssembler.endOfClause();
	
	++(Statistics.current().deductionInferences.forwardParamodulation.accomplished);

	assert GlobalEventCounter.inc();

    } // processForwardInferenceConclusion()


    /** Creates a conclusion based on the contents of
     *  <code>_mainPremiseWithPosEq</code> and <code>_sidePremiseWithRedex</code>
     *  in <code>_conclusionAssembler</code>.
     */
    private void processBackwardInferenceConclusion() {
	assert !_modeIsForward;
	
	if (_eqLHSUnitBeingPromoted.isDiscarded() ||
	    _sideSelUnitWithRedex.content.isDiscarded())
	    return;

	_conclusionAssembler.
	    openClause(InferenceType.BackwardParamodulation);
	
	_conclusionAssembler.addParent(_eqLHSUnitBeingPromoted.clause());
	_conclusionAssembler.addParent(_sideSelUnitWithRedex.content.clause());

	_mainPremiseWithPosEq.assembleResidualLiterals();
	_sidePremiseWithRedex.assembleLiterals(_mainPremiseWithPosEq.rhsFlatterm());

	_conclusionAssembler.endOfClause();

	++(Statistics.current().deductionInferences.backwardParamodulation.accomplished);
	
	assert GlobalEventCounter.inc();

    } // processBackwardInferenceConclusion()



    //                           Data:


    /** Indicates that the "directions" of unifiers should be taken 
     *  into account in forwardInferenceUnifierIsEligible()
     *  and backwardInferenceUnifierIsEligible().
     */
    private boolean _checkUnifierDirections;

    private NewClauseAssembler _conclusionAssembler;

    /** Indicates if the current mode is forward, ie,
     *  the main premise is a redex and side premises must 
     *  be positive equalities.
     */
    private boolean _modeIsForward;

    private FinSelUnRedexForParamod _redexUnitBeingPromoted;

    private Ref<FinSelUnEqLHSForParamod> _sideSelUnitWithPosEq;

    private FinSelUnEqLHSForParamod _eqLHSUnitBeingPromoted;

    private Ref<FinSelUnRedexForParamod> _sideSelUnitWithRedex; 
    
    private BitSet _oldCluster;

    private BitSet _newCluster;

    private LinkedList<BitSet> _complementaryClusters;


    private 
	ClusteredUnificationIndex<FinSelUnEqLHSForParamod> 
	_eqLHSIndex;

    
    private 
	ClusteredUnificationIndex<FinSelUnRedexForParamod> 
	_redexIndex;

    private 
	ClusteredUnificationIndex<FinSelUnEqLHSForParamod>.Retrieval
	_eqLHSRetrieval;

    
    private 
	ClusteredUnificationIndex<FinSelUnRedexForParamod>.Retrieval
	_redexRetrieval;

    private int _unifierSavepoint;


    private MainPremiseWithRedexForParamodulation _mainPremiseWithRedex;

    private SidePremiseWithPosEqForParamodulation _sidePremiseWithPosEq;


    private MainPremiseWithPosEqForParamodulation _mainPremiseWithPosEq;

    private SidePremiseWithRedexForParamodulation _sidePremiseWithRedex;

} // class Paramodulation