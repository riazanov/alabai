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

/** References to all kinds of objects that can be imported from plug-ins.*/
public class PlugInReference {

    /** Thrown when some reference cannot be resolved in some 
     *  plug-ins.
     */
    public static class UnresolvedReferenceException 
	extends java.lang.Exception {
	
	public UnresolvedReferenceException(String message) {
	    super(message);
	}

    }

    public static class ScalarClauseFeature 
	extends PlugInReference 
	implements logic.is.power.alabai.ScalarClauseFeature  {
	
	public ScalarClauseFeature(String name) {
	    _name = name;
	    _binding = null;
	}
	

	public void bind(logic.is.power.alabai.ScalarClauseFeature binding) {
	    _binding = binding;
	}
	
	public int evaluate(Collection<Literal> cl) {
	    assert _binding != null;
	    return _binding.evaluate(cl);
	}

	public int evaluateTemp(Collection<TmpLiteral> cl) {
	    assert _binding != null;
	    return _binding.evaluateTemp(cl);
	}   

	private logic.is.power.alabai.ScalarClauseFeature _binding;

    } // class ScalarClauseFeature



    public static class ScalarTermFeature 
	extends PlugInReference 
	implements logic.is.power.alabai.ScalarTermFeature  {
	
	public ScalarTermFeature(String name) {
	    _name = name;
	    _binding = null;
	}
	

	public void bind(logic.is.power.alabai.ScalarTermFeature binding) {
	    _binding = binding;
	}
	
	public int evaluate(Term term) {
	    assert _binding != null;
	    return _binding.evaluate(term);
	}

	public int evaluate(Flatterm term) {
	    assert _binding != null;
	    return _binding.evaluate(term);
	}   

	private logic.is.power.alabai.ScalarTermFeature _binding;

    } // class ScalarTermFeature


    /** Resolves all references to plug-in objects in <code>options</code>.
     *  For example, if 
     *  <code>options.forwardSubsumption.featureFunctionVector</code>
     *  contains a reference "MyClauseFeature", it will be resolved
     *  to the corresponding {@link logic.is.power.alabai#ClauseFeature} object
     *  provided by the plug-in, which should make it usable for indexing.
     *  @throws UnresolvedReferenceException if some reference
     *          cannot be resolved with the specified plug-in.
     */
    public static void resolve(KernelOptions options,PlugIn plugIn) 
    throws UnresolvedReferenceException {

	for (logic.is.power.alabai.ClauseFeature f : 
		 options.forwardSubsumption.featureFunctionVector.components())
	    if (f instanceof PlugInReference.ScalarClauseFeature)
		{
		    // Bind this feature or report an error:
		    
		    logic.is.power.alabai.ScalarClauseFeature plugInFeature =
			plugIn.getScalarClauseFeature(((PlugInReference.ScalarClauseFeature)f).
						_name);

		    if (plugInFeature == null)
			throw 
			    new UnresolvedReferenceException("Cannot resolve " + 
							     ((PlugInReference.ScalarClauseFeature)f).
							     _name + " in KernelOptions.forwardSubsumption.featureFunctionVector");
							     

		    ((PlugInReference.ScalarClauseFeature)f).bind(plugInFeature);
		};


	for (logic.is.power.alabai.TermFeature f : 
		 options.forwardDemodulation.featureFunctionVector.components())
	    if (f instanceof PlugInReference.ScalarTermFeature)
		{
		    // Bind this feature or report an error:
		    
		    logic.is.power.alabai.ScalarTermFeature plugInFeature =
			plugIn.getScalarTermFeature(((PlugInReference.ScalarTermFeature)f).
						_name);

		    if (plugInFeature == null)
			throw 
			    new UnresolvedReferenceException("Cannot resolve " + 
							     ((PlugInReference.ScalarTermFeature)f).
							     _name + " in KernelOptions.forwardDemodulation.featureFunctionVector");
							     

		    ((PlugInReference.ScalarTermFeature)f).bind(plugInFeature);
		};



	for (logic.is.power.alabai.ClauseFeature f : 
		 options.backwardSubsumption.featureFunctionVector.components())
	    if (f instanceof PlugInReference.ScalarClauseFeature)
		{
		    // Bind this feature or report an error:
		    
		    logic.is.power.alabai.ScalarClauseFeature plugInFeature =
			plugIn.getScalarClauseFeature(((PlugInReference.ScalarClauseFeature)f).
						_name);

		    if (plugInFeature == null)
			throw 
			    new UnresolvedReferenceException("Cannot resolve " + 
							     ((PlugInReference.ScalarClauseFeature)f).
							     _name + " in KernelOptions.backwardSubsumption.featureFunctionVector");
							     

		    ((PlugInReference.ScalarClauseFeature)f).bind(plugInFeature);
		};


	for (logic.is.power.alabai.TermFeature f : 
		 options.backwardDemodulation.featureFunctionVector.components())
	    if (f instanceof PlugInReference.ScalarTermFeature)
		{
		    // Bind this feature or report an error:
		    
		    logic.is.power.alabai.ScalarTermFeature plugInFeature =
			plugIn.getScalarTermFeature(((PlugInReference.ScalarTermFeature)f).
						_name);

		    if (plugInFeature == null)
			throw 
			    new UnresolvedReferenceException("Cannot resolve " + 
							     ((PlugInReference.ScalarTermFeature)f).
							     _name + " in KernelOptions.backwardDemodulation.featureFunctionVector");
							     

		    ((PlugInReference.ScalarTermFeature)f).bind(plugInFeature);
		};

    } // resolve(KernelOptions options,PlugIn plugIn)



    /** Identifies the plug-in value. */
    protected String _name;
    
    
} // class PlugInReference 