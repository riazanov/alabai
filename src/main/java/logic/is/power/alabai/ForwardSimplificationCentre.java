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


/**
 * Hub for the main forward simplification operations, 
 * including forward subsumption, forward subsumption resolution,
 * and forward demodulation.
 */
public final class ForwardSimplificationCentre implements SimpleReceiver<Clause> {

    public ForwardSimplificationCentre() {
	_quickEqualityPretestInSubsumption = false;
	_onlyEqualityTest = false;
	_subsumptionIndex = new ForwardSubsumptionIndex();
	_demodulation = new ForwardDemodulation(); 
    }
       
    
    /** Switches forward subsumption. 
     *  @param subsumptionFilter if non-null, it is evaluated when subsumption 
     *         is attempted, and the subsumption is cancelled if the result 
     *         of the evaluation is <code>false</code>.
     *  @param subsumptionHook if non-null, it is executed when subsumption happens
     */
    public void setForwardSubsumption(boolean fl,
				      BinaryPredicateObject<NewClause,Clause> subsumptionFilter,
				      BinaryProcedureObject<NewClause,Clause> subsumptionHook) {
	_subsumption = fl;
	_subsumptionFilter = subsumptionFilter;
	_subsumptionHook = subsumptionHook;
    }


    /** @param subsumptionFilter if non-null, it is evaluated when subsumption 
     *         is attempted, and the subsumption is cancelled if the result 
     *         of the evaluation is <code>false</code>.
     */
    public void setForwardSubsumptionFilter(BinaryPredicateObject<NewClause,Clause> subsumptionFilter) {
	_subsumptionFilter = subsumptionFilter;
    }

    /** @param subsumptionHook if non-null, it is executed when subsumption happens */
    public void setForwardSubsumptionHook(BinaryProcedureObject<NewClause,Clause> subsumptionHook) {
	_subsumptionHook = subsumptionHook;
    }


    /** Switches quick equality pretest in forward subsumption,
     *  if <code>fl == true</code>, the subsumption procedure will
     *  try to quickly find an equal clause before trying other possibilities.
     */
    public void setQuickEqualityPretestInForwardSubsumption(boolean fl) {
	_quickEqualityPretestInSubsumption = fl;
    }

    /** Switches proper subsumption test: if <code>fl == true</code>, 
     *  only the cheap equality test will be used.
     */
    public void setOnlyEqualityTest(boolean fl) {
	_onlyEqualityTest = fl;
    }


    /** Switches forward subsumption resolution. */
    public void setForwardSubsumptionResolution(boolean fl) {
	_subsumptionResolution = fl;
    }

    /** Switches forward demodulation. */
    public void setForwardDemodulation(boolean fl) {
	_demodulationFlag = fl;
    }

    public void setClauseFeatureFunctionVector(ClauseFeatureVector vector)
    {
	_subsumptionIndex.setFeatureFunctionVector(vector);

	_subsumptionRetrieval = _subsumptionIndex.new Retrieval(); 
	// This is not in the constructor because a retrieval object
	// can only be created after the feature function vector is set.
    }

    public void setTermFeatureFunctionVector(TermFeatureVector vector)
    {
	_demodulation.setTermFeatureFunctionVector(vector);
    }

    
    public synchronized String indexPerformanceSummary() {

	return _subsumptionIndex.performanceSummary();

    }


    /** Integrates the clause in the corresponding databases for
     *  various simplifications.
     *  @return always true
     */
    public synchronized final boolean receive(Clause cl) {
	if (_subsumption || _subsumptionResolution)
	    _subsumptionIndex.insert(cl);

	if (_demodulationFlag)
	    _demodulation.insert(cl);

	return true;

    } // receive(Clause cl)

