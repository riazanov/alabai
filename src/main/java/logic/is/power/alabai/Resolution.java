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
 * Performs binary resolution inferences; in particular, maintains
 * the graded activeness scale for the corresponding selection units (see 
 * <a href="{@docRoot}/../references.html#new_architectures_position_paper">[New Architectures position paper]</a>
 *  ).
 * TODO: delayed ordering constraint checks may be added 
 */
class Resolution {

    /** @param resolventAssembler where the resolvents will be assembled
     */
    public Resolution(NewClauseAssembler resolventAssembler) {
	_checkUnifierDirections = false; // default
	_resolventAssembler = resolventAssembler;
	_mainPremise = new MainPremiseWithFactoring(resolventAssembler); 
	_sidePremise = new SidePremiseWithFactoring(resolventAssembler); 
	_positiveLiteralIndex = new ClusteredUnificationIndex<FinSelUnResolution>();
	_positiveLiteralRetrieval = _positiveLiteralIndex.new Retrieval();
	_negativeLiteralIndex = new ClusteredUnificationIndex<FinSelUnResolution>();
	_negativeLiteralRetrieval = _negativeLiteralIndex.new Retrieval(); 
	_sideSelUnit = new Ref<FinSelUnResolution>();
    }

    public void setCheckUnifierDirections(boolean fl) {
	_checkUnifierDirections = fl;
    }


    /** Initiates promotion of the selection unit to a higher degree of activeness
     *  and begins generation of inferences with the selection unit;
     *  inferences are performed by later calls to {@link #performSomeInferences()}.
     *  @param selUnit must be an {@link alabai_je.FinSelUnResolutionI},
     *         {@link alabai_je.FinSelUnResolutionG} or {@link alabai_je.FinSelUnResolutionB}
     *  @param lastPromotion is assigned a value indicating whether
     *         this is the last possible promotion for this unit
     */
    public void promote(FineSelectionUnit selUnit,
			Ref<Boolean> lastPromotion) {
			
	  
	assert selUnit.kind() == FineSelectionUnit.Kind.ResolutionI ||
	    selUnit.kind() == FineSelectionUnit.Kind.ResolutionG ||
	    selUnit.kind() == FineSelectionUnit.Kind.ResolutionB;
	  
	_unitBeingPromoted = (FinSelUnResolution)selUnit; 
  
	_oldCluster = _unitBeingPromoted.cluster();

	_unitBeingPromoted.incrementActiveness();
 
	_newCluster = _unitBeingPromoted.cluster();

	_mainPremise.load(_unitBeingPromoted.clause(),
			  _unitBeingPromoted.literal());
 
	_mainPremise.resetFactoring();


	lastPromotion.content = 
	    (_unitBeingPromoted.activeness() >= 
	     Kernel.current().maximalSelectionUnitActiveness());
	  
	_currentRetrieval = 
	    retrieval(_unitBeingPromoted.literal().isPositive());

	LinkedList<BitSet> 
	    complementaryClusters = 
	    _unitBeingPromoted.complementaryClusters();
	
	assert complementaryClusters.size() == 1;
	// This is an implication of the current choice of selection
	// units for resolution.
	

	_currentRetrieval.
	    resetQuery(_mainPremise.activeLiteralFlatterm().atom(),
		       complementaryClusters.getFirst());

	
	_unifierSavepoint = 
	    _currentRetrieval.unifier().savepoint();

	_retrievalInProgress = false;

    } // promote(FineSelectionUnit selUnit,Ref<boolean> lastPromotion) 
      




