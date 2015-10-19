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

/** Index for forward subsumption and forward subsumption resolution. */
final class ForwardSubsumptionIndex {

    public ForwardSubsumptionIndex() {
	_numberOfQueries = 0;
	_numberOfOneToOneSubsumptionTests = 0;
	_numberOfSuccessfulEqualityPretests = 0;
    }

    /** Feature functions that will be used to compute feature
     *  vector on indexed clauses and queries;
     *  <b>pre:</b> the index is empty.
     */
    public 
	void 
	setFeatureFunctionVector(ClauseFeatureVector vector) {
	_featureFunctionVector = vector;
	_featureVector = 
	    new FeatureVector.ArrayBased(vector.size());
	_index = new FeatureVectorIndex<LinkedList<Clause>>(vector.size());
	_queryFeatureVector = 
	    new FeatureVector.ArrayBased(vector.size());
    }


    public String performanceSummary() {
	String result = "% Forward subsumption: ";
	if (_numberOfQueries == 0) 
	    {
		result += "NO QUERIES";
	    }
	else
	    {
		result += _numberOfOneToOneSubsumptionTests + " 1-to-1 tests / " + _numberOfQueries + " queries = " + (((double)_numberOfOneToOneSubsumptionTests)/_numberOfQueries) + "; " + _numberOfSuccessfulEqualityPretests + " succ. eq. pretests";
	    };
	return result;
    }

