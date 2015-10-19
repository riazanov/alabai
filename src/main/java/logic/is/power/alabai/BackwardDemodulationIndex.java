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


/** Index for backward demodulation; indexed objects are of the type
 *  {@link cushion_je#Pair}<code><Clause,Term></code>,
 *  where the clause is the indexed clause and the term is a potential 
 *  redex; note that only individual-valued terms can be redexes.
 */
final class BackwardDemodulationIndex {
    
    public BackwardDemodulationIndex() {
	_termToClauseMap = 
	    new HashMap<Term,LinkedList<Clause>>(); 
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
	_termIndex = 
	    new FeatureVectorIndex<LinkedList<Term>>(vector.size());
    }

    public void insert(Clause cl) {
	
	assert _featureFunctionVector != null; 
	// setFeatureFunctionVector(..) must have been called.
	
	for (Literal lit : cl.literals())
	    {
		Term.LeanIterator subtermIter = 
		    new Term.LeanIterator(lit.atom());
		    
		do
		    {
			Term subterm = subtermIter.next();
			if (!subterm.isVariable() &&
			    subterm.isIndividualValued())
			    {
				LinkedList<Clause> clauses = 
				    _termToClauseMap.get(subterm);
				
				if (clauses == null)
				    {
					clauses = new LinkedList<Clause>();
					clauses.addLast(cl);
					_termToClauseMap.put(subterm,clauses);

					// Integrate the term into _termIndex:

					_featureFunctionVector.
					    evaluate(subterm,
						     _featureVector,
						     0);

					Ref<LinkedList<Term>> indexedObjectList = 
					    _termIndex.insert(_featureVector);
					
					assert _termIndex.find(_featureVector) != null;
					
					if (indexedObjectList.content == null)
					    indexedObjectList.content = 
						new LinkedList<Term>();
					
					assert 
					    !indexedObjectList.content.contains(subterm);

					indexedObjectList.content.addLast(subterm);
				    }
				else if (!clauses.contains(cl))
				    {
					assert !clauses.isEmpty();
					clauses.addLast(cl);

					// The term must be already in _termIndex,
					// because otherwise we would have 
					// clauses == null
				    };

				    
				
			    }; // if (!subterm.isVariable() &&
		    }
		while (subtermIter.hasNext());
	    }; // for (Literal lit : cl.literals())

    } // insert(Clause cl)



    /** @return false if the clause is not in the index */
    public boolean remove(Clause cl) {

	boolean result = false;

	for (Literal lit : cl.literals())
	    {
		Term.LeanIterator subtermIter = 
		    new Term.LeanIterator(lit.atom());
		    
		do
		    {
			Term subterm = subtermIter.next();
			if (!subterm.isVariable() &&
			    subterm.isIndividualValued())
			    {
				LinkedList<Clause> clauses = 
				    _termToClauseMap.get(subterm);
				
				if (clauses != null && clauses.contains(cl))
				    {
					result = true;

					clauses.remove(cl);

					if (clauses.isEmpty())
					    {
						_termToClauseMap.remove(subterm);

						// Remove the term from _termIndex:

						_featureFunctionVector.
						    evaluate(subterm,
							     _featureVector,
							     0);

						
						Ref<LinkedList<Term>> indexedObjectList = 
						    _termIndex.find(_featureVector);
						
						assert indexedObjectList != null;
						// Because cl was in _termToClauseMap.get(subterm)
		
						assert 
						    indexedObjectList.content != null;				
						assert 
						    indexedObjectList.content.contains(subterm);
						
						indexedObjectList.content.remove(subterm);

						if (indexedObjectList.content.isEmpty())
						    _termIndex.remove(_featureVector,
								      new Ref<LinkedList<Term>>());
						


					    }; // if (clauses.isEmpty())


				    }; // if (clauses != null && clauses.contains(cl))

			    }; // if (!subterm.isVariable() &&
		    }
		while (subtermIter.hasNext());

	    }; // for (Literal lit : cl.literals())


	return result;

    } // remove(Clause cl)
    




