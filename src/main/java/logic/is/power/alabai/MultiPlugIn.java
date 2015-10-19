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

/** Allows to treat a collection of plugins as a single plugin;
 *  the order of member plugins is essential -- features
 *  in later plugins override the corresponding features in 
 *  earlier plugins.
 */
/* package */ class MultiPlugIn implements PlugIn {
    
    public MultiPlugIn() {
	_members = new LinkedList<PlugIn>();
    }

    public  MultiPlugIn(Collection<PlugIn> members) {
	_members = new LinkedList<PlugIn>();
	for (PlugIn m : members)
	    _members.addFirst(m);
    }
    
    public final void addFirst(PlugIn m) {
	_members.addLast(m);
    }

    public final void addLast(PlugIn m) {
	_members.addFirst(m);
    }

    
    /** Finds a user-defined {@link alabai_je#ScalarClauseFeature} object 
     *  associated with the specified name.
     *  @return null if <code>this</code> does not contain such object.
     */
    public final ScalarClauseFeature getScalarClauseFeature(String name) {
	
	for (PlugIn mem : _members)
	    {
		ScalarClauseFeature result = mem.getScalarClauseFeature(name);
		if (result != null) return result;
	    };
	return null;
    }


    /** Finds a user-defined {@link alabai_je#ScalarTermFeature} object 
     *  associated with the specified name.
     *  @return null if <code>this</code> does not contain such object.
     */
    public final ScalarTermFeature getScalarTermFeature(String name) {
	
	for (PlugIn mem : _members)
	    {
		ScalarTermFeature result = mem.getScalarTermFeature(name);
		if (result != null) return result;
	    };
	return null;
    }


    private LinkedList<PlugIn> _members;

} // class MultiPlugIn