    public void insert(Clause cl) {

	//System.out.println("FS INSERT " + cl);

	if (cl.literals().isEmpty()) return;

	    
	_featureFunctionVector.
	    evaluate(cl.literals(),
		     _featureVector,
		     0);

	//System.out.println("       FEATURES: " + _featureVector);
	

	Ref<LinkedList<Clause>> indexedObject =
	    _index.insert(_featureVector);

	if (indexedObject.content == null)
	    indexedObject.content = new LinkedList<Clause>();
	
	indexedObject.content.addLast(cl);
	
    } // insert(Clause cl)

    
    /** @return false if the clause is not in the index */
    public boolean erase(Clause cl) {

	//System.out.println("FS ERASE " + cl);

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


    /** Fast equality pretest; this is a very experimental
     *  feature that has not proven useful so far, so it
     *  may be removed any time.
     */
    public final Clause findEqualClause(Collection<TmpLiteral> query) {
		
	    
	_featureFunctionVector.
	    evaluateTemp(query,
			 _queryFeatureVector,
			 0);
	
	Ref<LinkedList<Clause>> clausesRef = 
	    _index.find(_queryFeatureVector);
	
	if (clausesRef == null) return null;
	

	int queryHashCode = Clause.hashCode(query);

	for (Clause cl : clausesRef.content)
	    {
		
	        //System.out.println("EQUAL? " + query + " ---->\n    " + cl);
		if (!cl.isDiscarded() && 
		    queryHashCode == cl.literalsHashCode() &&
		    areEqual(query,cl.literals()))
		    {
			++_numberOfQueries;
			++_numberOfSuccessfulEqualityPretests;
			return cl;
		    };
	    };

	return null; 

    } // findEqualClause(Collection<TmpLiteral> query)




    /** Iteration over all (undiscarded) indexed clauses subsuming 
     *  a given query clause.
     */
    public class Retrieval implements java.util.Iterator<Clause> {
	
	/** <b>pre:</b> 
	 *  {@link logic_warehouse_je#ForwardSubsumptionIndex#setFeatureFunctionVector()} 
	 *  must have been called.
	 */
	public Retrieval() {
	    _leaves = _index.new Subsuming();
	    _queryFeatureVector = 
		new FeatureVector.ArrayBased(_featureFunctionVector.size());
	    _oneToOneSubsumption = new ForwardSubsumption();
	    _witnessSubstitution = new Substitution2(); 
	}

	/** Clears the state; in particular, releases all pointers to external 
	 *  objects and clears the witness substitution. 
	 */
	public final void clear() {
	    _leaves.clear();
	    _query = null;
	    _clauseIter = null;
	    _nextClause = null;
	    _oneToOneSubsumption.clear();
	    assert _witnessSubstitution.empty();
	}
	
	public final void reset(Collection<TmpLiteral> query) {

	    //System.out.println("FS QUERY " + query);

	    ++_numberOfQueries;

	    _query = query;

	    _featureFunctionVector.
		evaluateTemp(_query,
			     _queryFeatureVector,
			     0);

	    _leaves.reset(_queryFeatureVector);

	    if (_leaves.hasNext())
		{
		    _clauseIter = _leaves.next().iterator();
		    assert _clauseIter.hasNext();
			    
		    _oneToOneSubsumption.setTestee(_query);
			    
		    findNextSubsumingClause();
		}
	    else
		{
		    _nextClause = null;
		};
		    
	} // reset(NewClause query)



	public final boolean hasNext() { 

	    //if (_nextClause != null)
	    //System.out.println("EXISTS F SUBSUMER " + _nextClause);

	    return _nextClause != null;
	}

	
	/** @return next subsuming clause that has not been discarded */
	public 
	    final 
	    Clause next() throws java.util.NoSuchElementException {
	    
	    if (!hasNext())
		throw new java.util.NoSuchElementException();

	    Clause result = _nextClause;

	    findNextSubsumingClause();
	    
	    //System.out.println("F SUBSUMER " + result);

	    return result;

	} // next()



	/** Cannot be used. */
	public final void remove() throws Error {
	    throw 
		new Error("Forbidden method call: alabai_je.ForwardSubsumptionIndex.Retrieval.remove()");
	}



	private void findNextSubsumingClause() {
	    while (_clauseIter.hasNext())
		{
		     ++_numberOfOneToOneSubsumptionTests;
		     
		    _nextClause = _clauseIter.next();

		    //System.out.println("  FS CAND: " + _nextClause);

		    if (!_nextClause.isDiscarded() &&
			_oneToOneSubsumption.subsumeBy(_nextClause.literals(),
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
		    
	} // findNextSubsumingClause()


	//               Data:


	private
	final
	    FeatureVectorIndex<LinkedList<Clause>>.Subsuming 
	    _leaves;

	private Collection<TmpLiteral> _query;

	private final FeatureVector.ArrayBased _queryFeatureVector;
	
	private Iterator<Clause> _clauseIter;

	private Clause _nextClause;

	private final ForwardSubsumption _oneToOneSubsumption;
	
	private final Substitution2 _witnessSubstitution;

    } // class Retrieval




    //               Private methods:
    
    private boolean areEqual(Collection<TmpLiteral> tmpLitList,
			     Collection<Literal> litList) {
	
	//System.out.println("EQUAL? " + tmpLitList + " ---->\n    " + litList);

	if (tmpLitList.size() != litList.size()) return false;

	Iterator<TmpLiteral> iter = tmpLitList.iterator();
	for (Literal lit : litList)
	    {
		TmpLiteral tmpLit = iter.next();

		if (!lit.body().equals(tmpLit.body()))
		    {
			//System.out.println("DIFFER: " + lit + " != " + tmpLit);
			return false;
		    };
	    };
	
	return true;

    } // areEqual(Collection<TmpLiteral> tmpLitList,



    //               Data:

    private ClauseFeatureVector _featureFunctionVector;

    private FeatureVectorIndex<LinkedList<Clause>> _index;

    private FeatureVector.ArrayBased _featureVector;
    
    /** For finding equal clauses. */
    private FeatureVector.ArrayBased _queryFeatureVector;

    private long _numberOfQueries;

    private long _numberOfOneToOneSubsumptionTests;

    private long _numberOfSuccessfulEqualityPretests;

} // class ForwardSubsumptionIndex