    /** Iteration over all redexes (with undiscarded clauses) 
     *  that can be rewritten with the given oriented equality;
     *  witness substitutions are available for all retrieved objects;
     *  the ordering constraint is checked on the query equality 
     *  on every witness substitution.
     */
    public class Retrieval 
	implements java.util.Iterator<Pair<Clause,Term>> {
	

	/** <b>pre:</b> 
	 *  {@link logic_warehouse_je#BackwardSubsumptionIndex#setFeatureFunctionVector()} 
	 *  must have been called on the host index.
	 */
	public Retrieval() {
	    _leaves = _termIndex.new Subsumed();
	    _queryFeatureVector = 
		new FeatureVector.ArrayBased(_featureFunctionVector.size());
	    _oneToOneMatching = new Matching3();
	    _witnessSubstitution = new Substitution3(); 
	}


	/** Clears the state; in particular, releases all pointers to external 
	 *  objects and clears the witness substitution. 
	 */
	public final void clear() {
	    _leaves.clear();
	    _queryLHS = null;
	    _queryRHS = null;
	    _redexCandidate = null;
	    _leafContentIter = null;
	    _clauseIter = null;
	    _nextIndexedObject = null;
	    _witnessSubstitution.uninstantiateAll();
	}
	

	/** @param lhs the LHS of the query equality; retrieved
	 *         terms must be instances of <code>lhs</code>
	 *         wrt the corresponding witness substitutions
	 *  @param rhs the RHS of the query equality; 
	 *         all witness substitutions must make 
	 *         <code>rhs</code> smaller than <code>lhs</code>
	 *         in the current reduction ordering.
	 */
	public final void reset(Term lhs,Term rhs) {

	    _queryLHS = lhs;
	    _queryRHS = rhs;

	    
	    _featureFunctionVector.
		evaluate(_queryLHS,
			 _queryFeatureVector,
			 0);


	    _leaves.reset(_queryFeatureVector);

	    _clauseIter = null;
	    
	    _tryToFindNext = true;

	} // reset(Term lhs,Term rhs)
	



	public final boolean hasNext() { 
	    if (_tryToFindNext)
		{
		  findNextInstance();
		  _tryToFindNext = false;
		};
	    return _nextIndexedObject != null;
	}

	public 
	    final 
	    Pair<Clause,Term> next() 
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
		new Error("Forbidden method call: alabai_je.BackwardDemodulationIndex.Retrieval.remove()");
	}


	public final Substitution3 witnessSubstitution() {
	    return _witnessSubstitution;
	}





	/** Also checks the ordering constraints. */
	private void findNextInstance() {
	    
	    if (_clauseIter == null)
		{
		    // First attempt to find something:
		    if (!_leaves.hasNext())
			{
			    // End of iteration.
			    _nextIndexedObject = null;
			    return;
			};
		    
		    _leafContentIter = _leaves.next().iterator();
		    assert _leafContentIter.hasNext();
		    
		    while (!tryNextTerm())
			{
			    // Another leaf has to be found:
			    if (!_leaves.hasNext())
				{
				    // End of iteration.
				    _nextIndexedObject = null;
				    return;
				};
			    _leafContentIter = _leaves.next().iterator();
			    assert _leafContentIter.hasNext();
			}; // while (!tryNextTerm())
		    
		}; // if (_clauseIter == null)


	    assert _clauseIter != null;

	    while (!tryNextClause())
		{
		    // Another term has to be considered:
		    while (!tryNextTerm())
			{
			    // Another leaf has to be found:
			    if (!_leaves.hasNext())
				{
				    // End of iteration.
				    _nextIndexedObject = null;
				    return;
				};
			    _leafContentIter = _leaves.next().iterator();
			    assert _leafContentIter.hasNext();
			}; // while (!tryNextTerm())
		}; // while (!tryNextClause())
	    
	    // Eligible idexed object is found.
	    return;

	} // findNextInstance()





	private boolean tryNextClause() {

	    if (!_leafContentIter.hasNext()) return false;

	    assert _redexCandidate != null;
	    
	    while (_clauseIter.hasNext())
		{
		    Clause cl = _clauseIter.next();
		    
		    if (!cl.isDiscarded())
			{
			    _nextIndexedObject = 
					new Pair<Clause,Term>(cl,
							      _redexCandidate);
			    return true;
			};
		};	

	    return false;

	} // tryNextClause()

	


	private boolean tryNextTerm() {

	    if (!_leafContentIter.hasNext()) return false;

	    _redexCandidate = _leafContentIter.next();

	    if (_oneToOneMatching.match(_queryLHS,
					_redexCandidate,
					_witnessSubstitution) &&
		checkOrderingConstraint())
		{
		    // The term is OK.

		    LinkedList<Clause> clauses = 
			_termToClauseMap.get(_redexCandidate);

		    assert clauses != null;
		    assert !clauses.isEmpty();
			    
		    _clauseIter = clauses.iterator();
		    
		    return true;
		};
		    
	    
	    return false;

	} // tryNextTerm()


	/** Checks if <code>_queryLHS</code> is greater than 
	 *  <code>_queryRHS</code> modulo global substitution 3.
	 */
	private boolean checkOrderingConstraint() {

	    return 
		ReductionOrdering.
		current().greaterModuloSubst3(_queryLHS,_queryRHS);

	} 


	//               Data:


	private Term _queryLHS;

	private Term _queryRHS;

	private final FeatureVector.ArrayBased _queryFeatureVector;
	
	private
	final
	    FeatureVectorIndex<LinkedList<Term>>.
	    Subsumed 
	    _leaves;

	private final Matching3 _oneToOneMatching;
	
	private final Substitution3 _witnessSubstitution;

	private Term _redexCandidate;

	/** Current position in a feature-vector index leaf. */
	private Iterator<Term> _leafContentIter;

	/** Current position in the clause list corresponding
	 *  to _redexCandidate.
	 */
	private Iterator<Clause> _clauseIter;

	private Pair<Clause,Term> _nextIndexedObject;
	
	private boolean _tryToFindNext;

    } // class Retrieval 




    //               Data:


    private TermFeatureVector _featureFunctionVector;

    private 
	FeatureVectorIndex<LinkedList<Term>> 
	_termIndex;

    private final HashMap<Term,LinkedList<Clause>> _termToClauseMap;

    private FeatureVector.ArrayBased _featureVector;


} // class BackwardDemodulationIndex
    