    /** Removes the clause from the corresponding databases for
     *  various simplifications, it it is present there.
     */
    public synchronized final boolean remove(Clause cl) {
	boolean result = false;

	if ((_subsumption || _subsumptionResolution) &&
	    _subsumptionIndex.erase(cl))
	    result = true;
	
	if (_demodulationFlag && _demodulation.remove(cl))
	    result = true;

	return result;
	    
    }

    
    /** Tries to subsume the clause. */
    public synchronized final boolean subsume(NewClause cl) {
	
	if (!_subsumption) return false;
	
	if (_quickEqualityPretestInSubsumption ||
	    _onlyEqualityTest) 
	    {
		Clause subsumer = 
		    _subsumptionIndex.findEqualClause(cl.literals());
		if (subsumer != null &&
		    (_subsumptionFilter == null ||
		     _subsumptionFilter.evaluate(cl,subsumer)))
		    {		
			++(Statistics.current().
			   forwardSimplifyingInferences.subsumption);	
			if (_subsumptionHook != null)
			    _subsumptionHook.execute(cl,subsumer);
			return true;
		    };
	    };

	if (_onlyEqualityTest) return false;
	
	_subsumptionRetrieval.reset(cl.literals());


	while (_subsumptionRetrieval.hasNext())
	    {
		Clause subsumer = _subsumptionRetrieval.next();

		assert subsumer != null;

		if (_subsumptionFilter == null ||
		    _subsumptionFilter.evaluate(cl,subsumer))
		    {
			++(Statistics.current().
			   forwardSimplifyingInferences.subsumption);
			
			if (_subsumptionHook != null)
			    _subsumptionHook.execute(cl,subsumer);

			_subsumptionRetrieval.clear();
			
			return true;
		    };

	    }; // while (_subsumptionRetrieval.hasNext())


	_subsumptionRetrieval.clear();
	
	return false;

    } // subsume(NewClause cl) 
    


    /** Tries to simplify the clause by subsumption resolution. */
    public synchronized final boolean simplifyBySubsumptionResolution(NewClause cl) {
	
	if (!_subsumptionResolution) return false;

	boolean result = false;
	
	Iterator<TmpLiteral> iter = cl.literals().iterator();

	while (iter.hasNext())
	    {
		TmpLiteral lit = iter.next();
		lit.flipPolarity();

		boolean litSimplified = false;

		_subsumptionRetrieval.reset(cl.literals());

		while (_subsumptionRetrieval.hasNext())
		    {
			Clause simplifier = 
			    _subsumptionRetrieval.next();
			if (!simplifier.isDiscarded())
			    {
				litSimplified = true;

				result = true;

				// This literal can be removed:
				iter.remove();
				
				cl.addInference(InferenceType.ForwardSubsumptionResolution);
				cl.addParent(simplifier);
				

				++(Statistics.current().
				   forwardSimplifyingInferences.
				   subsumptionResolution);
			
				break;
			    }; // if (!simplifier.isDiscarded())
		    }; // while (_subsumptionRetrieval.hasNext())

		_subsumptionRetrieval.clear();
		
		if (!litSimplified) lit.flipPolarity();

	    }; // while (iter.hasNext())
       
	return result;
    } // simplifyBySubsumptionResolution(NewClause cl)


    /** Tries to simplify the clause as much as possible by forward 
     *  demodulation. 
     */
    public synchronized final boolean simplifyByDemodulation(NewClause cl) {
	return _demodulationFlag && 
	    _demodulation.simplify(cl);
    }


    //                Data:


    private boolean _subsumption;

    
    /** If non-null, it is evaluated when subsumption is attempted, 
     *  and the subsumption is cancelled if the result of the evaluation
     *  is <code>false</code>.
     */
    private BinaryPredicateObject<NewClause,Clause> _subsumptionFilter;

    /** If non-null, it is executed when subsumption happens. */
    private BinaryProcedureObject<NewClause,Clause> _subsumptionHook;

    private boolean _quickEqualityPretestInSubsumption;

    private boolean _onlyEqualityTest;

    private boolean _subsumptionResolution;

    private boolean _demodulationFlag;

    private final ForwardSubsumptionIndex _subsumptionIndex;
    
    private ForwardSubsumptionIndex.Retrieval _subsumptionRetrieval;

    private final ForwardDemodulation _demodulation;

}; // class ForwardSimplificationCentre
