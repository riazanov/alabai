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

/** Index for forward demodulation; indexed objects are of the type
 *  {@link cushion_je#Triple}<code><Clause,Literal,Term></code>,
 *  where the clause is the indexed clause, the literal identifies the positive
 *  equality and the term identifies the LHS of the equality.
 */
final class ForwardDemodulationIndex {
    
    public ForwardDemodulationIndex() {
    }
    

    /** Feature functions that will be used to compute feature
     *  vector on indexed terms and queries;
     *  <b>pre:</b> the index is empty.
     */
    public 
	void 
	setFeatureFunctionVector(TermFeatureVector vector) {
	_featureFunctionVector = vector;
	_featureVector = 
	    new FeatureVector.ArrayBased(vector.size());
	_index = 
	    new FeatureVectorIndex<LinkedList<Triple<Clause,Literal,Term>>>(vector.size());
    }


    /** Only orientable unit equality clauses are currently actually indexed. */
    public void insert(Clause cl) {

	if (cl.literals().size() != 1) return;

	Literal lit = cl.literals().iterator().next();

	if (!lit.isEquality() || lit.isNegative()) return;
	
	Term arg1 = lit.atom().firstArg();
	Term arg2 = lit.atom().secondArg();

	if (ReductionOrdering.
	    current().
	    canBeGreaterModuloSubst(arg1,arg2))
	    insert(new Triple<Clause,Literal,Term>(cl,lit,arg1));

	if (ReductionOrdering.
	    current().
	    canBeGreaterModuloSubst(arg2,arg1))
	    insert(new Triple<Clause,Literal,Term>(cl,lit,arg2));

    } // insert(Clause cl)
    
    

    /** @return false if the clause is not in the index */
    public boolean remove(Clause cl) {

	if (cl.literals().size() != 1) return false;

	Literal lit = cl.literals().iterator().next();

	if (!lit.isEquality() || lit.isNegative()) return false;
	
	Term arg1 = lit.atom().firstArg();
	Term arg2 = lit.atom().secondArg();

	boolean result1 = remove(cl,arg1);

	boolean result2 = remove(cl,arg2);

	return result1 || result2;
	
    } // remove(Clause cl)
    