      /** Generates some inferences with the most recently promoted
       *  selection unit.
       *  @return <code>false</code> if no more inferences can be done with the selection unit 
       *          being promoted
       */
    public boolean performSomeInferences() {
	

	if (!_retrievalInProgress)
	    {
		_retrievalInProgress = true;

		
		while (_currentRetrieval.retrieveNext(_sideSelUnit))
		    {
			_sidePremise.load(_sideSelUnit.content.clause(),
					  _sideSelUnit.content.literal());
			
			if (unifierIsEligible())
			    {
				_sidePremise.resetFactoring();
				
				processResolvent();
				
				return true;
			    };

		    }; // while (_currentRetrieval.retrieveNext(_sideSelUnit))

		_currentRetrieval.finish();
		
		return false;

	    }; // if (!_retrievalInProgress)

	
	
	// Try another factoring on _sidePremise:
	
	if (!_sidePremise.makeAnotherFactoringInference())
	    {
		_sidePremise.resetFactoring();
		// Try another factoring on _mainPremise:
		
		if (!_mainPremise.makeAnotherFactoringInference())
		    {
			_mainPremise.resetFactoring();

			// Try to retrieve another side selection unit,
			// possibly changing the unifier:
			
			boolean retrievalSuccessful;
			while ((retrievalSuccessful = 
				_currentRetrieval.retrieveNext(_sideSelUnit)))
			    {
				_sidePremise.load(_sideSelUnit.content.clause(),
						  _sideSelUnit.content.literal());
				
				if (unifierIsEligible())
				    break;
			    };

			if (!retrievalSuccessful)
			    {
				_currentRetrieval.finish();
				return false;
			    };
			


			_sidePremise.load(_sideSelUnit.content.clause(),
					  _sideSelUnit.content.literal());
			
			_sidePremise.resetFactoring();
			    
		    }; // if (!_mainPremise.makeAnotherFactoringInference())
		
	    }; // if (!_sidePremise.makeAnotherFactoringInference())

	processResolvent();
	
	return true;

    } // performSomeInferences()




    /** Wraps up the current promotion; every promotion cycle must be finished
     *  with a call to this method, regardless of whether all inferences
     *  have been made or not.
     */
    public void finishCurrentPromotion() {

	_mainPremise.unload();
	  
	Literal lit = _unitBeingPromoted.literal();
	  
	if (_unitBeingPromoted.activeness() == 1)
	{
	    // _unitBeingPromoted is not in the index yet
	    index(lit.isPositive()).insert(lit.atom(),
					   _newCluster,
					   _unitBeingPromoted);
	}
	else // _unitBeingPromoted.activeness() != 1
	{
	    // _unitBeingPromoted is already in the index
	    index(lit.isPositive()).relocate(lit.atom(),
					     _oldCluster,
					     _newCluster,
					     new Equals(_unitBeingPromoted));
	};
	
	_sideSelUnit.content = null;
	
    } // finishCurrentPromotion()

    /** Removes the selection unit from the internal database. 
     *  @param selUnit must be an {@link alabai_je.FinSelUnResolutionI},
     *         {@link alabai_je.FinSelUnResolutionG} or 
     *         {@link alabai_je.FinSelUnResolutionB};
     *         it does not have to be in the internal database
     */
    public void remove(FineSelectionUnit selUnit) {
	  
	assert selUnit.kind() == FineSelectionUnit.Kind.ResolutionI ||
	    selUnit.kind() == FineSelectionUnit.Kind.ResolutionG ||
	    selUnit.kind() == FineSelectionUnit.Kind.ResolutionB;

	FinSelUnResolution resSelUn = (FinSelUnResolution)selUnit;
	  
	Literal lit = resSelUn.literal();

	index(lit.isPositive()).erase(lit.atom(),
				      resSelUn.cluster(),
				      new Equals(selUnit));

    } // remove(FineSelectionUnit selUnit)



      //                           Package access methods:



