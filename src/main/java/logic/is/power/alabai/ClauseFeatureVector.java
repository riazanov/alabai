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


/** Vectors of clause feature functions; allows VectorClauseFeature
 *  instances as subvectors.
 */
public class ClauseFeatureVector {

    public ClauseFeatureVector() {
	_components = new LinkedList<ClauseFeature>();
	_size = 0; 
    }

    public ClauseFeatureVector(Collection<? extends ClauseFeature> components) {
	_components = new LinkedList<ClauseFeature>(components);
	_size = 0;
	for (ClauseFeature f : _components)
	    if (f instanceof VectorClauseFeature)
		{
		    _size += ((VectorClauseFeature)f).size();
		}
	    else 
		++_size;
    }
    
    public ClauseFeatureVector(ClauseFeatureVector v) {
	_components = new LinkedList<ClauseFeature>(v._components);
	_size = v._size;
    }

    public final void add(ClauseFeature f) {
	_components.addLast(f);
	if (f instanceof VectorClauseFeature)
	    {
		_size += ((VectorClauseFeature)f).size();
	    }
	else 
	    {
		assert (f instanceof ScalarClauseFeature);
		++_size;
	    };
    }

    public final Collection<ClauseFeature> components() {
	return _components;
    }
    
    public final int size() { return _size; }
    
    
    public final void evaluate(Collection<Literal> cl,
			       FeatureVector.ArrayBased result,
			       int offset) {

	for (ClauseFeature f : _components)
	    if (f instanceof VectorClauseFeature)
		{
		    int[] subvector =
			((VectorClauseFeature)f).evaluate(cl);
		    for (int n = 0; n < ((VectorClauseFeature)f).size(); ++n)
			{
			    result.set(offset,subvector[n]);
			    ++offset;
			};
		}
	    else 
		{
		    result.set(offset,((ScalarClauseFeature)f).evaluate(cl));
		    ++offset;
		}

    } // evaluate(Collection<Literal> cl,


    public final void evaluateTemp(Collection<TmpLiteral> cl,
				   FeatureVector.ArrayBased result,
				   int offset) {


	for (ClauseFeature f : _components)
	    if (f instanceof VectorClauseFeature)
		{
		    int[] subvector =
			((VectorClauseFeature)f).evaluateTemp(cl);
		    for (int n = 0; n < ((VectorClauseFeature)f).size(); ++n)
			{
			    result.set(offset,subvector[n]);
			    ++offset;
			};
		}
	    else 
		{
		    result.set(offset,
			       ((ScalarClauseFeature)f).evaluateTemp(cl));
		    ++offset;
		}


    } // evaluateTemp(Collection<TmpLiteral> cl,




    private LinkedList<ClauseFeature> _components;
 
    private int _size;

} // class ClauseFeatureVector
