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

import logic.is.power.logic_warehouse.*;

/** Vectors of term feature functions; allows VectorTermFeature
 *  instances as subvectors.
 */
public class TermFeatureVector {

    public TermFeatureVector() {
	_components = new LinkedList<TermFeature>();
	_size = 0; 
    }

    public TermFeatureVector(Collection<TermFeature> components) {
	_components = new LinkedList<TermFeature>(components);
	_size = 0;
	for (TermFeature f : _components)
	    if (f instanceof VectorTermFeature)
		{
		    _size += ((VectorTermFeature)f).size();
		}
	    else 
		++_size;
    }
    
    public TermFeatureVector(TermFeatureVector v) {
	_components = new LinkedList<TermFeature>(v._components);
	_size = v._size;
    }

    public final void add(TermFeature f) {
	_components.addLast(f);
	if (f instanceof VectorTermFeature)
	    {
		_size += ((VectorTermFeature)f).size();
	    }
	else 
	    {
		assert (f instanceof ScalarTermFeature);
		++_size;
	    };
    }

    public final Collection<TermFeature> components() {
	return _components;
    }
    
    
    public final int size() { return _size; }
    
    
    public final void evaluate(Term term,
			       FeatureVector.ArrayBased result,
			       int offset) {

	for (TermFeature f : _components)
	    if (f instanceof VectorTermFeature)
		{
		    int[] subvector =
			((VectorTermFeature)f).evaluate(term);
		    for (int n = 0; n < ((VectorTermFeature)f).size(); ++n)
			{
			    result.set(offset,subvector[n]);
			    ++offset;
			};
		}
	    else 
		{
		    result.set(offset,((ScalarTermFeature)f).evaluate(term));
		    ++offset;
		}

    } // evaluate(Collection<Literal> cl,


    public final void evaluate(Flatterm term,
			       FeatureVector.ArrayBased result,
			       int offset) {


	for (TermFeature f : _components)
	    if (f instanceof VectorTermFeature)
		{
		    int[] subvector =
			((VectorTermFeature)f).evaluate(term);
		    for (int n = 0; n < ((VectorTermFeature)f).size(); ++n)
			{
			    result.set(offset,subvector[n]);
			    ++offset;
			};
		}
	    else 
		{
		    result.set(offset,((ScalarTermFeature)f).evaluate(term));
		    ++offset;
		}



    } // evaluateTemp(Collection<TmpLiteral> cl,




    private LinkedList<TermFeature> _components;
 
    private int _size;

} // class TermFeatureVector
