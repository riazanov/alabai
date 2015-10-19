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

/** Index for backward subsumption and backward subsumption resolution. */
final class BackwardSubsumptionIndex {

    public BackwardSubsumptionIndex() {
	_locks = 0;
    }

    /** Feature functions that will be used to compute feature
     *  vector on indexed clauses and queries;
     *  <b>pre:</b> the index is empty.
     */
    public 
	final
	void 
	setFeatureFunctionVector(ClauseFeatureVector vector) {
	_featureFunctionVector = vector;
	_featureVector = 
	    new FeatureVector.ArrayBased(vector.size());
	_index = new FeatureVectorIndex<LinkedList<Clause>>(vector.size());
    }

    /** Indicates if the index is in use by some retrieval objects. */
    public final boolean isLocked() { return _locks > 0; }

    /** <b>pre:</b> <code>!isLocked()</code> */
    public final void insert(Clause cl) {

	//System.out.println("BS INSERT " + cl);

	assert !isLocked();

	if (cl.literals().isEmpty()) return;

	_featureFunctionVector.
	    evaluate(cl.literals(),
		     _featureVector,
		     0);

	Ref<LinkedList<Clause>> indexedObject =
	    _index.insert(_featureVector);

	if (indexedObject.content == null)
	    indexedObject.content = new LinkedList<Clause>();
	
	indexedObject.content.addLast(cl);
	
    } // insert(Clause cl)

    
    /** <b>pre:</b> <code>!isLocked()</code>
     *  @return false if the clause is not in the index 
     */
    public final boolean erase(Clause cl) {

	//System.out.println("BS ERASE " + cl);

	assert !isLocked();

	_featureFunctionVector.
	    evaluate(cl.literals(),
		     _featureVector,
		     0);

	Ref<LinkedList<Clause>> indexedObject = 
	    _index.find(_featureVector);

	if (indexedObject == null) return false;
	
	assert indexedObject.content != null;
	assert !indexedObject.content.isEmpty();
	
	if (!indexedObject.content.remove(cl)) 
	    return false;

	if (indexedObject.content.isEmpty())
	    _index.remove(_featureVector,
			  new Ref<LinkedList<Clause>>());

	return true; 

    } // erase(Clause cl)



    /** Iteration over all (undiscarded) indexed clauses subsumed 
     *  by a given query clause.
     */
    public class Retrieval implements java.util.Iterator<Clause> {
	
	/** <b>pre:</b> 
	 *  {@link logic_warehouse_je#BackwardSubsumptionIndex#setFeatureFunctionVector()} 
	 *  must have been called.
	 */
	public Retrieval() {
	    _leaves = _index.new Subsumed();
	    _queryFeatureVector = 
		new FeatureVector.ArrayBased(_featureFunctionVector.size());
	    _oneToOneSubsumption = new BackwardSubsumption();
	    _witnessSubstitution = new Substitution3(); 
	}

	/** Clears the state; in particular, releases all pointers to external 
	 *  objects and clears the witness substitution; in particular,
	 *  unlocks the host index if it was locked by retrieval.
	 */
	public final void clear() {
	    _leaves.clear();

	    if (_query != null) --_locks; 

	    _query = null;
	    _clauseIter = null;
	    _nextClause = null;
	    _oneToOneSubsumption.clear();
	    assert _witnessSubstitution.empty();
	}
	
	/** Initiates a new retrieval round; automatically locks 
	 *  the host index unless the object has been already
	 *  performing retrieval.
	 */
	public final void reset(Clause query) {

	    //System.out.println("BS QUERY " + query);

	    if (_query != null) --_locks; 

	    _query = query;
	    ++_locks;

	    _featureFunctionVector.
		evaluate(_query.literals(),
			 _queryFeatureVector,
			 0);

	    _leaves.reset(_queryFeatureVector);

	    if (_leaves.hasNext())
		{
		    _clauseIter = _leaves.next().iterator();
		    assert _clauseIter.hasNext();
			    
		    _oneToOneSubsumption.setSubsumer(_query.literals());
			    
		    findNextSubsumedClause();
		}
	    else
		{
		    _nextClause = null;
		};
		    
	} // reset(NewClause query)



	public final boolean hasNext() { 

	    //if (_nextClause != null)
	    //System.out.println("EXISTS B SUBSUMED " + _nextClause);

	    return _nextClause != null;
	}

	
	/** Next subsumed clause; cannot be the same object as 
	 *  the current query, although it can be syntactically
	 *  identical.
	 */
	public 
	    final 
	    Clause next() throws java.util.NoSuchElementException {
	    
	    if (!hasNext())
		throw new java.util.NoSuchElementException();

	    Clause result = _nextClause;

	    findNextSubsumedClause();
	    
	    //System.out.println("B SUBSUMED " + result);

	    return result;

	} // next()



	/** Cannot be used. */
	public final void remove() throws Error {
	    throw 
		new Error("Forbidden method call: alabai_je.BackwardSubsumptionIndex.Retrieval.remove()");
	}



	private void findNextSubsumedClause() {
	    while (_clauseIter.hasNext())
		{
		    _nextClause = _clauseIter.next();

		    if (_nextClause != _query &&
			!_nextClause.isDiscarded() &&
			_oneToOneSubsumption.subsume(_nextClause.literals(),
						     _witnessSubstitution))
			{
			    _witnessSubstitution.uninstantiateAll();
			    return;
			};
			    
		    if (!_clauseIter.hasNext())
			if (_leaves.hasNext())
			    {
				_clauseIter = _leaves.next().iterator();
				assert _clauseIter.hasNext();
			    };
			    
		}; // while (__clauseIter.hasNext())
		    
		    
	    _nextClause = null;
		    
	} // findNextSubsumedClause()


	//               Data:


	private
	final
	    FeatureVectorIndex<LinkedList<Clause>>.Subsumed 
	    _leaves;

	private Clause _query;

	private final FeatureVector.ArrayBased _queryFeatureVector;
	
	private Iterator<Clause> _clauseIter;

	private Clause _nextClause;

	private final BackwardSubsumption _oneToOneSubsumption;
	
	private final Substitution3 _witnessSubstitution;

    } // class Retrieval




    //               Data:

    private int _locks;

    private ClauseFeatureVector _featureFunctionVector;

    private FeatureVectorIndex<LinkedList<Clause>> _index;

    private FeatureVector.ArrayBased _featureVector;
    

} // class BackwardSubsumptionIndex