    /** Iteration over all indexed objects (with undiscarded 
     *  clauses) whose LHS term component is a generalisation of 
     *  a given query term; witness substitutions are available 
     *  for all retrieved objects; the ordering constraint is checked
     *  on the LHS and RHS for every retrieved indexed object.
     */
    public class Retrieval 
	implements java.util.Iterator<Triple<Clause,Literal,Term>> {
	
	
	/** <b>pre:</b> 
	 *  {@link logic_warehouse_je#ForwardSubsumptionIndex#setFeatureFunctionVector()} 
	 *  must have been called on the host index.
	 */
	public Retrieval() {
	    _leaves = _index.new Subsuming();
	    _queryFeatureVector = 
		new FeatureVector.ArrayBased(_featureFunctionVector.size());
	    _oneToOneMatching = new Matching2();
	    _witnessSubstitution = new Substitution2(); 
	}
	

	/** Clears the state; in particular, releases all pointers to external 
	 *  objects and clears the witness substitution. 
	 */
	public final void clear() {
	    _leaves.clear();
	    _query = null;
	    _indexedObjectIter = null;
	    _nextIndexedObject = null;
	    _witnessSubstitution.uninstantiateAll();
	}
	

	/** <b>pre:</b> !query.isVariable() */
	public final void reset(Flatterm query) {
	    
	    //System.out.println("FD QUERY " + query);

	    _query = query;

	    _featureFunctionVector.
		evaluate(_query,
			 _queryFeatureVector,
			 0);
	    

	    _leaves.reset(_queryFeatureVector);

	    if (_leaves.hasNext())
		{
		    _indexedObjectIter = _leaves.next().iterator();
		    assert _indexedObjectIter.hasNext();

		    _tryToFindNext = true;
		}
	    else
		{
		    _tryToFindNext = false;
		    _nextIndexedObject = null;
		};

	} // reset(Flatterm query) 


	public final boolean hasNext() { 
	    if (_tryToFindNext)
		{
		  findNextGeneralisation();
		  _tryToFindNext = false;
		};
	    return _nextIndexedObject != null;
	}

	
	public 
	    final 
	    Triple<Clause,Literal,Term> next() 
	    throws java.util.NoSuchElementException {
	    
	    if (!hasNext())
		throw new java.util.NoSuchElementException();
	    
	    assert !_tryToFindNext;

	    _tryToFindNext = true;

	    return _nextIndexedObject;

	} // next()





	/** Cannot be used. */
	public final void remove() throws Error {
	    throw 
		new Error("Forbidden method call: alabai_je.ForwardDemodulationIndex.Retrieval.remove()");
	}


	public final Substitution2 witnessSubstitution() {
	    return _witnessSubstitution;
	}


	/** Also checks the ordering constraints. */
	private void findNextGeneralisation() {
	    
	    while (_indexedObjectIter.hasNext())
		{
		    _nextIndexedObject = _indexedObjectIter.next();

		    _witnessSubstitution.uninstantiateAll();

			    
		    if (!_nextIndexedObject.first.isDiscarded() &&
			_oneToOneMatching.match(_nextIndexedObject.third,
						_query,
						_witnessSubstitution) &&
			checkOrderingConstraint(_nextIndexedObject.second,
						_nextIndexedObject.third))
			return;

		    if (!_indexedObjectIter.hasNext())
			if (_leaves.hasNext())
			    {
				_indexedObjectIter = _leaves.next().iterator();
				assert _indexedObjectIter.hasNext();
			    };
			    
		}; // while (_indexedObjectIter.hasNext())
	    
	    _nextIndexedObject = null;

	} // findNextGeneralisation()


	/** Checks that <code>lhs</code> is greater than the other
	 *  argument of the positive equality <code>lit</code>
	 *  modulo global substitution 2.
	 */
	private boolean checkOrderingConstraint(Literal lit,Term lhs) {

	    Term rhs =
		(lit.atom().firstArg() == lhs)?
		lit.atom().secondArg()
		:
		lit.atom().firstArg();
	    
	    return 
		ReductionOrdering.
		current().greaterModuloSubst2(lhs,rhs);

	} // checkOrderingConstraint(Literal lit,..)



	//               Data:

	private
	final
	    FeatureVectorIndex<LinkedList<Triple<Clause,Literal,Term>>>.
	    Subsuming 
	    _leaves;

	private Flatterm _query;

	private final FeatureVector.ArrayBased _queryFeatureVector;
	
	private Iterator<Triple<Clause,Literal,Term>> _indexedObjectIter;

	private Triple<Clause,Literal,Term> _nextIndexedObject;
	

	private final Matching2 _oneToOneMatching;
	
	private final Substitution2 _witnessSubstitution;

	private boolean _tryToFindNext;

    } // class Retrieval 



    private void insert(Triple<Clause,Literal,Term> indexedObject) {
	
	Term term = indexedObject.third;
	    
	_featureFunctionVector.
	    evaluate(term,
		     _featureVector,
		     0);
	
	
	Ref<LinkedList<Triple<Clause,Literal,Term>>> indexedObjectList = 
	    _index.insert(_featureVector);

	if (indexedObjectList.content == null)
	    indexedObjectList.content = 
		new LinkedList<Triple<Clause,Literal,Term>>();
	
	indexedObjectList.content.addLast(indexedObject);

    } // insert(Triple<Clause,Literal,Term> indexedObject)


    private boolean remove(Clause cl,Term term) {

	_featureFunctionVector.
	    evaluate(term,
		     _featureVector,
		     0);
	
	Ref<LinkedList<Triple<Clause,Literal,Term>>> indexedObjectList = 
	    _index.find(_featureVector);
	
	if (indexedObjectList == null) return false;
	
	assert indexedObjectList.content != null;
	assert !indexedObjectList.content.isEmpty();
	
	Iterator<Triple<Clause,Literal,Term>> iter = 
	    indexedObjectList.content.iterator();

	boolean result = false;

	while (iter.hasNext())
	    if (iter.next().first == cl)
		{
		    iter.remove();
		    result = true;
		};
	

	if (indexedObjectList.content.isEmpty())
	    _index.remove(_featureVector,
			  new Ref<LinkedList<Triple<Clause,Literal,Term>>>());

	return result; 

    } // remove(Clause cl,Term term)
    


    //               Data:


    private TermFeatureVector _featureFunctionVector;

    private 
	FeatureVectorIndex<LinkedList<Triple<Clause,Literal,Term>>> 
	_index;

    private FeatureVector.ArrayBased _featureVector;


} // class ForwardDemodulationIndex