    /** Checks that the substitution part after the specified savepoint
     *  which unifies some terms <code>t1</code> and <code>t2</code>, is
     * <a href="{@docRoot}/../glossary.html#instantiating_unifier">instantiating</a> 
     *  wrt <code>t1</code>; 
     *  the free variables of <code>t1</code> and <code>t2</code> 
     *  are given as <code>variables1</code> and <code>variables2</code>.
     *  Practically, this means that either (A) no variable from <code>variables2</code>
     *  has been instantiated since <code>savepoint</code>, or
     *  (B) no variable from <code>variables1</code> has been instantiated,
     *  and all variables from <code>variables2</code> instantiated
     *  since <code>savepoint</code> were instantiated by distinct free variables.
     *  Parameter <code>strong</code> specifies if the substitution must be instantiating
     *  in a strong sense which requires at least one of the variables from 
     *  <code>variables1</code> to be affected.
     */
    /* package */ 
    static boolean substIsInstantiating(Substitution1 subst,
					int savepoint,
					Set<Variable> variables1,
					Set<Variable> variables2,
					boolean strong) {

	Iterator<Variable> iter = variables2.iterator();
	
	while (iter.hasNext())
	    {
		Variable var = iter.next();

		if (subst.variableWasInstantiatedAfter(var,savepoint))
		    {
			// Situation (B) is still possible:

			Flatterm targetTerm = var.ultimateInstance1();
			
			if (!targetTerm.isVariable()) return false;
			
			TreeSet<Variable> targetVars = new TreeSet<Variable>();
			
			targetVars.add(targetTerm.variable());
			
			while (iter.hasNext())
			    {
				var = iter.next();

				if (subst.variableWasInstantiatedAfter(var,savepoint))
				    {
					targetTerm = var.ultimateInstance1();
					
					if (!targetTerm.isVariable()) return false;
					
					if (targetVars.contains(targetTerm.variable()))
					    return false;
					
					targetVars.add(targetTerm.variable());
					
				    }; // if (subst.variableWasInstantiatedAfter(var,savepoint))		

			    }; // while (iter.hasNext())
			
			// If we are here, we have situation (B).

			if (strong)
			    {
				for (Variable var1 : variables1)
				    if (subst.variableWasInstantiatedAfter(var1,savepoint) || 
					targetVars.contains(var1)) 
					return true;
				return false;
			    };
			

			return true;
			
		    }; // if (subst.variableWasInstantiatedAfter(var,savepoint))

	    }; // while (iter.hasNext())
	

	// Situation (A).

	if (strong)
	    {
		for (Variable var1 : variables1)
		    if (subst.variableWasInstantiatedAfter(var1,savepoint))
			return true;
		return false;
	    };

	return true;

    } // substIsInstantiating(Substitution1 subst,..)




      //                           Private:


      /** Index for literals of the specified polarity. */
    private 
    final 
    ClusteredUnificationIndex<FinSelUnResolution> index(boolean positive) {
	return (positive)? _positiveLiteralIndex : _negativeLiteralIndex;
    }

      /** Retrieval index for literals of the specified polarity. */
    private final ClusteredUnificationIndex<FinSelUnResolution>.Retrieval 
	retrieval(boolean positive) {
	return (positive)? _negativeLiteralRetrieval : _positiveLiteralRetrieval;
    }


    /** Creates a resolvent based on the contents of
     *  <code>_mainPremise</code> and <code>_sidePremise</code>
     *  in <code>_resolventAssembler</code>.
     */
    private void processResolvent() {


	if (_unitBeingPromoted.isDiscarded() ||
	    _sideSelUnit.content.isDiscarded())
	    return;
	    

	_resolventAssembler.
	    openClause(InferenceType.Resolution);

	_resolventAssembler.addParent(_unitBeingPromoted.clause());
	_resolventAssembler.addParent(_sideSelUnit.content.clause());

	_mainPremise.assembleResidualLiterals();
	_sidePremise.assembleResidualLiterals();


	_resolventAssembler.endOfClause();

	++(Statistics.current().deductionInferences.resolution.accomplished);
	
	assert GlobalEventCounter.inc();

    } // processResolvent()


    /** Checks if the unifier is eligible wrt the types of 
     *  the current main and side selection units.
     */
    private boolean unifierIsEligible() {
	
	if (!_checkUnifierDirections) return true;
	    
	//System.out.println("Main " + _unitBeingPromoted);
	//System.out.println("Side " + _sideSelUnit.content);
	//System.out.println("    Unifier: " + _currentRetrieval.unifier() + "\n");

	if (_unitBeingPromoted.kind() == FineSelectionUnit.Kind.ResolutionI)
	    {
		// Check that the substitution is instantiating
		// wrt the main premise and generalising wrt
		// the side premise.

		// Note that if _mainPremise.activeLiteralFlatterm().isPositive(),
		// the substitution has to be instantiating in the strong sense.
		// This is done to avoid some duplicate inferences.

		return 
		    substIsInstantiating(_currentRetrieval.unifier(),
					 _unifierSavepoint,
					 _mainPremise.activeLiteralVariableSet(),
					 _sidePremise.activeLiteralVariableSet(),
					 _mainPremise.activeLiteralFlatterm().isPositive());
	    };

	if (_unitBeingPromoted.kind() == FineSelectionUnit.Kind.ResolutionG)
	    {
		// Check that the substitution is instantiating
		// wrt the side premise and generalising wrt
		// the main premise.

		// Note that if _sidePremise.activeLiteralFlatterm().isPositive(),
		// the substitution has to be instantiating in the strong sense.
		// This is done to avoid some duplicate inferences.

		return 
		    substIsInstantiating(_currentRetrieval.unifier(),
					 _unifierSavepoint,
					 _sidePremise.activeLiteralVariableSet(),
					 _mainPremise.activeLiteralVariableSet(),
					 _sidePremise.activeLiteralFlatterm().isPositive());

	    };

	if (_unitBeingPromoted.kind() == FineSelectionUnit.Kind.ResolutionB)
	    {
		// Check that at least one variable from the active literal
		// in the main premise and at least one variable from 
		// the active literal in the side premise has been instantiated 
		// since _unifierSavepoint.

		for (Variable var1 : _mainPremise.activeLiteralVariableSet())
		    if (_currentRetrieval.
			unifier().
			variableWasInstantiatedAfter(var1,
						     _unifierSavepoint))
			{
			    for (Variable var2 :
				     _sidePremise.activeLiteralVariableSet())
				if (_currentRetrieval.
				    unifier().
				    variableWasInstantiatedAfter(var2,
								 _unifierSavepoint))
				    return true;
			    return false;
			};
		return false;

	    }; // if (_unitBeingPromoted.kind() == FineSelectionUnit.Kind.ResolutionB)


	assert false;
	return false;

    } // unifierIsEligible()



    //                           Data:

    /** Indicates that the "directions" of unifiers should be taken 
     *  into account in unifierIsEligible().
     */
    private boolean _checkUnifierDirections;

    private NewClauseAssembler _resolventAssembler;

    private MainPremiseWithFactoring _mainPremise;

    private SidePremiseWithFactoring _sidePremise;


    private 
	ClusteredUnificationIndex<FinSelUnResolution> 
	_positiveLiteralIndex;

    private 
	ClusteredUnificationIndex<FinSelUnResolution>.Retrieval 
	_positiveLiteralRetrieval;

    private 
	ClusteredUnificationIndex<FinSelUnResolution> 
	_negativeLiteralIndex;

    private 
	ClusteredUnificationIndex<FinSelUnResolution>.Retrieval 
	_negativeLiteralRetrieval;
      
    private FinSelUnResolution _unitBeingPromoted;

    private BitSet _oldCluster;

    private BitSet _newCluster;

    private 
	ClusteredUnificationIndex<FinSelUnResolution>.Retrieval
	_currentRetrieval;

    private int _unifierSavepoint;

    private boolean _retrievalInProgress;

    private Ref<FinSelUnResolution> _sideSelUnit;

}; // class Resolution
